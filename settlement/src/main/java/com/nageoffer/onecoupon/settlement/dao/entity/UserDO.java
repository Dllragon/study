package com.nageoffer.onecoupon.settlement.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 结算模块用户数据库持久层实体, Mock User 不存DB
 * <p>
 * 作者：Henry Wan
 * 加项目群：早加入就是优势！500人内部项目群，分享的知识总有你需要的 <a href="https://t.zsxq.com/cw7b9" />
 * 开发时间：2024/7/20  2:08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_user")
public class UserDO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户类型
     * 0 : 普通用户
     * 1 : 商家
     */
    private Integer userType;

    /**
     * 店铺编号 (仅商家用户需要)
     */
    private String shopNumber;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 删除标识 0：未删除 1：已删除
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;
}