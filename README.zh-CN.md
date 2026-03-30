# AI MCP Gateway

> **中文文档** | [English](./README.md)

基于 **DDD（领域驱动设计）** 架构的 MCP（Model Context Protocol）网关，用于将 AI 模型的工具调用请求路由到后端服务（HTTP、Kafka 等协议）。

---

## 目录

- [项目简介](#项目简介)
- [架构概览](#架构概览)
- [模块说明](#模块说明)
- [核心流程](#核心流程)
- [数据库设计](#数据库设计)
- [API 接口](#api-接口)
- [快速开始](#快速开始)
- [技术栈](#技术栈)
- [MCP 协议版本](#mcp-协议版本)

---

## 项目简介

AI MCP Gateway 是一个 MCP 协议网关，作为 AI 模型（如 Claude、GPT）与实际业务服务之间的中间层。

**核心能力**：
- 接受来自 AI 模型客户端的 SSE 长连接
- 解析 JSON-RPC 2.0 协议消息（`initialize`、`tools/list`、`tools/call` 等）
- 根据数据库配置，将工具调用路由到 HTTP 或 Kafka 后端
- 支持工具参数的嵌套结构映射与字段转换
- 会话生命周期管理（30 分钟超时自动清理）

---

## 架构概览

```
AI Model Client
      │
      │  SSE (Server-Sent Events)
      ▼
┌─────────────────────────────────────┐
│          ai-mcp-gateway             │
│                                     │
│  trigger → case → domain → infra   │
│                                     │
│  HTTP/Kafka 后端服务                │
└─────────────────────────────────────┘
```

分层依赖关系（从外到内，内层不依赖外层）：

```
trigger（HTTP 入口）
    └── case（用例编排 / 状态机）
            └── domain（领域逻辑 / 处理器）
                    └── infrastructure（DB / HTTP / Kafka）
```

---

## 模块说明

| 模块 | 职责 |
|------|------|
| `ai-mcp-gateway-app` | Spring Boot 启动入口，配置文件，端口 8777 |
| `ai-mcp-gateway-trigger` | HTTP Controller，SSE 连接建立与消息接收 |
| `ai-mcp-gateway-api` | 对外服务接口定义 |
| `ai-mcp-gateway-case` | 用例层，基于状态机编排会话创建流程 |
| `ai-mcp-gateway-domain` | 核心业务逻辑，MCP 协议处理器，会话管理 |
| `ai-mcp-gateway-infrastructure` | MyBatis 数据访问，Retrofit HTTP 客户端，Kafka 生产者 |
| `ai-mcp-gateway-types` | 公共枚举、异常、常量 |

### 领域层处理器

| 处理器 | 对应 MCP 方法 | 说明 |
|--------|--------------|------|
| `InitializeHandler` | `initialize` | 握手，返回服务端能力声明 |
| `ToolsListHandler` | `tools/list` | 查询网关下所有工具及其 JSON Schema |
| `ToolsCallHandler` | `tools/call` | 执行工具调用，支持 HTTP / Kafka |
| `ResourcesListHandler` | `resources/list` | 资源列表（当前返回空列表） |

---

## 核心流程

### 1. 建立连接

```
Client  ──GET /api-gateway/{gatewayId}/mcp/sse──▶  Gateway
        ◀── SSE stream ─────────────────────────
        ◀── event: endpoint  (含 sessionId) ────
```

### 2. 初始化握手

```
Client  ──POST /mcp/sse?sessionId=xxx──▶  Gateway
        Body: { "method": "initialize", ... }

        ◀── SSE message: InitializeResult ──────
        (包含协议版本、服务端能力信息)
```

### 3. 查询工具列表

```
Client  ──POST──▶  { "method": "tools/list" }
        ◀── SSE: 工具名称、描述、JSON Schema 参数结构
```

### 4. 调用工具

```
Client  ──POST──▶  { "method": "tools/call", "params": { "name": "...", "arguments": {...} } }
        Gateway 查询数据库获取协议配置（HTTP 或 Kafka）
        ├── HTTP: 通过 Retrofit 发送 GET/POST 请求
        └── Kafka: 通过 KafkaProducer 发送消息到指定 topic
        ◀── SSE: 工具执行结果
```

---

## 数据库设计

数据库名：`ai_mcp_gateway`，建表脚本：`docs/dev-ops/mysql/sql/ai_mcp_gateway.sql`

| 表名 | 说明 |
|------|------|
| `mcp_gateway` | 网关注册信息（gatewayId、名称、版本） |
| `mcp_gateway_auth` | API Key 与限流配置 |
| `mcp_gateway_tool` | 工具定义，关联网关与协议 |
| `mcp_protocol_http` | HTTP 协议配置（URL、Method、Headers、超时） |
| `mcp_protocol_kafka` | Kafka 协议配置（broker、topic、序列化等） |
| `mcp_protocol_mapping` | 字段映射配置，支持嵌套结构（parent_path） |

---

## API 接口

### 建立 SSE 连接

```
GET /api-gateway/{gatewayId}/mcp/sse
```

响应为 `text/event-stream`，首个事件携带后续消息发送地址（含 sessionId）。

### 发送 MCP 消息

```
POST /api-gateway/{gatewayId}/mcp/sse?sessionId={sessionId}
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "initialize | tools/list | tools/call | resources/list",
  "id": "1",
  "params": { ... }
}
```

响应为 `202 Accepted`，实际结果通过 SSE 流推送给客户端。

---

## 快速开始

### 前置依赖

- Java 17+
- Maven 3.8+
- MySQL 8.0（端口 13306，或修改配置）
- Kafka（端口 9092，仅 Kafka 协议工具需要）

### 初始化数据库

```bash
mysql -u root -p < docs/dev-ops/mysql/sql/ai_mcp_gateway.sql
```

### 修改配置

编辑 `ai-mcp-gateway-app/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:13306/ai_mcp_gateway
    username: root
    password: 123456
  kafka:
    bootstrap-servers: localhost:9092
```

### 构建与运行

```bash
mvn clean package -DskipTests
java -jar ai-mcp-gateway-app/target/ai-mcp-gateway-app.jar
```

服务启动后监听：`http://localhost:8777/api-gateway`

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Spring Boot 3.4.3, Spring WebFlux |
| 响应式 | Project Reactor (Flux / Mono / Sinks) |
| ORM | MyBatis 3.0.4 |
| HTTP 客户端 | Retrofit 2.9.0 + OkHttp 4.9.3 |
| 消息队列 | Apache Kafka 3.2.0 |
| 数据库 | MySQL 8.0 |
| AI 集成 | Spring AI 1.0.0 |
| 设计模式库 | xfg-wrench 3.0.0（状态机等） |
| 工具 | Lombok, FastJSON 2, Guava |

---

## MCP 协议版本

`2024-11-05`，兼容 JSON-RPC 2.0。

错误码遵循 MCP 规范：

| 错误码 | 含义 |
|--------|------|
| -32700 | 解析错误 |
| -32601 | 方法不存在 |
| -32000 | 会话未找到 |
| -32001 | 会话已过期 |
| -32003 | 工具未找到 |
| -32007 | 不支持的协议版本 |
