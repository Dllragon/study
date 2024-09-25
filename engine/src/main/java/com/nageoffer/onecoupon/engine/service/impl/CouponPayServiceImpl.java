/*
 * 牛券（oneCoupon）优惠券平台项目
 *
 * 版权所有 (C) [2024-至今] [山东流年网络科技有限公司]
 *
 * 保留所有权利。
 *
 * 1. 定义和解释
 *    本文件（包括其任何修改、更新和衍生内容）是由[山东流年网络科技有限公司]及相关人员开发的。
 *    "软件"指的是与本文件相关的任何代码、脚本、文档和相关的资源。
 *
 * 2. 使用许可
 *    本软件的使用、分发和解释均受中华人民共和国法律的管辖。只有在遵守以下条件的前提下，才允许使用和分发本软件：
 *    a. 未经[山东流年网络科技有限公司]的明确书面许可，不得对本软件进行修改、复制、分发、出售或出租。
 *    b. 任何未授权的复制、分发或修改都将被视为侵犯[山东流年网络科技有限公司]的知识产权。
 *
 * 3. 免责声明
 *    本软件按"原样"提供，没有任何明示或暗示的保证，包括但不限于适销性、特定用途的适用性和非侵权性的保证。
 *    在任何情况下，[山东流年网络科技有限公司]均不对任何直接、间接、偶然、特殊、典型或间接的损害（包括但不限于采购替代商品或服务；使用、数据或利润损失）承担责任。
 *
 * 4. 侵权通知与处理
 *    a. 如果[山东流年网络科技有限公司]发现或收到第三方通知，表明存在可能侵犯其知识产权的行为，公司将采取必要的措施以保护其权利。
 *    b. 对于任何涉嫌侵犯知识产权的行为，[山东流年网络科技有限公司]可能要求侵权方立即停止侵权行为，并采取补救措施，包括但不限于删除侵权内容、停止侵权产品的分发等。
 *    c. 如果侵权行为持续存在或未能得到妥善解决，[山东流年网络科技有限公司]保留采取进一步法律行动的权利，包括但不限于发出警告信、提起民事诉讼或刑事诉讼。
 *
 * 5. 其他条款
 *    a. [山东流年网络科技有限公司]保留随时修改这些条款的权利。
 *    b. 如果您不同意这些条款，请勿使用本软件。
 *
 * 未经[山东流年网络科技有限公司]的明确书面许可，不得使用此文件的任何部分。
 *
 * 本软件受到[山东流年网络科技有限公司]及其许可人的版权保护。
 */

