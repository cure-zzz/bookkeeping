# 个人记账网站 - 项目规范

## 1. 概念与愿景

一款简洁优雅的个人记账应用，采用现代卡片式设计语言，让用户轻松记录日常收支。界面清新明快，操作流畅自然，图表可视化让财务状况一目了然。

## 2. 设计语言

### 美学方向
- 采用清新现代的设计风格，以柔和渐变和卡片阴影营造层次感
- 主色调使用蓝绿色系，象征财务健康和增长

### 色彩系统
```
主色(Primary): #409EFF (Element Plus 蓝)
成功色(Success): #67C23A
警告色(Warning): #E6A23C
危险色(Danger): #F56C6C
信息色(Info): #909399
背景色(Background): #F5F7FA
卡片色(Card): #FFFFFF
文字主色(Text Primary): #303133
文字次要(Text Secondary): #606266
文字辅助(Text Placeholder): #C0C4CC
边框色(Border): #EBEEF5
```

### 字体
- 主字体：'PingFang SC', 'Microsoft YaHei', sans-serif
- 数字字体：'DIN Alternate', 'Helvetica Neue', monospace

### 动效哲学
- 页面切换：淡入淡出 300ms ease
- 卡片悬浮：translateY(-2px) + shadow 200ms
- 按钮点击：scale(0.98) 100ms
- 数据加载：骨架屏动画

## 3. 项目架构

### 后端 (Spring Boot + MyBatis)
```
backend/
├── src/main/java/com/bookkeeping/
│   ├── BookkeepingApplication.java
│   ├── config/           # 配置类
│   ├── controller/       # 控制器
│   ├── service/          # 业务逻辑
│   ├── mapper/           # MyBatis Mapper
│   ├── entity/           # 实体类
│   ├── dto/              # 数据传输对象
│   ├── common/           # 通用类
│   └── interceptor/      # 拦截器
├── src/main/resources/
│   ├── mapper/           # XML映射文件
│   └── application.yml
└── pom.xml
```

### 前端 (Vue 3 + Element Plus)
```
frontend/
├── src/
│   ├── api/              # API请求
│   ├── assets/           # 静态资源
│   ├── components/       # 组件
│   ├── router/           # 路由
│   ├── stores/           # Pinia状态管理
│   ├── utils/            # 工具函数
│   ├── views/            # 页面
│   ├── App.vue
│   └── main.js
├── index.html
└── package.json
```

### 数据库设计 (MySQL)
```sql
-- 用户表
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    avatar VARCHAR(255) DEFAULT '/avatar/default.jpg',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 分类表
CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(50),
    type TINYINT NOT NULL COMMENT '1:收入 2:支出',
    color VARCHAR(20),
    user_id BIGINT COMMENT 'NULL表示系统预设',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 账单表
CREATE TABLE bill (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    type TINYINT NOT NULL COMMENT '1:收入 2:支出',
    category_id BIGINT NOT NULL,
    description VARCHAR(255),
    bill_date DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (category_id) REFERENCES category(id)
);

-- 预算表
CREATE TABLE budget (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    month VARCHAR(7) NOT NULL COMMENT '格式: 2024-01',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (category_id) REFERENCES category(id)
);
```

## 4. 功能清单

### 认证模块
- [x] 用户注册（用户名、密码、确认密码）
- [x] 用户登录（用户名、密码）
- [x] JWT Token 鉴权
- [x] 登出功能

### 账单管理
- [x] 添加账单（金额、类型、分类、日期、备注）
- [x] 账单列表（分页、筛选）
- [x] 编辑账单
- [x] 删除账单
- [x] 按日期范围筛选
- [x] 按分类筛选

### 统计报表
- [x] 本月收支概览
- [x] 支出分类饼图
- [x] 收支趋势折线图
- [x] 预算进度条

### 分类管理
- [x] 系统预设分类
- [x] 自定义分类

## 5. API 设计

### 认证接口
```
POST /api/auth/register    - 用户注册
POST /api/auth/login      - 用户登录
POST /api/auth/logout     - 退出登录
GET  /api/auth/info        - 获取用户信息
```

### 账单接口
```
GET    /api/bills          - 获取账单列表
POST   /api/bills          - 添加账单
PUT    /api/bills/:id      - 更新账单
DELETE /api/bills/:id      - 删除账单
GET    /api/bills/statistics - 获取统计数据
```

### 分类接口
```
GET /api/categories        - 获取分类列表
```

### 预算接口
```
GET    /api/budgets        - 获取预算列表
POST   /api/budgets        - 设置预算
PUT    /api/budgets/:id    - 更新预算
DELETE /api/budgets/:id    - 删除预算
```

## 6. 页面清单

### 登录/注册页
- 双栏布局：左侧品牌展示，右侧表单
- 表单验证、错误提示
- 登录注册切换动画

### 主页布局
- 顶部导航栏（Logo、用户头像、下拉菜单）
- 侧边菜单（首页、账单管理、统计报表、预算管理）

### 首页
- 欢迎卡片 + 今日收支概览
- 最近账单列表
- 快捷添加按钮

### 账单管理页
- 筛选栏（日期范围、类型、分类）
- 账单表格（支持编辑、删除）
- 新增账单抽屉表单

### 统计报表页
- 月份选择器
- 收支概览卡片
- ECharts 饼图 + 折线图

### 预算管理页
- 月份选择器
- 预算列表（环形进度）
- 新增/编辑预算弹窗

## 7. 技术栈版本

### 后端
- Java 17
- Spring Boot 3.2.x
- MyBatis 3.5.x
- MySQL 8.0
- JWT (jjwt 0.12.x)
- Hutool 工具库

### 前端
- Vue 3.4.x
- Vite 5.x
- Element Plus 2.5.x
- Pinia 2.x
- Vue Router 4.x
- Axios
- ECharts 5.x
- @vueuse/core
