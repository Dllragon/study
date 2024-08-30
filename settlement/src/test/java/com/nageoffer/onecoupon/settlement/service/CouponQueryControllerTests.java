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

package com.nageoffer.onecoupon.settlement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nageoffer.onecoupon.settlement.controller.CouponQueryController;
import com.nageoffer.onecoupon.settlement.dto.req.QueryCouponsReqDTO;
import com.nageoffer.onecoupon.settlement.dto.resp.QueryCouponsRespDTO;
import com.nageoffer.onecoupon.settlement.handler.AsyncResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CouponQueryControllerTests {

    private MockMvc mockMvc;

    @Mock
    private CouponQueryService couponQueryService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @Mock
    private AsyncResponseHandler asyncResponseHandler;

    @InjectMocks
    private CouponQueryController couponQueryController;

    private static final SimpleDateFormat DATE_FORMAT;

    static {
        DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(couponQueryController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redissonClient.getLock(any(String.class))).thenReturn(lock);
    }

    private Date parseDate(String date) throws ParseException {
        return DATE_FORMAT.parse(date);
    }

    @Test
    public void testPageQueryUserCoupons() throws Exception {
        List<QueryCouponsRespDTO> availableCoupons = new ArrayList<>();

        // 立减券
        availableCoupons.add(QueryCouponsRespDTO.builder()
                .couponTemplateId(1810966706881941507L)
                .couponAmount(BigDecimal.valueOf(10))
                .receiveTime(parseDate("2024-07-15 16:46:05"))
                .validStartTime(parseDate("2024-07-20 16:46:05"))
                .validEndTime(parseDate("2024-07-25 17:18:04"))
                .status(0)
                .build());

        // 满减券
        availableCoupons.add(QueryCouponsRespDTO.builder()
                .couponTemplateId(1810966706881941508L)
                .couponAmount(BigDecimal.valueOf(25))
                .receiveTime(parseDate("2024-07-15 16:46:05"))
                .validStartTime(parseDate("2024-07-20 16:46:05"))
                .validEndTime(parseDate("2024-07-25 17:18:04"))
                .status(0)
                .build());

        // 满减大金额券
        availableCoupons.add(QueryCouponsRespDTO.builder()
                .couponTemplateId(1810966706881941509L)
                .couponAmount(BigDecimal.valueOf(100))
                .receiveTime(parseDate("2024-07-15 16:46:05"))
                .validStartTime(parseDate("2024-07-20 16:46:05"))
                .validEndTime(parseDate("2024-07-25 17:18:04"))
                .status(0)
                .build());

        // 无门槛券
        availableCoupons.add(QueryCouponsRespDTO.builder()
                .couponTemplateId(1810966706881941510L)
                .couponAmount(BigDecimal.valueOf(5))
                .receiveTime(parseDate("2024-07-15 16:46:05"))
                .validStartTime(parseDate("2024-07-20 16:46:05"))
                .validEndTime(parseDate("2024-07-25 17:18:04"))
                .status(0)
                .build());

        // 折扣券（例如 20% 折扣）
        availableCoupons.add(QueryCouponsRespDTO.builder()
                .couponTemplateId(1810966706881941511L)
                .couponAmount(BigDecimal.valueOf(20)) // 折扣券折合金额
                .receiveTime(parseDate("2024-07-15 16:46:05"))
                .validStartTime(parseDate("2024-07-20 16:46:05"))
                .validEndTime(parseDate("2024-07-25 17:18:04"))
                .status(0)
                .build());

        // 按优惠金额降序排序
        availableCoupons.sort(Comparator.comparing(QueryCouponsRespDTO::getCouponAmount).reversed());

        CompletableFuture<List<QueryCouponsRespDTO>> couponsFuture = CompletableFuture.completedFuture(availableCoupons);
        when(couponQueryService.queryUserCoupons(any(QueryCouponsReqDTO.class)))
                .thenReturn(couponsFuture);

        when(asyncResponseHandler.createDeferredResult(couponsFuture))
                .thenCallRealMethod();

        MvcResult mvcResult = mockMvc.perform(get("/api/settlement/coupon-query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("userId", "1812833908648099852")
                        .param("shopNumber", "123456"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNotEmpty());

        String actualJson = mvcResult.getResponse().getContentAsString();

        String expectedJson = """
                {
                    "code": "0",
                    "message": null,
                    "data": [
                        {
                            "couponTemplateId": 1810966706881941509,
                            "couponAmount": 100,
                            "receiveTime": "2024-07-15 16:46:05",
                            "validStartTime": "2024-07-20 16:46:05",
                            "validEndTime": "2024-07-25 17:18:04",
                            "status": 0
                        },
                        {
                            "couponTemplateId": 1810966706881941511,
                            "couponAmount": 20,
                            "receiveTime": "2024-07-15 16:46:05",
                            "validStartTime": "2024-07-20 16:46:05",
                            "validEndTime": "2024-07-25 17:18:04",
                            "status": 0
                        },
                        {
                            "couponTemplateId": 1810966706881941508,
                            "couponAmount": 25,
                            "receiveTime": "2024-07-15 16:46:05",
                            "validStartTime": "2024-07-20 16:46:05",
                            "validEndTime": "2024-07-25 17:18:04",
                            "status": 0
                        },
                        {
                            "couponTemplateId": 1810966706881941507,
                            "couponAmount": 10,
                            "receiveTime": "2024-07-15 16:46:05",
                            "validStartTime": "2024-07-20 16:46:05",
                            "validEndTime": "2024-07-25 17:18:04",
                            "status": 0
                        },
                        {
                            "couponTemplateId": 1810966706881941510,
                            "couponAmount": 5,
                            "receiveTime": "2024-07-15 16:46:05",
                            "validStartTime": "2024-07-20 16:46:05",
                            "validEndTime": "2024-07-25 17:18:04",
                            "status": 0
                        }
                    ],
                    "requestId": null,
                    "success": true,
                    "fail": false
                }
                """;

        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }
}
