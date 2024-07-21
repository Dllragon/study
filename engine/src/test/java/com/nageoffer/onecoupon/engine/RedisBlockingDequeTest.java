package com.nageoffer.onecoupon.engine;

import cn.hutool.json.JSONUtil;
import com.nageoffer.onecoupon.engine.service.handler.remind.dto.RemindCouponTemplateDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.nageoffer.onecoupon.engine.common.constant.EngineRedisConstant.COUPON_REMIND_CHECK_KEY;

@SpringBootTest
public class RedisBlockingDequeTest {
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test() throws InterruptedException {
        RemindCouponTemplateDTO remindDTO = new RemindCouponTemplateDTO();
        remindDTO.setCouponTemplateId("111");
        remindDTO.setName("shop");
        remindDTO.setShopNumber("1111");
        remindDTO.setUserId("888");
        remindDTO.setContact("123@qq.com");
        remindDTO.setType(0);
        remindDTO.setRemindTime(5);
        remindDTO.setStartTime(new Date());

        RBlockingDeque<String> blockingDeque = redissonClient.getBlockingDeque("COUPON_REMIND_QUEUE");
        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        String key = String.format(COUPON_REMIND_CHECK_KEY, remindDTO.getUserId(), remindDTO.getCouponTemplateId(), remindDTO.getRemindTime(), remindDTO.getType());
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(remindDTO));
        delayedQueue.offer(key, 10, TimeUnit.SECONDS);
    }

}
