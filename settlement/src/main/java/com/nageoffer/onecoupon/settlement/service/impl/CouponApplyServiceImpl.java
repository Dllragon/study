/**
 * 牛券（oneCoupon）优惠券平台项目
 * <p>
 * 版权所有 (C) [2024-至今] [山东流年网络科技有限公司]
 * <p>
 * 保留所有权利。
 * <p>
 * 1. 定义和解释
 * 本文件（包括其任何修改、更新和衍生内容）是由[山东流年网络科技有限公司]及相关人员开发的。
 * "软件"指的是与本文件相关的任何代码、脚本、文档和相关的资源。
 * <p>
 * 2. 使用许可
 * 本软件的使用、分发和解释均受中华人民共和国法律的管辖。只有在遵守以下条件的前提下，才允许使用和分发本软件：
 * a. 未经[山东流年网络科技有限公司]的明确书面许可，不得对本软件进行修改、复制、分发、出售或出租。
 * b. 任何未授权的复制、分发或修改都将被视为侵犯[山东流年网络科技有限公司]的知识产权。
 * <p>
 * 3. 免责声明
 * 本软件按"原样"提供，没有任何明示或暗示的保证，包括但不限于适销性、特定用途的适用性和非侵权性的保证。
 * 在任何情况下，[山东流年网络科技有限公司]均不对任何直接、间接、偶然、特殊、典型或间接的损害（包括但不限于采购替代商品或服务；使用、数据或利润损失）承担责任。
 * <p>
 * 4. 侵权通知与处理
 * a. 如果[山东流年网络科技有限公司]发现或收到第三方通知，表明存在可能侵犯其知识产权的行为，公司将采取必要的措施以保护其权利。
 * b. 对于任何涉嫌侵犯知识产权的行为，[山东流年网络科技有限公司]可能要求侵权方立即停止侵权行为，并采取补救措施，包括但不限于删除侵权内容、停止侵权产品的分发等。
 * c. 如果侵权行为持续存在或未能得到妥善解决，[山东流年网络科技有限公司]保留采取进一步法律行动的权利，包括但不限于发出警告信、提起民事诉讼或刑事诉讼。
 * <p>
 * 5. 其他条款
 * a. [山东流年网络科技有限公司]保留随时修改这些条款的权利。
 * b. 如果您不同意这些条款，请勿使用本软件。
 * <p>
 * 未经[山东流年网络科技有限公司]的明确书面许可，不得使用此文件的任何部分。
 * <p>
 * 本软件受到[山东流年网络科技有限公司]及其许可人的版权保护。
 */
package com.nageoffer.onecoupon.settlement.service.impl;

import com.nageoffer.onecoupon.settlement.dto.req.ApplyCouponReqDTO;
import com.nageoffer.onecoupon.settlement.dto.resp.ApplyCouponRespDTO;
import com.nageoffer.onecoupon.settlement.dto.resp.QueryCouponsRespDTO;
import com.nageoffer.onecoupon.settlement.service.CouponApplyService;
import com.nageoffer.onecoupon.settlement.service.CouponCalculationService;
import com.nageoffer.onecoupon.settlement.service.CouponQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 优惠券应用服务实现类
 *
 * <p>
 * 作者：Henry Wan
 * 开发时间：2024-08-29
 */
@Service
@RequiredArgsConstructor
public class CouponApplyServiceImpl implements CouponApplyService {

    private final CouponQueryService couponQueryService;
    private final CouponCalculationService couponCalculationService;

    @Override
    public ApplyCouponRespDTO applySelectedCoupon(ApplyCouponReqDTO applyCouponReqDTO, Long selectedCouponId) {

        // 获取用户所有可用优惠券
        List<QueryCouponsRespDTO> availableCoupons = couponQueryService.queryUserCoupons(applyCouponReqDTO.toQueryCouponsReqDTO()).join();

        // 查找用户选择的优惠券
        Optional<QueryCouponsRespDTO> selectedCouponOpt = availableCoupons.stream()
                .filter(coupon -> coupon.getCouponTemplateId().equals(selectedCouponId))
                .findFirst();

        if (selectedCouponOpt.isPresent()) {
            QueryCouponsRespDTO selectedCoupon = selectedCouponOpt.get();

            // 获取优惠金额
            BigDecimal discountAmount = selectedCoupon.getCouponAmount();
            // 计算折后金额
            BigDecimal finalAmount = applyCouponReqDTO.getOrderAmount().subtract(discountAmount);

            return ApplyCouponRespDTO.builder()
                    .orderId(applyCouponReqDTO.getOrderId())
                    .originalAmount(applyCouponReqDTO.getOrderAmount())
                    .finalAmount(finalAmount)
                    .appliedCouponId(selectedCoupon.getCouponTemplateId())
                    .build();
        } else {
            // 未找到匹配的优惠券，返回原金额
            return ApplyCouponRespDTO.builder()
                    .orderId(applyCouponReqDTO.getOrderId())
                    .originalAmount(applyCouponReqDTO.getOrderAmount())
                    .finalAmount(applyCouponReqDTO.getOrderAmount())
                    .appliedCouponId(null)
                    .build();
        }
    }
}
