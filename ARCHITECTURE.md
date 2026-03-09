# AI MCP Gateway 架构图

## 项目概述

**AI MCP Gateway** 是一个基于 DDD（领域驱动设计）架构的 MCP (Model Context Protocol) 网关，用于管理和转发 MCP 协议的工具调用请求。

---

## 整体架构图

```mermaid
flowchart TB
    subgraph Client ["客户端"]
        AI["AI Model"]
        User["User"]
    end

    subgraph Gateway ["AI MCP Gateway"]
        subgraph Trigger ["触发器层 ai-mcp-gateway-trigger"]
            Controller["McpGatewayController\nHTTP/SSE 入口"]
        end

        subgraph API ["API 接口层 ai-mcp-gateway-api"]
            IFace["IMcpGatewayService\n对外服务接口"]
            Resp["Response<T>\n通用响应"]
        end

        subgraph Case ["用例层 ai-mcp-gateway-case"]
            SessionSvc["IMcpSessionService\n会话服务"]
            MessageSvc["IMcpMessageService\n消息服务"]
            SessionFactory["DefaultMcpSessionFactory\n会话工厂"]
            SessionNode["SessionNode\n状态机节点"]
        end

        subgraph Domain ["领域层 ai-mcp-gateway-domain"]
            subgraph DomainService ["领域服务"]
                SessionMgmt["SessionManagementService\n会话管理"]
                SessionMsg["SessionMessageService\n消息路由"]
            end

            subgraph Handler ["Handler 处理器"]
                InitHandler["InitializeHandler\n初始化握手"]
                ToolsList["ToolsListHandler\n工具列表"]
                ToolsCall["ToolsCallHandler\n工具调用"]
                ResList["ResourcesListHandler\n资源列表"]
            end

            subgraph VO ["值对象"]
                SchemaVO["McpSchemaVO\nMCP协议结构"]
                SessionCfg["SessionConfigVO\n会话配置"]
                GatewayCfg["McpGatewayConfigVO\n网关配置"]
                ToolCfg["McpGatewayToolConfigVO\n工具配置"]
            end
        end

        subgraph Infrastructure ["基础设施层 ai-mcp-gateway-infrastructure"]
            DAO["DAO 层\nIMcpGatewayDao\nIMcpGatewayToolDao..."]
            Repo["SessionRepository\n会话仓储"]
            Port["SessionPort\n外部端口"]
            HTTPGateway["GenericHttpGateway\nHTTP网关\nRetrofit + OkHttp"]
        end

        subgraph Types ["类型定义 ai-mcp-gateway-types"]
            Enums["ResponseCode\nMcpErrorCodes\n枚举定义"]
            Exceptions["AppException\n应用异常"]
        end
    end

    subgraph External ["外部服务"]
        MCPTool1["MCP Tool Server 1"]
        MCPTool2["MCP Tool Server 2"]
        MCPToolN["MCP Tool Server N"]
    end

    AI -->|HTTP/SSE| Controller
    User -->|HTTP/SSE| Controller
    Controller --> IFace
    Controller --> SessionSvc
    SessionSvc --> SessionFactory
    SessionFactory --> SessionNode
    SessionNode --> SessionMgmt
    SessionNode --> SessionMsg
    SessionMsg --> InitHandler
    SessionMsg --> ToolsList
    SessionMsg --> ToolsCall
    SessionMsg --> ResList
    InitHandler --> Port
    ToolsList --> Port
    ToolsCall --> Port
    ResList --> Port
    Port --> HTTPGateway
    HTTPGateway -->|HTTP| MCPTool1
    HTTPGateway -->|HTTP| MCPTool2
    HTTPGateway -->|HTTP| MCPToolN
    Port --> Repo
    Repo --> DAO
```

---

## 模块依赖关系

```mermaid
graph TD
    App["ai-mcp-gateway-app\n应用启动"] --> Trigger
    App --> Types
    
    Trigger["ai-mcp-gateway-trigger\n触发器层"] --> API
    Trigger --> Case
    
    API["ai-mcp-gateway-api\n接口层"] --> Types
    
    Case["ai-mcp-gateway-case\n用例层"] --> Domain
    Case --> Types
    Case --> API
    
    Domain["ai-mcp-gateway-domain\n领域层"] --> Types
    Domain --> Infrastructure
    
    Infrastructure["ai-mcp-gateway-infrastructure\n基础设施层"] --> Domain
    Infrastructure --> Types
    
    Types["ai-mcp-gateway-types\n类型定义"]
```

---

## 数据流图

```mermaid
sequenceDiagram
    participant Client as AI Model
    participant Controller as McpGatewayController
    participant SessionSvc as SessionService
    participant Handler as RequestHandler
    participant Port as SessionPort
    participant HTTPGateway as GenericHttpGateway
    participant MCPService as MCP Tool Server

    Client->>Controller: HTTP/SSE Request
    Controller->>SessionSvc: 建立会话/处理消息
    
    alt 建立会话
        SessionSvc->>SessionSvc: 创建 Session
        SessionSvc-->>Client: 返回 Session ID
    else 处理消息
        SessionSvc->>Handler: 路由到具体 Handler
        Handler->>Port: 执行调用
        Port->>HTTPGateway: HTTP 请求
        HTTPGateway->>MCPService: 发起 MCP Protocol Request
        MCPService-->>HTTPGateway: MCP Protocol Response
        HTTPGateway-->>Port: HTTP Response
        Port-->>Handler: 处理结果
        Handler-->>SessionSvc: 返回结果
        SessionSvc-->>Client: SSE Response
    end
```

---

## 技术栈

- **框架**: Spring Boot 3.4.3
- **AI**: Spring AI 1.0.0
- **协议**: MCP (Model Context Protocol) - JSON-RPC 2.0
- **HTTP**: Retrofit 2.9.0 + OkHttp 4.9.3
- **数据库**: MySQL + MyBatis
- **设计框架**: xfg-wrench-starter-design-framework
- **其他**: Guava, FastJSON, JWT

---

## 数据库表

| 表名 | 说明 |
|------|------|
| mcp_gateway | 网关配置表 |
| mcp_gateway_tool | 工具配置表 |
| mcp_gateway_auth | 认证配置表 |
| mcp_protocol_http | HTTP 协议配置表 |
| mcp_protocol_mapping | 协议映射配置表 |
| mcp_protocol_registry | 协议注册表 |
