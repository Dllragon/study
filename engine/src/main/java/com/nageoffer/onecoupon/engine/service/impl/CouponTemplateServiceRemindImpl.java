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

package com.nageoffer.onecoupon.engine.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.onecoupon.engine.dao.entity.CouponTemplateDO;
import com.nageoffer.onecoupon.engine.dao.entity.CouponTemplateRemindDO;
import com.nageoffer.onecoupon.engine.dao.mapper.CouponTemplateRemindMapper;
import com.nageoffer.onecoupon.engine.dto.req.CouponTemplateRemindCancelReqDTO;
import com.nageoffer.onecoupon.engine.dto.req.CouponTemplateRemindCreateReqDTO;
import com.nageoffer.onecoupon.engine.dto.req.CouponTemplateRemindQueryReqDTO;
import com.nageoffer.onecoupon.engine.dto.resp.CouponTemplateRemindQueryRespDTO;
import com.nageoffer.onecoupon.engine.service.CouponTemplateRemindService;
import com.nageoffer.onecoupon.engine.service.CouponTemplateService;
import com.nageoffer.onecoupon.engine.toolkit.CouponTemplateRemindUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 优惠券预约提醒业务逻辑实现层
 * <p>
 * 作者：优雅
 * 加项目群：早加入就是优势！500人内部项目群，分享的知识总有你需要的 <a href="https://t.zsxq.com/cw7b9" />
 * 开发时间：2024-07-16
 */
@Service
@RequiredArgsConstructor
public class CouponTemplateServiceRemindImpl extends ServiceImpl<CouponTemplateRemindMapper, CouponTemplateRemindDO> implements CouponTemplateRemindService {

    private final CouponTemplateRemindMapper couponTemplateRemindMapper;
    private final CouponTemplateService couponTemplateService;
    private final RBloomFilter<String> couponTemplateCancelRemindBloomFilter;

    @Override
    public boolean createCouponRemind(CouponTemplateRemindCreateReqDTO requestParam) {
        return false;
    }

    @Override
    public List<CouponTemplateRemindQueryRespDTO> listCouponRemind(CouponTemplateRemindQueryReqDTO requestParam) {
        LambdaQueryWrapper<CouponTemplateRemindDO> queryWrapper = Wrappers.lambdaQuery(CouponTemplateRemindDO.class)
                .eq(CouponTemplateRemindDO::getUserId, requestParam.getUserId());
        // 查出用户预约的信息
        List<CouponTemplateRemindDO> couponTemplateRemindDOS = couponTemplateRemindMapper.selectList(queryWrapper);
        if (couponTemplateRemindDOS == null || couponTemplateRemindDOS.isEmpty())
            return new ArrayList<>();
        // 根据优惠券id查询优惠券信息
        List<Long> couponIds = couponTemplateRemindDOS.stream().map(CouponTemplateRemindDO::getCouponTemplateId).toList();
        List<Long> shopNumbers = couponTemplateRemindDOS.stream().map(CouponTemplateRemindDO::getShopNumber).toList();
        List<CouponTemplateDO> couponTemplateDOS = couponTemplateService.listCouponTemplateById(couponIds, shopNumbers);
        List<CouponTemplateRemindQueryRespDTO> resp = BeanUtil.copyToList(couponTemplateDOS, CouponTemplateRemindQueryRespDTO.class);
        // 填充响应结果的其它信息
        resp.forEach(each -> {
            // 找到当前优惠券对应的预约提醒信息
            couponTemplateRemindDOS.stream().filter(i -> i.getCouponTemplateId().equals(each.getId())).findFirst().ifPresent(i -> {
                // 解析并填充预约提醒信息
                CouponTemplateRemindUtil.fillRemindInformation(each, i.getInformation());
            });
        });
        return resp;
    }

    @Override
    public boolean cancelCouponRemind(CouponTemplateRemindCancelReqDTO requestParam) {
        LambdaQueryWrapper<CouponTemplateRemindDO> queryWrapper = Wrappers.lambdaQuery(CouponTemplateRemindDO.class)
                .eq(CouponTemplateRemindDO::getUserId, requestParam.getUserId())
                .eq(CouponTemplateRemindDO::getCouponTemplateId, requestParam.getCouponTemplateId());
        CouponTemplateRemindDO couponTemplateRemindDO = couponTemplateRemindMapper.selectOne(queryWrapper);
        // 计算bitMap信息
        Long bitMap = CouponTemplateRemindUtil.calculateBitMap(requestParam.getRemindTime(), requestParam.getType());
        bitMap ^= couponTemplateRemindDO.getInformation();
        if (bitMap.equals(0L)) {
            // 如果新bitmap信息是0，说明已经没有预约提醒了，可以直接删除
            couponTemplateRemindMapper.delete(queryWrapper);
        } else {
            // 虽然删除了这个预约提醒，但还有其它提醒，更新数据库
            couponTemplateRemindDO.setInformation(bitMap);
            couponTemplateRemindMapper.updateById(couponTemplateRemindDO);
        }
        // 取消提醒这个信息添加到布隆过滤器中
        add2BloomFilter(requestParam.getCouponTemplateId(), requestParam.getUserId(), requestParam.getRemindTime(), requestParam.getType());
        return true;
    }

    private void add2BloomFilter(String couponTemplateId, String userId, Integer remindTime, Integer type) {
        couponTemplateCancelRemindBloomFilter.add(String.valueOf(Objects.hash(couponTemplateId, userId, remindTime, type)));
    }
}
