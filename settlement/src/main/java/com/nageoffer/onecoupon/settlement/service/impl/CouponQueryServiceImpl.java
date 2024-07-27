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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.onecoupon.settlement.dao.entity.UserCouponDO;
import com.nageoffer.onecoupon.settlement.dao.mapper.UserCouponMapper;
import com.nageoffer.onecoupon.settlement.dto.req.QueryCouponsReqDTO;
import com.nageoffer.onecoupon.settlement.dto.resp.QueryCouponsRespDTO;
import com.nageoffer.onecoupon.settlement.service.CouponQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

/**
 *
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

    /**
     * 查询用户可用的优惠券列表
     * @param  requestParam
     * @return 可用的优惠券列表
     */
    @Override
    public IPage<QueryCouponsRespDTO> pageQueryAvailableCoupons(QueryCouponsReqDTO requestParam) {
        // 分页对象
        Page<UserCouponDO> page = new Page<>(requestParam.getPageNum(), requestParam.getPageSize());

        // TODO 可用使用ID排序，比时间效率更高
        // 查询条件
        QueryWrapper<UserCouponDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", requestParam.getUserId())
                .eq("status", 0)  // 状态为0，表示未使用的优惠券
                .orderByDesc("receive_time");

        // 执行分页查询
        IPage<UserCouponDO> couponPage = userCouponMapper.selectPage(page, queryWrapper);

        // 转换成响应DTO
        IPage<QueryCouponsRespDTO> result = couponPage.convert(userCoupon -> {
            return QueryCouponsRespDTO.builder()
                    .couponTemplateId(userCoupon.getCouponTemplateId())
                    .receiveTime(userCoupon.getReceiveTime())
                    .validStartTime(userCoupon.getValidStartTime())
                    .validEndTime(userCoupon.getValidEndTime())
                    .status(userCoupon.getStatus())
                    .build();
        });

        return result;
    }

    /**
     * 查询用户不可用的优惠券列表
     * @param requestParam
     * @return 不可用的优惠券列表
     */
    @Override
    public IPage<QueryCouponsRespDTO> pageQueryUnavailableCoupons(QueryCouponsReqDTO requestParam) {
        // 分页对象
        Page<UserCouponDO> page = new Page<>(requestParam.getPageNum(), requestParam.getPageSize());

        // 查询条件
        QueryWrapper<UserCouponDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", requestParam.getUserId())
                .ne("status", 0)  // 状态不为0，表示不可用的优惠券
                .orderByDesc("receive_time");

        // 执行分页查询
        IPage<UserCouponDO> couponPage = userCouponMapper.selectPage(page, queryWrapper);

        // 转换成响应DTO
        IPage<QueryCouponsRespDTO> result = couponPage.convert(userCoupon -> QueryCouponsRespDTO.builder()
                .couponTemplateId(userCoupon.getCouponTemplateId())
                .receiveTime(userCoupon.getReceiveTime())
                .validStartTime(userCoupon.getValidStartTime())
                .validEndTime(userCoupon.getValidEndTime())
                .status(userCoupon.getStatus())
                .build());

        return result;
    }
}