# 黑马学成在线课程代码（后端）

学成在线（在线教育平台）后端微服务代码，基于 Spring Cloud Alibaba 实现，包含门户（课程浏览、检索、下单、学习）与教学管理（课程、媒资、订单管理）两套业务。

## 技术栈

- Spring Boot 2.3.7 + Spring Cloud Hoxton.SR9 + Spring Cloud Alibaba 2.2.6
- Nacos：服务注册发现与配置中心（各模块仅保留 `bootstrap.yml`，业务配置集中在 Nacos）
- Spring Cloud Gateway：统一入口、路由转发与认证过滤
- Spring Security OAuth2 / JWT：认证授权，token 存 Redis
- MyBatis-Plus + MySQL、Druid 连接池；Redis 缓存
- Elasticsearch：课程搜索；MinIO：媒资存储；RabbitMQ：消息通知
- XXL-JOB：视频转码、课程发布等定时任务
- Swagger / Knife4j：接口文档

## 项目结构

- xuecheng-plus-auth        用户认证服务（密码/微信登录，颁发 token）
- xuecheng-plus-checkcode   图形验证码服务
- xuecheng-plus-content     内容管理系统（课程、课程计划、课程发布）
- xuecheng-plus-learning    课程学习系统（选课、学习进度、支付回调）
- xuecheng-plus-media       媒资管理系统（文件上传、视频转码）
- xuecheng-plus-orders      课程订单与支付模块
- xuecheng-plus-search      基于 ES 的课程搜索
- xuecheng-plus-system      系统管理（字典、系统配置）
- xuecheng-plus-base        公共基础工具（统一异常、分页模型、通用配置）
- xuecheng-plus-message-sdk RabbitMQ 消息封装件（消息表 + 可靠投递）
- xuecheng-plus-gateway     微服务网关
- xuecheng-plus-parent      Maven 父模块（统一依赖与插件版本）
- xuecheng-plus-generator   代码生成器（可选，生成的代码已在其他模块）

## 设计说明

业务模块（content / learning / media / orders / system）统一拆分为三个子模块：

- `xxx-model`：PO、DTO、VO 等数据模型，供各层依赖
- `xxx-service`：Service、Mapper 与业务实现，可独立作为定时任务进程启动
- `xxx-api`：Controller 与安全配置，依赖 service 模块，对外提供 HTTP 接口

调用链为：前端 → gateway（鉴权、路由）→ xxx-api（参数校验、权限注解）→ xxx-service（业务与持久化）→ MySQL / Redis / ES / MQ。

服务名与网关路由：各服务注册名见模块 `bootstrap.yml`（如 `content-api`、`learning-api`、`orders-api`、`media-api`、`system-api`、`search`、`auth-service`、`checkcode`、`gateway`）；路由与数据源、Redis、MQ 等配置统一维护在 Nacos 配置中心。

## 本地运行

1. 启动依赖中间件：MySQL、Redis、Nacos、RabbitMQ，按需启动 Elasticsearch、MinIO、XXL-JOB。
2. Nacos 默认地址 `192.168.101.65:8848`，namespace `dev`，group `xuecheng-plus-project`。
3. 依次启动 gateway 及各 api 服务，课程测试数据脚本见 `doc/sql`。

调试提示：为便于不启动 checkcode 服务时测试登录，密码登录已临时跳过图形验证码校验，相关代码在 `xuecheng-plus-auth` 的 `PasswordAuthServiceImpl` 中以 `TODO @xuecheng-plus-checkcode` 标注，恢复校验时启用即可。
