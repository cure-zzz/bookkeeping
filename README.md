# 个人记账网站

一款简洁优雅的个人记账应用，基于 Vue 3 + Spring Boot 构建。

## 功能特性

- **用户认证**：注册、登录、JWT Token 鉴权
- **账单管理**：添加、编辑、删除、筛选账单
- **统计报表**：收支概览、分类占比图、趋势图
- **预算管理**：设置分类预算、进度追踪

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.5
- MyBatis 3.0.3
- MySQL 8.0
- JWT (jjwt 0.12.5)

### 前端
- Vue 3.4.x
- Vite 5.x
- Element Plus 2.5.x
- Pinia 2.x
- ECharts 5.x

## 快速开始

### 1. 数据库配置

创建 MySQL 数据库并执行 SQL 脚本：

```sql
-- 创建数据库
CREATE DATABASE bookkeeping DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bookkeeping;

-- 执行以下建表语句（参考 backend/src/main/resources/schema.sql）
```

修改 `backend/src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bookkeeping?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: your_password
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端服务将运行在 http://localhost:8080

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将运行在 http://localhost:5173

## 项目结构

```
bookkeeping/
├── backend/                    # 后端 Spring Boot 项目
│   ├── src/main/java/com/bookkeeping/
│   │   ├── config/            # 配置类
│   │   ├── controller/        # 控制器
│   │   ├── service/          # 业务逻辑
│   │   ├── mapper/           # MyBatis Mapper
│   │   ├── entity/           # 实体类
│   │   ├── dto/              # 数据传输对象
│   │   └── common/           # 通用类
│   └── src/main/resources/
│       └── mapper/           # XML映射文件
├── frontend/                   # 前端 Vue 3 项目
│   ├── src/
│   │   ├── api/              # API请求
│   │   ├── components/       # 组件
│   │   ├── router/           # 路由
│   │   ├── stores/           # Pinia状态管理
│   │   └── views/            # 页面
│   └── index.html
└── SPEC.md                   # 项目规范文档
```

## API 接口

### 认证接口
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| GET | /api/auth/info | 获取用户信息 |
| POST | /api/auth/logout | 退出登录 |

### 账单接口
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/bills | 获取账单列表 |
| POST | /api/bills | 添加账单 |
| PUT | /api/bills/:id | 更新账单 |
| DELETE | /api/bills/:id | 删除账单 |
| GET | /api/bills/statistics | 获取统计数据 |

### 分类接口
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/categories | 获取分类列表 |

### 预算接口
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/budgets | 获取预算列表 |
| POST | /api/budgets | 设置预算 |
| PUT | /api/budgets/:id | 更新预算 |
| DELETE | /api/budgets/:id | 删除预算 |

## 默认分类

### 收入分类
- 工资、奖金、投资收益、副业收入、其他收入

### 支出分类
- 餐饮、交通、购物、居住、医疗、娱乐、教育、通讯、服装、其他支出

## 截图

![登录页面](screenshots/login.png)
![首页](screenshots/home.png)
![账单管理](screenshots/bills.png)
![统计报表](screenshots/statistics.png)
