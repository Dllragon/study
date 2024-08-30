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

package com.nageoffer.onecoupon.settlement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nageoffer.onecoupon.settlement.dao.entity.CouponTemplateDO;
import com.nageoffer.onecoupon.settlement.dao.entity.UserCouponDO;
import com.nageoffer.onecoupon.settlement.dao.mapper.CouponTemplateMapper;
import com.nageoffer.onecoupon.settlement.dao.mapper.UserCouponMapper;
import com.nageoffer.onecoupon.settlement.dto.req.QueryCouponsReqDTO;
import com.nageoffer.onecoupon.settlement.dto.resp.QueryCouponsRespDTO;
import com.nageoffer.onecoupon.settlement.service.CouponCalculationService;
import com.nageoffer.onecoupon.settlement.service.CouponQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nageoffer.onecoupon.settlement.toolkit.CouponFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 查询用户可用优惠券列表接口
 * <p>
 * 作者：Henry Wan
 * 加项目群：早加入就是优势！500人内部项目群，分享的知识总有你需要的 <a href="https://t.zsxq.com/cw7b9" />
 * 开发时间：2024-07-25
 */
@Service
@RequiredArgsConstructor
public class CouponQueryServiceImpl implements CouponQueryService {

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private CouponTemplateMapper couponTemplateMapper;

    @Autowired
    private CouponCalculationService couponCalculationService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper; // 用于 JSON 序列化和反序列化
    private final StringRedisTemplate stringRedisTemplate;
    private static final String COUPON_CACHE_KEY_PREFIX = "user:coupons:";

    /**
     * 查询用户可用的优惠券列表，返回 CouponsRespDTO 对象
     *
     * @param requestParam 查询参数
     * @return CompletableFuture<CouponsRespDTO> 包含可用优惠券的分页结果
     */
    @Override
    public CompletableFuture<List<QueryCouponsRespDTO>> queryUserCoupons(QueryCouponsReqDTO requestParam) {
        return CompletableFuture.supplyAsync(() -> {
            // 定义 Redis 操作对象
            ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();

            // 构造缓存键
            String cacheKey = COUPON_CACHE_KEY_PREFIX + requestParam.getUserId() + ":" + requestParam.getShopNumber();

            List<QueryCouponsRespDTO> cachedCoupons = null;

            try {
                // 尝试从缓存获取所有优惠券（获取的是JSON字符串）
                String cachedJson = valueOps.get(cacheKey);
                if (cachedJson != null) {
                    // 将 JSON 字符串反序列化为 List<QueryCouponsRespDTO>
                    cachedCoupons = objectMapper.readValue(cachedJson, new TypeReference<List<QueryCouponsRespDTO>>() {
                    });
                }
            } catch (Exception e) {
                // 记录缓存获取时的异常信息
                System.err.println("Error retrieving from Redis: " + e.getMessage());
                e.printStackTrace();
            }

            // 如果缓存命中，直接返回
            if (cachedCoupons != null) {
                return cachedCoupons;
            }

            // 查询用户所有优惠券
            List<UserCouponDO> allCoupons = queryAllUserCoupons(requestParam);

            // 收集所有的 couponTemplateId
            Set<Long> templateIds = allCoupons.stream().map(UserCouponDO::getCouponTemplateId).collect(Collectors.toSet());

            // 一次性查询所有优惠券模型的信息，降低DB压力
            List<CouponTemplateDO> templates = couponTemplateMapper.selectBatchIds(templateIds);

            // 将券模板信息存入Map，便于后续查找
            Map<Long, CouponTemplateDO> templateMap = templates.stream().collect(Collectors.toMap(CouponTemplateDO::getId, template -> template));

            // 筛选可用优惠券
            List<QueryCouponsRespDTO> availableCoupons = new ArrayList<>();

            for (UserCouponDO coupon : allCoupons) {
                CouponTemplateDO couponTemplate = templateMap.get(coupon.getCouponTemplateId());
                if (couponTemplate != null && isCouponApplicable(couponTemplate, requestParam)) {
                    // 创建具体的优惠券实例
                    CouponTemplateDO couponInstance = CouponFactory.createCoupon(couponTemplate, new HashMap<>());

                    // 计算优惠金额
                    BigDecimal couponAmount = couponCalculationService.calculateDiscount(couponInstance, requestParam.getOrderAmount());

                    // 转换成响应DTO
                    QueryCouponsRespDTO queryCouponsRespDTO = convertToRespDTO(coupon, couponTemplate, couponAmount);
                    queryCouponsRespDTO.setCouponAmount(couponAmount);
                    availableCoupons.add(queryCouponsRespDTO);
                }
            }
            // 与业内标准一致，按最终优惠力度从大到小排序
            availableCoupons.sort(this::compareCouponDiscount);

            try {
                // 将 CouponsRespDTO 对象序列化为 JSON 字符串
                String responseJson = objectMapper.writeValueAsString(availableCoupons);

                valueOps.set(cacheKey, responseJson);
                // 缓存结果并设置失效时间为 1 小时
                // valueOps.set(cacheKey, response, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                // 记录缓存存储时的异常信息
                System.err.println("Error storing to Redis: " + e.getMessage());
                e.printStackTrace();
            }

            return availableCoupons;
        });
    }

    private boolean isCouponApplicable(CouponTemplateDO template, QueryCouponsReqDTO requestParam) {
        // 判断优惠券是否适用于当前订单：店铺匹配或全店通用，且订单金额满足消费规则
        return template.getShopNumber().equals(requestParam.getShopNumber()) || template.getTarget() == 1;
    }

    /**
     * 查询用户所有优惠券
     *
     * @param requestParam 查询参数
     * @return 用户优惠券的结果列表
     */
    private List<UserCouponDO> queryAllUserCoupons(QueryCouponsReqDTO requestParam) {

        // 创建查询条件
        QueryWrapper<UserCouponDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", requestParam.getUserId()).orderByDesc("id");

        // 执行查询
        return userCouponMapper.selectList(queryWrapper);
    }

    /**
     * 转换 UserCouponDO 对象为 QueryCouponsRespDTO
     *
     * @param userCoupon 用户优惠券对象
     * @return 响应DTO
     */
    private QueryCouponsRespDTO convertToRespDTO(UserCouponDO userCoupon, CouponTemplateDO template, BigDecimal couponAmount) {

        return QueryCouponsRespDTO.builder()
                .couponTemplateId(template.getId())
                .couponName(template.getName())
                .couponAmount(couponAmount)
                .applicableGoods(template.getGoods())
                .applicableShop(template.getShopNumber().toString())
                .receiveTime(userCoupon.getReceiveTime())
                .validStartTime(userCoupon.getValidStartTime())
                .validEndTime(userCoupon.getValidEndTime())
                .status(userCoupon.getStatus())
                .build();
    }


    private int compareCouponDiscount(QueryCouponsRespDTO c1, QueryCouponsRespDTO c2) {
        // 比较两个优惠券的优惠力度
        return c2.getCouponAmount().compareTo(c1.getCouponAmount());
    }
}