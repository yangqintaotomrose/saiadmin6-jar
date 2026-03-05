# Spring Boot 企业级管理系统

<div align="center">

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.0-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](#)

基于Spring Boot构建的企业级后台管理系统，集成了完善的权限控制、代码生成、系统监控等功能

</div>

## 🌟 项目特色

- 🔐 **完善的权限体系**: 基于Sa-Token的RBAC权限控制，支持细粒度权限管理
- 🛠️ **智能代码生成**: 一键生成前后端CRUD代码，大幅提升开发效率
- 📊 **全面系统监控**: 实时监控服务器内存、磁盘、性能指标
- 📁 **丰富功能模块**: 用户管理、角色权限、系统配置、数据字典等完整功能
- 🔒 **多重安全保障**: JWT认证、Redis缓存、操作日志、安全审计
- 🎨 **现代化界面**: 响应式设计，支持多端访问

## 🏗️ 技术架构

### 后端技术栈
```
Spring Boot 2.7.0
├── Sa-Token (认证授权框架)
├── MyBatis-Plus (ORM框架)
├── Redis (缓存/会话存储)
├── MySQL (主数据库)
├── Hutool (Java工具库)
├── FastJSON (JSON处理)
├── Velocity (模板引擎)
└── ip2region (IP地理位置)
```

### 前端技术栈
```
Vue 3 + Element Plus
├── Vue Router (路由管理)
├── Pinia (状态管理)
├── Axios (HTTP客户端)
└── Sass (样式预处理)
```

## 🚀 快速开始

### 环境要求

- **JDK**: 8+
- **MySQL**: 5.7+
- **Redis**: 3.0+
- **Maven**: 3.6+

### 安装部署

#### 1. 克隆项目
```bash
git clone https://github.com/your-username/springboot-enterprise-system.git
cd springboot-enterprise-system
```

#### 2. 数据库配置
```sql
-- 创建数据库
CREATE DATABASE enterprise_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入初始数据（如有）
-- mysql -u root -p enterprise_system < src/main/resources/sql/init.sql
```

#### 3. 环境配置
```bash
# 复制配置文件模板
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml

# 编辑配置文件，设置数据库和Redis连接信息
vim src/main/resources/application-dev.yml
```

#### 4. 启动项目
```bash
# 开发环境启动
mvn spring-boot:run

# 或者打包后运行
mvn clean package -DskipTests
java -jar target/springboot-enterprise-system.jar
```

### 访问地址

- **管理后台**: http://localhost:8080
- **API文档**: http://localhost:8080/doc.html
- **健康检查**: http://localhost:8080/actuator/health

## 📁 项目结构

```
src/main/java/com/abc/
├── bean/                  # 业务实体类
│   ├── CodeTemplateBean.java    # 代码生成模板引擎
│   └── TreeBuilder.java         # 树形结构构建工具
├── config/                # 配置类
│   ├── AppConfig.java           # 应用基础配置
│   ├── DaoInterceptor.java      # 数据库拦截器
│   ├── GlobalExceptionHandler.java # 全局异常处理
│   ├── RedisConfig.java         # Redis配置
│   ├── SaTokenConfig.java       # 认证授权配置
│   ├── SessionAuthenticationInterceptor.java # 会话认证拦截器
│   ├── WebMvcConfig.java        # Web MVC配置
│   └── XtrRunner.java           # 应用启动初始化
├── web/                   # Web层
│   ├── annotation/        # 自定义注解
│   │   └── Log.java             # 操作日志注解
│   ├── aspect/            # 切面编程
│   │   └── LogAspect.java       # 操作日志切面
│   ├── controller/        # 控制器层
│   │   ├── common/        # 通用控制器
│   │   │   ├── CoreController.java     # 核心认证接口
│   │   │   ├── UserController.java     # 用户管理
│   │   │   ├── RoleController.java     # 角色管理
│   │   │   ├── MenuController.java     # 菜单管理
│   │   │   ├── SystemController.java   # 系统管理
│   │   │   ├── ConfigController.java   # 系统配置
│   │   │   ├── DictTypeController.java # 字典类型管理
│   │   │   ├── DictDataController.java # 字典数据管理
│   │   │   ├── FileController.java     # 文件管理
│   │   │   ├── MonitorController.java  # 系统监控
│   │   │   ├── GenerateTablesController.java # 代码生成
│   │   │   └── DemoController.java     # 演示功能
│   │   └── WebController.java   # 基础Web控制器
│   ├── domain/            # 领域模型
│   │   ├── HttpStatus.java      # HTTP状态码
│   │   └── R.java               # 统一响应结果
│   ├── scheduler/         # 定时任务
│   │   └── AsyncConfig.java     # 异步配置
│   ├── service/           # 服务层
│   │   └── JwtRedisService.java # JWT Redis服务
│   ├── timer/             # 定时器
│   │   └── TaskTimer.java       # 任务定时器
│   └── util/              # 工具类
│       ├── EnhancedOperationLogUtil.java # 增强操作日志工具
│       ├── FileUploadUtil.java    # 文件上传工具
│       ├── Ip2regionUtil.java     # IP地理位置工具
│       ├── JwtUtil.java           # JWT工具类
│       ├── LoginHelper.java       # 登录助手
│       ├── LoginLogUtil.java      # 登录日志工具
│       ├── OllamaUtil.java        # AI服务工具
│       ├── OperationLogUtil.java  # 操作日志工具
│       └── SaTokenUtil.java       # Sa-Token工具类
└── MainApplication.java   # 应用启动类
```

## 🔧 核心功能模块

### 👥 用户权限管理
- **用户管理**: 用户信息维护、状态管理、密码重置
- **角色管理**: 角色定义、权限分配、菜单授权
- **菜单管理**: 菜单配置、权限标识、层级管理
- **部门管理**: 组织架构、人员归属、权限继承

### ⚙️ 系统配置
- **参数配置**: 系统参数管理、配置分组、动态生效
- **数据字典**: 字典类型管理、字典项维护、状态控制
- **通知公告**: 系统公告发布、状态管理、阅读统计
- **操作日志**: 用户行为记录、操作审计、安全追溯

### 🛠️ 开发工具
- **代码生成**: 基于数据库表结构自动生成前后端代码
- **模板引擎**: Velocity模板支持，灵活定制生成规则
- **API调试**: 内置接口测试工具，方便开发调试
- **系统监控**: 服务器资源监控、性能指标展示

### 📁 文件管理
- **附件管理**: 文件上传下载、分类管理、权限控制
- **存储配置**: 本地存储、云存储等多种存储方式
- **文件预览**: 支持常见文件格式在线预览
- **安全控制**: 文件访问权限、防盗链保护

## 🔐 安全特性

### 认证授权
- 基于Sa-Token的JWT令牌认证
- Redis缓存用户会话信息
- 细粒度的RBAC权限控制
- 多终端登录支持和会话管理

### 数据安全
- 敏感信息加密存储
- SQL注入防护机制
- XSS攻击防范措施
- CSRF跨站请求伪造保护

### 访问控制
- IP黑白名单管理
- 登录失败次数限制
- 会话超时自动退出
- 操作行为审计日志

### 系统防护
- 接口限流和熔断
- 异常信息脱敏处理
- 安全日志记录
- 实时安全监控告警

## 🛠️ 开发指南

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用统一的代码格式化配置
- 完善的注释和文档说明
- 单元测试覆盖率不低于80%

### API设计
```java
// RESTful风格的API设计示例
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/{id}")
    public R<User> detail(@PathVariable Long id) {
        // 实现逻辑
    }
    
    @PostMapping
    public R<Void> create(@RequestBody UserDTO userDTO) {
        // 实现逻辑
    }
}
```

### 数据库设计
- 使用下划线命名规范
- 主键统一使用bigint类型
- 时间字段统一使用timestamp
- 软删除字段delete_time

## 📊 API文档

### 认证接口
```
POST /proxy/core/login        # 用户登录
POST /proxy/core/logout       # 用户登出
GET  /proxy/core/captcha      # 获取验证码
```

### 用户管理接口
```
GET    /proxy/core/user/index     # 用户列表
POST   /proxy/core/user/save      # 新增用户
PUT    /proxy/core/user/update    # 更新用户
DELETE /proxy/core/user/destroy   # 删除用户
```

### 系统监控接口
```
GET /proxy/core/server/monitor    # 系统监控信息
GET /proxy/core/system/dictAll    # 获取字典数据
GET /proxy/core/system/menu       # 获取菜单信息
```

## 🐳 Docker部署

```dockerfile
# Dockerfile
FROM openjdk:8-jre-alpine
COPY target/springboot-enterprise-system.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# 构建和运行
docker build -t enterprise-system .
docker run -d -p 8080:8080 \
  -e DB_HOST=mysql \
  -e REDIS_HOST=redis \
  enterprise-system
```

## 🤝 贡献指南

我们欢迎任何形式的贡献！

### 提交Issue
- 使用清晰的标题描述问题
- 提供详细的复现步骤
- 标注相关的环境信息

### Pull Request
1. Fork项目到个人仓库
2. 创建feature分支
3. 提交代码变更
4. 发起Pull Request

### 代码规范
- 遵循项目现有的代码风格
- 添加必要的单元测试
- 更新相关文档

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

- **项目主页**: https://github.com/your-username/springboot-enterprise-system
- **问题反馈**: https://github.com/your-username/springboot-enterprise-system/issues
- **电子邮箱**: support@example.com
- **交流群**: QQ群 123456789

## 🙏 致谢

感谢以下开源项目的支持：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Sa-Token](https://sa-token.cc/)
- [MyBatis-Plus](https://baomidou.com/)
- [Hutool](https://hutool.cn/)
- [Element Plus](https://element-plus.org/)

---

<p align="center">
  Made with ❤️ by Enterprise System Team
</p>