package com.nageoffer.onecoupon.engine.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nageoffer.onecoupon.engine.common.constant.EngineRedisConstant;
import com.nageoffer.onecoupon.engine.common.context.UserContext;
import com.nageoffer.onecoupon.engine.dao.entity.CouponSettlementDO;
import com.nageoffer.onecoupon.engine.dao.entity.UserCouponDO;
import com.nageoffer.onecoupon.engine.dao.mapper.CouponSettlementMapper;
import com.nageoffer.onecoupon.engine.dao.mapper.UserCouponMapper;
import com.nageoffer.onecoupon.engine.dto.req.CouponCreatePaymentGoodsReqDTO;
import com.nageoffer.onecoupon.engine.dto.req.CouponCreatePaymentReqDTO;
import com.nageoffer.onecoupon.engine.dto.req.CouponProcessPaymentReqDTO;
import com.nageoffer.onecoupon.engine.dto.req.CouponProcessRefundReqDTO;
import com.nageoffer.onecoupon.engine.dto.req.CouponTemplateQueryReqDTO;
import com.nageoffer.onecoupon.engine.dto.resp.CouponTemplateQueryRespDTO;
import com.nageoffer.onecoupon.engine.service.CouponPayService;
import com.nageoffer.onecoupon.engine.service.CouponTemplateService;
import com.nageoffer.onecoupon.framework.exception.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * 优惠券支付服务相关接口层实现
 * <p>
 * 作者：马丁
 * 加项目群：早加入就是优势！500人内部沟通群，分享的知识总有你需要的 <a href="https://t.zsxq.com/cw7b9" />
 * 开发时间：2024-09-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponPayServiceImpl implements CouponPayService {

    private final CouponTemplateService couponTemplateService;
    private final UserCouponMapper userCouponMapper;
    private final CouponSettlementMapper couponSettlementMapper;
    private final RedissonClient redissonClient;

    @Override
    public void createPaymentRecord(CouponCreatePaymentReqDTO requestParam) {
        RLock lock = redissonClient.getLock(String.format(EngineRedisConstant.LOCK_CREATE_PAYMENT_RECORD_KEY, requestParam.getCouponId()));
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            throw new ClientException("正在创建优惠券结算单，请稍候再试");
        }
        try {
            LambdaQueryWrapper<CouponSettlementDO> queryWrapper = Wrappers.lambdaQuery(CouponSettlementDO.class)
                    .eq(CouponSettlementDO::getCouponId, requestParam.getCouponId())
                    .eq(CouponSettlementDO::getUserId, Long.parseLong(UserContext.getUserId()))
                    .in(CouponSettlementDO::getStatus, 0, 2);

            // 验证优惠券是否正在使用或者已经被使用
            if (couponSettlementMapper.selectOne(queryWrapper) != null) {
                throw new ClientException("请检查优惠券是否已使用");
            }

            UserCouponDO userCouponDO = userCouponMapper.selectOne(Wrappers.lambdaQuery(UserCouponDO.class)
                    .eq(UserCouponDO::getId, requestParam.getCouponId())
                    .eq(UserCouponDO::getUserId, Long.parseLong(UserContext.getUserId())));

            // 验证用户优惠券状态和有效性
            if (Objects.isNull(userCouponDO)) {
                throw new ClientException("优惠券不存在");
            }
            if (userCouponDO.getValidEndTime().before(new Date())) {
                throw new ClientException("优惠券已过期");
            }
            if (userCouponDO.getStatus() != 0) {
                throw new ClientException("优惠券使用状态异常");
            }

            // 获取优惠券模板和消费规则
            CouponTemplateQueryRespDTO couponTemplate = couponTemplateService.findCouponTemplate(
                    new CouponTemplateQueryReqDTO(requestParam.getShopNumber(), String.valueOf(userCouponDO.getCouponTemplateId())));
            JSONObject consumeRule = JSONObject.parseObject(couponTemplate.getConsumeRule());

            // 计算折扣金额
            BigDecimal discountAmount;

            // 商品专属优惠券
            if (couponTemplate.getTarget().equals(0)) {
                // 获取第一个匹配的商品
                Optional<CouponCreatePaymentGoodsReqDTO> matchedGoods = requestParam.getGoodsList().stream()
                        .filter(each -> Objects.equals(couponTemplate.getGoods(), each.getGoodsNumber()))
                        .findFirst();

                if (matchedGoods.isEmpty()) {
                    throw new ClientException("商品信息与优惠券模板不符");
                }

                // 验证折扣金额
                CouponCreatePaymentGoodsReqDTO paymentGoods = matchedGoods.get();
                BigDecimal maximumDiscountAmount = consumeRule.getBigDecimal("maximumDiscountAmount");
                if (!paymentGoods.getGoodsAmount().subtract(maximumDiscountAmount).equals(paymentGoods.getGoodsPayableAmount())) {
                    throw new ClientException("商品折扣后金额异常");
                }

                discountAmount = maximumDiscountAmount;
            } else { // 店铺专属
                // 检查店铺编号（如果是店铺券）
                if (couponTemplate.getSource() == 0 && !requestParam.getShopNumber().equals(couponTemplate.getShopNumber())) {
                    throw new ClientException("店铺编号不一致");
                }

                BigDecimal termsOfUse = consumeRule.getBigDecimal("termsOfUse");
                if (requestParam.getOrderAmount().compareTo(termsOfUse) < 0) {
                    throw new ClientException("订单金额未满足使用条件");
                }

                BigDecimal maximumDiscountAmount = consumeRule.getBigDecimal("maximumDiscountAmount");

                switch (couponTemplate.getType()) {
                    case 0: // 立减券
                        discountAmount = maximumDiscountAmount;
                        break;
                    case 1: // 满减券
                        discountAmount = maximumDiscountAmount;
                        break;
                    case 2: // 折扣券
                        BigDecimal discountRate = consumeRule.getBigDecimal("discountRate");
                        discountAmount = requestParam.getOrderAmount().multiply(discountRate);
                        break;
                    default:
                        throw new ClientException("无效的优惠券类型");
                }
            }

            // 计算折扣后金额并进行检查
            BigDecimal actualPayableAmount = requestParam.getOrderAmount().subtract(discountAmount);
            if (actualPayableAmount.compareTo(requestParam.getPayableAmount()) != 0) {
                throw new ClientException("折扣后金额不一致");
            }

            CouponSettlementDO couponSettlementDO = CouponSettlementDO.builder()
                    .orderId(requestParam.getOrderId())
                    .couponId(requestParam.getCouponId())
                    .userId(Long.parseLong(UserContext.getUserId()))
                    .status(0)
                    .build();
            couponSettlementMapper.insert(couponSettlementDO);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void processPayment(CouponProcessPaymentReqDTO requestParam) {

    }

    @Override
    public void processRefund(CouponProcessRefundReqDTO requestParam) {

    }
}
