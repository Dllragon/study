## ©️版权告示

为了保障星球用户权益，牛券 `oneCoupon` 不再实行开源策略，而是通过邀请星球用户进入私有项目进行学习。

**严禁未经本项目原作者明确书面授权擅自分享至 GitHub、Gitee 等任何开放平台。违者将面临版权法律追究。**

- 知识星球 [《侵权责任法》、《著作权法》和《信息网络传播权保护条例》](https://support.zsxq.com/guidance.html)。
- 项目版权[《中华人民共和国著作权法实施条例》](https://gitcode.net/nageoffer/onecoupon/-/blob/main/copyright/%E4%B8%AD%E5%8D%8E%E4%BA%BA%E6%B0%91%E5%85%B1%E5%92%8C%E5%9B%BD%E8%91%97%E4%BD%9C%E6%9D%83%E6%B3%95%E5%AE%9E%E6%96%BD%E6%9D%A1%E4%BE%8B.pdf)。

## 什么是牛券 oneCoupon？

牛券是一款高性能优惠券系统，与其他网上优惠券系统不同，**牛券能够承受近十万次查询和分发请求的高并发压力**。

项目旨在帮助校招和社招的同学掌握足够的亮点，为获得理想的 offer 助力。此次代码实现非常优雅，甚至细致到参数定义都蕴含深意，值得大家学习借鉴。

其中的一些亮点部分已重点标记，大家可根据实际情况学习即可。该图会持续更新。

![](https://oss.open8gu.com/image-20240723011449928.png)

## 业务架构

从优惠券的业务属性上来说，每个平台都离不开优惠券，优惠券又分为平台券和店铺券，在此之上券类型分为折扣券、满减券以及立减券。当然，优惠券的领取和使用同样具有限制，详情如下：

![](https://oss.open8gu.com/image-20240911201453992.png)

下方的业务架构图全面描述了项目的服务集合、组件库列表和基础设置层等要素，有助于用户快速了解牛券平台的顶层设计和业务细节，从零到一进行构建。

能力层部分虽然涵盖了多个问题解决方案，但与整个项目的亮点相比，这部分内容并非全面展示。随着学习的深入，大家将能够更清楚地理解和掌握这些内容，并从中获得更多的实际应用能力。

![](https://oss.open8gu.com/image-20240822225009816.png)

## 技术架构

我们选择了基于 Spring Boot 3 和 JDK17 进行底层建设，同时组件库的版本大多也是最新的。这样做既能享受新技术带来的性能提升，也能体验到新特性带来的惊喜。

如果用一张图来概括牛券的技术架构，其展现形态如下图所示。

![](https://oss.open8gu.com/image-20240722104707368.png)

技术架构涵盖了 SpringBoot 3、SpringCloudAlibaba、Nacos、Sentinel、Skywalking、RocketMQ 5.x、ElasticSearch、Redis、MySQL、EasyExcel、XXL-Job、Redisson 等技术。

框架技术和版本号关系如下表格所示。

|      | 技术                | 名称               | 版本           | 官网                                                         |
| ---- | ------------------- | ------------------ | -------------- | ------------------------------------------------------------ |
| 1    | Spring Boot         | 基础框架           | 3.0.7          | [https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot) |
| 2    | SpringCloud Alibaba | 分布式框架         | 2022.0.0.0-RC2 | [https://github.com/alibaba/spring-cloud-alibaba](https://github.com/alibaba/spring-cloud-alibaba) |
| 3    | SpringCloud Gateway | 网关框架           | 2022.0.3       | [https://spring.io/projects/spring-cloud-gateway](https://spring.io/projects/spring-cloud-gateway) |
| 4    | MyBatis-Plus        | 持久层框架         | 3.5.7          | [https://baomidou.com](https://baomidou.com)                 |
| 5    | MySQL               | OLTP 关系型数据库  | 5.7.36         | https://www.mysql.com/cn                                     |
| 6    | Redis               | 分布式缓存数据库   | Latest         | [https://redis.io](https://redis.io)                         |
| 7    | RocketMQ            | 消息队列           | 2.3.0          | [https://rocketmq.apache.org](https://rocketmq.apache.org)   |
| 8    | ShardingSphere      | 数据库生态系统     | 5.3.2          | [https://shardingsphere.apache.org](https://shardingsphere.apache.org) |
| 9    | FastJson2           | JSON 序列化工具    | 2.0.36         | [https://github.com/alibaba/fastjson2](https://github.com/alibaba/fastjson2) |
| 10   | Canal               | BinLog 订阅组件    | 1.1.6          | [https://github.com/alibaba/canal](https://github.com/alibaba/canal) |
| 11   | HuTool              | 小而全的工具集项目 | 5.8.27         | [https://hutool.cn](https://hutool.cn)                       |
| 12   | Maven               | 项目构建管理       | 3.9.1          | [http://maven.apache.org](http://maven.apache.org)           |
| 13   | Redisson            | Redis Java 客户端  | 3.27.2         | [https://redisson.org](https://redisson.org/)                |
| 14   | Sentinel            | 流控防护框架       | 1.8.6          | [https://github.com/alibaba/Sentinel](https://github.com/alibaba/Sentinel) |
| 15   | XXL-Job             | 分布式定时任务框架 | 2.4.1          | [http://www.xuxueli.com/xxl-job](http://www.xuxueli.com/xxl-job) |
| 16   | BizLog              | 操作日志工具       | 3.0.6          | https://github.com/mouzt/mzt-biz-log                         |
| 17   | EasyExcel           | Excel 处理工具     | 4.0.1          | https://easyexcel.opensource.alibaba.com                     |
| 18   | ElasticSearch       | 分布式搜索引擎     | TODO           | https://github.com/elastic/elasticsearch                     |

**学习项目需要什么前置技术？**

虽然上面的技术点用到的很多，但是很多只是知道框架是做什么，会使用 API 即可满足开发条件，不需要深入原理。所以看着技术点比较多，但是上手必须的框架技术却很少。

> 其实强依赖的只有 `分布式缓存 Redis`、`消息队列 RocketMQ`，其他大家都不需要刻意学习，课程讲的过程当中会说明。

从项目学习的角度上，**大家需要至少做过一个 SpringBoot 项目，比如点评、外卖或者 SaaS 短链接**。掌握了基本开发流程，就可以上手开始项目。

另外星球提供了 Redis 和 RocketMQ 的云中间件服务，大家可以直接使用。你只需在本地启动一个 5.7.x 版本的 MySQL，就可以开始项目学习了！

## 加群沟通

为了更加便捷地沟通和分享，我创建了一个专属的微信会员群。群内技术氛围浓厚，许多同学在这里交流技术和面试经验，大家共同成长。

扫描我的下方二维码，在微信中扫描添加好友。

![](https://oss.open8gu.com/1_990064918_171_84_3_716500817_c4659af930df3a2532d02b8fcc0f0cbe.png)

添加时备注：**星球编号**，好友通过后请发送截图右侧的星球个人详情页。

![](https://oss.open8gu.com/image-20240722111319147.png)

## 如何写到简历？

牛券 oneCoupon 系统拆分了六个模块，分别是引擎模块、分发模块、结算模块、后管模块、搜索模块以及网关模块。

更多内容查看知识星球主题：[如何将牛券oneCoupon写到简历上？](https://t.zsxq.com/gLRzP)

## 项目结构&分支

采用标准基于 Maven 的 SpringBoot 多 Modules 项目，并拆分通用基础组件避免技术类重复定义。而且，我们定义的包结构是适用于绝大部分场景的，你学完牛券，再去看公司的项目，基本上不会有违和。

![](https://oss.open8gu.com/image-20240822232858036.png)

为了让大家更好的学习，我们将课程和项目分支进行了结合。有一个完整代码 main 分支的技术上，然后开启了一个从零到一的分支，就是说会按照课程目录的形式，这样大家跟着写代码或者看的时候，能够做到绝对的循序渐进。

分支列表如下：

- **main**：主分支，包含最新代码和改动。
- **20240708_init-code_ding.ma**：初始化分支，包括一些公用代码，比如基础架构规约封装等。
- **20240814_dev_create-template_chain_ding.ma**：基于责任链模式创建优惠券模板。
- **20240815_dev_coupon-tablue_shardingsphere_ding.ma**：通过 ShardingSphere 完成优惠券分库分表。
- **20240816_dev_operation-log_mzt-biz-log_ding.ma**：引入日志组件优雅记录操作日志。
- **20240817_dev_no-duplicate-submit_lock_ding.ma**：基于注解实现分布式锁防重复提交。
- **20240818_dev_other-coupon-template_feature_ding.ma**：开发优惠券模板结束、增加发行量等功能
- **20240821_dev_coupon-template-close_rocketmq5_ding.ma**：RocketMQ5.x 延时消息修改优惠券结束状态。
- **20240822_dev_create-coupon-task_easyexcel_ding.ma**：通过 EasyExcel 创建优惠券分发任务。
- **20240823_optimize_create-coupon-task_threadpool-delayqueue_ding.ma**：通过线程池和延迟队列优化分发任务创建流程。
- **20240824_dev_coupon-task-execute_template-method_ding.ma**：执行优惠券分发任务。
- **20240825_dev_coupon-task-timing_xxl-job_ding.ma**：通过 XXL-Job 定时执行优惠券分发任务。
- **20240826_dev_coupon-template-query_cache_ding.ma**：解决缓存击穿、穿透等问题。
- **20240829_dev_coupon-distribute-v1-4_easyexcel-cache_ding.ma**：一步步优化用户优惠券分发过程，提高执行性能。
- **20240908_dev_acquire-coupon_seckill_ding.ma**：通过缓存配合数据库以及 Canal 完成秒杀流程。
- **20240910_dev_acquire-coupon-v2_seckill_ding.ma**：通过缓存配合 RocketMQ 消息队列完成秒杀流程。
- ......

## 项目开发者

参与项目开发的小伙伴们，欢迎提交 PR，分享你在开发过程中对代码优化、逻辑改进或问题修复的想法。提交的代码将会经过审核，审核通过后会被合并，并为你的贡献进行记录。

```xml
<developers>
    <developer>
        <name>马丁</name>
        <email>machen@apache.org</email>
        <url>https://github.com/magestacks</url>
        <organization>Apache and openGoofy</organization>
    </developer>

    <developer>
        <name>优雅</name>
        <email>elegance520@163.com</email>
    </developer>

    <developer>
        <name>Henry</name>
        <email>yzwh0102@qq.com</email>
    </developer>
</developers>
```
