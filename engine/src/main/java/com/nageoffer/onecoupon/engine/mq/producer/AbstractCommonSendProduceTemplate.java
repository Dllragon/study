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

package com.nageoffer.onecoupon.engine.mq.producer;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.nageoffer.onecoupon.engine.mq.base.BaseSendExtendDTO;
import com.nageoffer.onecoupon.engine.mq.base.MessageWrapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;

import java.util.UUID;

/**
 * RocketMQ 抽象公共发送消息组件
 * <p>
 * 作者：马丁
 * 加项目群：早加入就是优势！500人内部项目群，分享的知识总有你需要的 <a href="https://t.zsxq.com/cw7b9" />
 * 开发时间：2024-07-18
 */
@RequiredArgsConstructor
@Slf4j(topic = "CommonSendProduceTemplate")
public abstract class AbstractCommonSendProduceTemplate<T> {

    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 构建消息发送事件基础扩充属性实体
     *
     * @param messageSendEvent 消息发送事件
     * @return 扩充属性实体
     */
    protected abstract BaseSendExtendDTO buildBaseSendExtendParam(T messageSendEvent);

    /**
     * 构建消息基本参数，请求头、Keys...
     *
     * @param messageSendEvent 消息发送事件
     * @param requestParam     扩充属性实体
     * @return 消息基本参数
     */
    protected abstract Message<?> buildMessage(T messageSendEvent, BaseSendExtendDTO requestParam);

    /**
     * 消息事件通用发送
     *
     * @param messageSendEvent 消息发送事件
     * @return 消息发送返回结果
     */
    public SendResult sendMessage(T messageSendEvent) {
        return sendMessage(messageSendEvent, null);
    }

    /**
     * 消息事件通用发送
     *
     * @param messageSendEvent 消息发送事件
     * @param deliverTimeStamp 任意延迟时间
     * @return 消息发送返回结果
     */
    @SneakyThrows
    public SendResult sendMessage(T messageSendEvent, Long deliverTimeStamp) {
        BaseSendExtendDTO baseSendExtendDTO = buildBaseSendExtendParam(messageSendEvent);
        SendResult sendResult;
        try {
            if (deliverTimeStamp == null) {
                StringBuilder destinationBuilder = StrUtil.builder().append(baseSendExtendDTO.getTopic());
                if (StrUtil.isNotBlank(baseSendExtendDTO.getTag())) {
                    destinationBuilder.append(":").append(baseSendExtendDTO.getTag());
                }

                sendResult = rocketMQTemplate.syncSend(
                        destinationBuilder.toString(),
                        buildMessage(messageSendEvent, baseSendExtendDTO),
                        baseSendExtendDTO.getSentTimeout()
                );
            } else {
                byte[] bytes = JSON.toJSONBytes(new MessageWrapper(baseSendExtendDTO.getKeys(), messageSendEvent));
                org.apache.rocketmq.common.message.Message message = new org.apache.rocketmq.common.message.Message(baseSendExtendDTO.getTopic(), bytes);
                if (StrUtil.isNotBlank(baseSendExtendDTO.getTag())) {
                    message.setTags(baseSendExtendDTO.getTag());
                }
                String keys = StrUtil.isEmpty(baseSendExtendDTO.getKeys()) ? UUID.randomUUID().toString() : baseSendExtendDTO.getKeys();
                message.setKeys(keys);

                message.setDeliverTimeMs(deliverTimeStamp); // 设置消息的送达时间，毫秒级 Unix 时间戳
                DefaultMQProducer defaultMQProducer = rocketMQTemplate.getProducer();

                sendResult = defaultMQProducer.send(message);
            }
            log.info("[生产者] {} - 发送结果：{}，消息ID：{}，消息Keys：{}", baseSendExtendDTO.getEventName(), sendResult.getSendStatus(), sendResult.getMsgId(), baseSendExtendDTO.getKeys());
        } catch (Throwable ex) {
            log.error("[生产者] {} - 消息发送失败，消息体：{}", baseSendExtendDTO.getEventName(), JSON.toJSONString(messageSendEvent), ex);
            throw ex;
        }
        return sendResult;
    }
}
