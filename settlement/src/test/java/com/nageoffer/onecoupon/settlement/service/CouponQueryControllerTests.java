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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nageoffer.onecoupon.settlement.controller.CouponQueryController;
import com.nageoffer.onecoupon.settlement.dto.req.QueryCouponsReqDTO;
import com.nageoffer.onecoupon.settlement.dto.resp.QueryCouponsRespDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CouponQueryControllerTests {

    private MockMvc mockMvc;

    @Mock
    private CouponQueryService couponQueryService;

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
    }

    private Date parseDate(String date) throws ParseException {
        return DATE_FORMAT.parse(date);
    }

    @Test
    public void testPageQueryAvailableCoupons() throws Exception {
        List<QueryCouponsRespDTO> availableCoupons = new ArrayList<>();
        availableCoupons.add(QueryCouponsRespDTO.builder()
                .couponTemplateId(1810966706881941507L)
                .receiveTime(parseDate("2024-07-15 16:46:05"))
                .validStartTime(parseDate("2024-07-20 16:46:05"))
                .validEndTime(parseDate("2024-07-25 17:18:04"))
                .status(0)
                .build());

        IPage<QueryCouponsRespDTO> page = new Page<>();
        page.setRecords(availableCoupons);
        page.setTotal(1);  // 设置 total 为 1
        page.setPages(1);  // 设置 pages 为 1
        when(couponQueryService.pageQueryAvailableCoupons(any(QueryCouponsReqDTO.class))).thenReturn(page);

        MvcResult result = mockMvc.perform(get("/api/settlement/coupon-query/page/available")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("userId", "1812833908648099852"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        String actualJson = result.getResponse().getContentAsString();
        System.out.println("Actual JSON Response: " + actualJson);

        String expectedJson = """
        {
            "code": "0",
            "message": null,
            "data": {
                "records": [
                    {
                        "couponTemplateId": 1810966706881941507,
                        "couponName": null,
                        "receiveTime": "2024-07-15 16:46:05",
                        "validStartTime": "2024-07-20 16:46:05",
                        "validEndTime": "2024-07-25 17:18:04",
                        "status": 0
                    }
                ],
                "total": 1,
                "size": 10,
                "current": 1,
                "pages": 1
            },
            "requestId": null,
            "success": true,
            "fail": false
        }
        """;

        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }
}
