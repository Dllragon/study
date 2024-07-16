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

package com.nageoffer.onecoupon.distribution.mq.consumer;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nageoffer.onecoupon.distribution.common.constant.DistributionRedisConstant;
import com.nageoffer.onecoupon.distribution.common.constant.DistributionRocketMQConstant;
import com.nageoffer.onecoupon.distribution.common.enums.CouponSourceEnum;
import com.nageoffer.onecoupon.distribution.common.enums.CouponStatusEnum;
import com.nageoffer.onecoupon.distribution.dao.entity.UserCouponDO;
import com.nageoffer.onecoupon.distribution.dao.mapper.UserCouponMapper;
import com.nageoffer.onecoupon.distribution.mq.base.MessageWrapper;
import com.nageoffer.onecoupon.distribution.mq.event.CouponTemplateExecuteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.BatchExecutorException;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 优惠券执行分发到用户消费者
 * <p>
 * 作者：马丁
 * 加项目群：早加入就是优势！500人内部沟通群，分享的知识总有你需要的 <a href="https://t.zsxq.com/cw7b9" />
 * 开发时间：2024-07-14
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = DistributionRocketMQConstant.TEMPLATE_EXECUTE_DISTRIBUTION_TOPIC_KEY,
        consumerGroup = DistributionRocketMQConstant.TEMPLATE_EXECUTE_DISTRIBUTION_CG_KEY
)
@Slf4j(topic = "CouponExecuteDistributionConsumer")
public class CouponExecuteDistributionConsumer implements RocketMQListener<MessageWrapper<CouponTemplateExecuteEvent>> {

    private final UserCouponMapper userCouponMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private final static int BATCH_USER_COUPON_SIZE = 5000;

    @Override
    public void onMessage(MessageWrapper<CouponTemplateExecuteEvent> messageWrapper) {
        // 开头打印日志，平常可 Debug 看任务参数，线上可报平安（比如消息是否消费，重新投递时获取参数等）
        log.info("[消费者] 优惠券分发到用户账号 - 执行消费逻辑，消息体：{}", JSON.toJSONString(messageWrapper));

        CouponTemplateExecuteEvent event = messageWrapper.getMessage();
        String couponTemplateId = event.getCouponTemplateId();

        // 当保存用户优惠券集合达到批量保存数量或分发任务结束标识为 TRUE
        if (event.getBatchUserSetSize() >= BATCH_USER_COUPON_SIZE || event.getDistributionEndFlag()) {
            // 获取保存在 Redis 中的用户临时存储结果，弹出后数据即删除，如果 batchUserSetKey 中没有数据将会被自动删除
            // 为什么 BATCH_USER_COUPON_SIZE << 1 而不是直接用 BATCH_USER_COUPON_SIZE？同上面说的，考虑宕机结果。为什么不 +1 或者 +2，是因为宕机多少次不好说，不要相信极端情况，只能粗暴 *2
            String batchUserSetKey = String.format(DistributionRedisConstant.TEMPLATE_TASK_EXECUTE_BATCH_USER_KEY, event.getCouponTaskId());
            List<String> batchUserIds = stringRedisTemplate.opsForSet().pop(batchUserSetKey, BATCH_USER_COUPON_SIZE << 1);
            if (CollUtil.isEmpty(batchUserIds)) {
                return;
            }

            // 因为 batchUserIds 数据较多，ArrayList 会进行数次扩容，为了避免额外性能消耗，直接初始化 batchUserIds 大小的数组
            List<UserCouponDO> userCouponDOList = new ArrayList<>(batchUserIds.size());
            Date now = new Date();

            // 构建 userCouponDOList 用户优惠券批量数组
            for (String each : batchUserIds) {
                UserCouponDO userCouponDO = UserCouponDO.builder()
                        .couponTemplateId(Long.parseLong(couponTemplateId))
                        .userId(Long.parseLong(each))
                        .receiveTime(now)
                        .receiveCount(1) // 代表第一次领取该优惠券
                        .validStartTime(now)
                        .validEndTime(JSON.parseObject(event.getCouponTemplateConsumeRule()).getDate("validityPeriod"))
                        .source(CouponSourceEnum.PLATFORM.getType())
                        .status(CouponStatusEnum.EFFECTIVE.getType())
                        .createTime(new Date())
                        .updateTime(new Date())
                        .delFlag(0)
                        .build();
                userCouponDOList.add(userCouponDO);
            }

            // 平台优惠券每个用户限领一次。批量新增用户优惠券记录，底层通过递归方式直到全部新增成功
            batchSaveUserCouponList(Long.parseLong(couponTemplateId), userCouponDOList);
        }
    }

    private void batchSaveUserCouponList(Long couponTemplateId, List<UserCouponDO> userCouponDOList) {
        // MyBatis-Plus 批量执行用户优惠券记录
        try {
            userCouponMapper.insert(userCouponDOList, userCouponDOList.size());
        } catch (Exception ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof BatchExecutorException) {
                LambdaQueryWrapper<UserCouponDO> queryWrapper = Wrappers.lambdaQuery(UserCouponDO.class)
                        .eq(UserCouponDO::getCouponTemplateId, couponTemplateId)
                        .in(UserCouponDO::getUserId, userCouponDOList.stream().map(UserCouponDO::getUserId).toList());
                List<UserCouponDO> existentuserCouponDOList = userCouponMapper.selectList(queryWrapper);
                // 遍历已经存在的集合，获取 userId，并从需要新增的集合中移除匹配的元素
                for (UserCouponDO each : existentuserCouponDOList) {
                    Long userId = each.getUserId();

                    // 使用迭代器遍历需要新增的集合，安全移除元素
                    Iterator<UserCouponDO> iterator = userCouponDOList.iterator();
                    while (iterator.hasNext()) {
                        UserCouponDO item = iterator.next();
                        if (item.getUserId().equals(userId)) {
                            iterator.remove();
                            // TODO 应该添加到 t_coupon_task_fail 并标记错误原因
                        }
                    }
                }

                // 采用递归方式重试，直到不存在重复的记录为止
                if (CollUtil.isNotEmpty(userCouponDOList)) {
                    batchSaveUserCouponList(couponTemplateId, userCouponDOList);
                }
            }
        }
    }
}
