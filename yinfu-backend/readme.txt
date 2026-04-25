银龄心伴Spring Boot后端服务

本文件夹为银龄心伴后端服务的根目录，采用Spring Boot 2.7.18、MyBatis Plus、MySQL、JWT等技术开发。

主要目录说明：
- src/main/java/  - Java源代码
- src/main/resources/ - 配置文件
- sql/            - 数据库初始化脚本
- target/         - 编译输出目录
- pom.xml         - Maven项目配置

后端架构（Controller-Service-Mapper三层架构）：
- controller/    - 控制器层，处理HTTP请求
- service/        - 业务逻辑层
- mapper/         - 数据访问层（MyBatis Plus）
- entity/         - 实体类
- dto/            - 数据传输对象
- common/         - 公共组件（Result、JwtUtil、Constants）
- config/         - 配置类（CORS、Security）
- exception/      - 异常处理

主要功能模块：
- 用户认证（登录注册）
- 健康管理
- 用药提醒
- SOS紧急求救
- AI聊天与问诊
