package com.nageoffer.onecoupon.settlement.service;

import com.nageoffer.onecoupon.settlement.dao.entity.OrderDO;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 作者：Henry Wan
 * 加项目群：早加入就是优势！500人内部项目群，分享的知识总有你需要的 <a href="https://t.zsxq.com/cw7b9" />
 * 开发时间：2024/7/21  14:10
 */


public interface OrderService {
    OrderDO createUserOrder(Long userId, String shopNumber, BigDecimal totalAmount, BigDecimal payableAmount, Long couponId, BigDecimal couponAmount);
    List<OrderDO> getUserOrder(Long userId);
    OrderDO getOrderDetail(Long orderId);
}
