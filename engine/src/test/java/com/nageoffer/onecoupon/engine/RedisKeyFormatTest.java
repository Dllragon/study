package com.nageoffer.onecoupon.engine;

import static com.nageoffer.onecoupon.engine.common.constant.EngineRedisConstant.COUPON_REMIND_CHECK_KEY;

public class RedisKeyFormatTest {
    public static void main(String[] args) {
        String format = String.format(COUPON_REMIND_CHECK_KEY, "111", "222", 3, 4);
        System.out.println(format);
    }
}
