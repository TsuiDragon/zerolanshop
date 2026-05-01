# zerolanshop

零蓝权益销售系统，包含前台采购端和后台管理端。前台采购端面向采购用户，后台管理端面向系统运营者。

## 功能概览

- 前台采购端
  - 用户注册、登录、退出
  - 采购工作台首页
  - 为后续采购中心、订单、财务、卡密、报表等模块预留导航

- 后台管理端
  - 管理员登录
  - 后台首页数据概览
  - 商品分类管理
  - 商品分类支持两级结构、排序、启用/禁用、编辑、删除

## 技术栈

- 后端：Spring Boot 3.5、Spring Security、MyBatis-Plus、MySQL、Redis、JWT、Lombok
- 前端：Vue 3、TypeScript、Vite、lucide-vue-next
- 数据库：MySQL 8.x
- 运行环境：JDK 21、Node.js、npm

## 目录结构

```text
zerolanshop
├─ backend                  # Spring Boot 后端
│  ├─ src/main/java
│  │  └─ cn/zerolan/zerolanshop
│  │     ├─ common          # 通用响应、全局异常处理
│  │     ├─ config          # 基础配置
│  │     ├─ controller      # REST 控制器
│  │     ├─ domain          # DTO 与实体
│  │     ├─ mapper          # MyBatis-Plus Mapper
│  │     ├─ security        # Spring Security 与 JWT 过滤器
│  │     ├─ service         # 业务逻辑
│  │     └─ util            # 工具类
│  └─ src/main/resources
│     ├─ application.yaml   # 后端配置
│     └─ db/schema.sql      # 数据库表结构
├─ frontend                 # Vue 前端
│  ├─ src/App.vue
│  ├─ src/main.ts
│  └─ src/style.css
└─ README.md
```

## 路由约定

### 前台采购端

| 路径 | 说明 |
| --- | --- |
| `/` | 自动跳转到 `/login` |
| `/login` | 前台用户登录/注册页 |
| `/index` | 前台采购工作台首页 |

### 后台管理端

| 路径 | 说明 |
| --- | --- |
| `/admin` | 自动跳转到 `/admin/login` |
| `/admin/login` | 后台管理员登录页 |
| `/admin/index` | 后台首页 |
| `/admin/goods/category` | 商品分类管理页 |

## 后端接口

接口统一返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 前台用户认证

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/auth/sessions` | 创建前台登录会话 |
| `POST` | `/api/auth/register` | 注册前台用户 |
| `DELETE` | `/api/auth/sessions/current` | 删除当前登录会话 |

兼容旧路径：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/auth/login` | 旧登录路径 |
| `POST` | `/api/auth/logout` | 旧退出路径 |

### 后台管理员认证

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/admin/sessions` | 创建后台登录会话 |

兼容旧路径：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/admin/login` | 旧管理员登录路径 |

### 后台商品分类

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/admin/categories` | 查询分类平铺列表 |
| `GET` | `/api/admin/categories/tree` | 查询两级分类树 |
| `GET` | `/api/admin/categories/{id}` | 查询分类详情 |
| `POST` | `/api/admin/categories` | 创建分类 |
| `PUT` | `/api/admin/categories/{id}` | 更新分类 |
| `PATCH` | `/api/admin/categories/{id}/status` | 更新分类状态 |
| `DELETE` | `/api/admin/categories/{id}` | 删除分类 |

商品分类业务规则：

- 只支持两级分类。
- `parentId = 0` 表示一级分类。
- 创建分类时自动取同级最大 `sort + 1`。
- 同一父级下分类名称不能重复。
- 有子分类的一级分类禁止删除。
- 状态更新不级联影响子分类。

## 环境变量

后端支持通过环境变量覆盖默认配置：

| 变量名 | 默认值 | 说明 |
| --- | --- | --- |
| `JWT_SECRET` | `zerolanshop-jwt-secret-key-2026-min-32-bytes` | JWT 签名密钥 |
| `JWT_EXPIRATION` | `86400000` | JWT 有效期，单位毫秒 |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | 空 | Redis 密码 |
| `REDIS_DATABASE` | `0` | Redis 数据库编号 |

MySQL 连接配置目前在 `backend/src/main/resources/application.yaml` 中维护：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zerolanshop?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 1234
```

公开仓库中不要提交真实生产账号、密码、密钥。生产环境建议通过环境变量或部署平台密钥管理能力注入。

## 数据库初始化

1. 创建数据库：

```sql
CREATE DATABASE zerolanshop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行表结构脚本：

```text
backend/src/main/resources/db/schema.sql
```

3. 插入后台管理员账号。密码需要使用 BCrypt 加密后写入 `admin.password` 字段。

## 启动项目

### 启动后端

```powershell
cd backend
mvn spring-boot:run
```

后端默认监听：

```text
http://localhost:8080
```

### 启动前端

```powershell
cd frontend
npm install
npm run dev
```

前端默认监听：

```text
http://localhost:5173
```

前端开发服务已配置 `/api` 代理到 `http://localhost:8080`。

## 代码规范

- 后端 Controller 使用资源路径和 HTTP 方法表达操作语义。
- 后端 Controller 保持薄层逻辑，业务规则放在 Service。
- 后端统一使用 `Result<T>` 响应结构。
- 后端异常由 `GlobalExceptionHandler` 统一处理。
- 商品分类等关键业务规则使用中文注释说明。
- 前端路由按前台端和后台端分离：
  - 前台：`/login`、`/index`
  - 后台：`/admin/login`、`/admin/index`、`/admin/goods/category`

## 常见问题
