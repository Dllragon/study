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

package com.nageoffer.onecoupon.settlement.sharding;

import org.junit.jupiter.api.Test;

/**
 * <p>
 * 作者：Henry Wan
 * 加项目群：早加入就是优势！500人内部项目群，分享的知识总有你需要的 <a href="https://t.zsxq.com/cw7b9" />
 * 开发时间：2024-07-25
 */
public class AutoCreateShardingTableTests {

    private final String settlementTableTemplate = "CREATE TABLE `t_coupon_settlement_%d` (\n" +
            "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "  `shop_number` varchar(64) DEFAULT NULL COMMENT '店铺编号',\n" +
            "  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',\n" +
            "  `coupon_id` bigint(20) DEFAULT NULL COMMENT '优惠券ID',\n" +
            "  `total_amount` decimal(10,2) DEFAULT NULL COMMENT '结算单总金额',\n" +
            "  `payable_amount` decimal(10,2) DEFAULT NULL COMMENT '应付金额',\n" +
            "  `coupon_amount` decimal(10,2) DEFAULT NULL COMMENT '券金额',\n" +
            "  `status` int(11) DEFAULT NULL COMMENT '结算单状态',\n" +
            "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  `update_time` datetime DEFAULT NULL COMMENT '修改时间',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  KEY `idx_userid` (`user_id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券结算单表';";

    @Test
    public void generateSettlementTables() {
        int numberOfDatabases = 2;
        int tablesPerDatabase = 8;

        for (int db = 0; db < numberOfDatabases; db++) {
            System.out.println("-- Database: onecoupon_" + db);
            for (int i = 0; i < tablesPerDatabase; i++) {
                int tableIndex = db * tablesPerDatabase + i;
                System.out.println(String.format(settlementTableTemplate, tableIndex));
                System.out.println();
            }
            System.out.println();
        }
    }



}
