package com.nageoffer.onecoupon.settlement.service;

import com.nageoffer.onecoupon.settlement.dao.entity.PaymentDO;
import com.nageoffer.onecoupon.settlement.gateway.PaymentGateway;

import java.math.BigDecimal;

/**
 * <p>
 * 作者：Henry Wan
 * 加项目群：早加入就是优势！500人内部项目群，分享的知识总有你需要的 <a href="https://t.zsxq.com/cw7b9" />
 * 开发时间：2024/7/21  14:29
 */


public interface PaymentService {
    PaymentDO createPaymentRecord(Long orderId, Long userId, BigDecimal paymentAmount, String paymentMethod);
    boolean processPayment(Long paymentId, PaymentGateway paymentGateway);
}
