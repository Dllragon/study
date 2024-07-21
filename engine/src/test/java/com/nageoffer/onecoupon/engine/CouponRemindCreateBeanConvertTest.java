package com.nageoffer.onecoupon.engine;

import cn.hutool.core.bean.BeanUtil;
import com.nageoffer.onecoupon.engine.dao.entity.CouponTemplateRemindDO;
import com.nageoffer.onecoupon.engine.dto.req.CouponTemplateRemindCreateReqDTO;
import com.nageoffer.onecoupon.engine.mq.event.CouponRemindEvent;

import java.util.Date;

public class CouponRemindCreateBeanConvertTest {
    public static void main(String[] args) {
        CouponTemplateRemindCreateReqDTO dto = new CouponTemplateRemindCreateReqDTO();
        dto.setCouponTemplateId("111");
        dto.setName("aaa");
        dto.setShopNumber("222");
        dto.setUserId("333");
        dto.setContact("123@qq.com");
        dto.setType(1);
        dto.setRemindTime(10);
        dto.setStartTime(new Date());

        CouponTemplateRemindDO couponTemplateRemindDO = BeanUtil.toBean(dto, CouponTemplateRemindDO.class);
        System.out.println(couponTemplateRemindDO.toString());

        CouponRemindEvent couponRemindEvent = BeanUtil.toBean(dto, CouponRemindEvent.class);
        System.out.println(couponRemindEvent.toString());
    }
}
