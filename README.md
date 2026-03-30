# AI MCP Gateway

> [中文文档](./README.zh-CN.md) | **English**

A **DDD (Domain-Driven Design)**-based MCP (Model Context Protocol) gateway that routes AI model tool-call requests to backend services over HTTP, Kafka, and other protocols.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Modules](#modules)
- [Core Flow](#core-flow)
- [Database Design](#database-design)
- [API Reference](#api-reference)
- [Quick Start](#quick-start)
- [Tech Stack](#tech-stack)
- [MCP Protocol](#mcp-protocol)

---

## Overview

AI MCP Gateway sits between AI models (Claude, GPT, etc.) and real backend services, acting as a protocol translation and routing layer.

**Key capabilities**:
- Accepts persistent SSE connections from AI model clients
- Parses JSON-RPC 2.0 messages (`initialize`, `tools/list`, `tools/call`, etc.)
- Routes tool calls to HTTP or Kafka backends based on database configuration
- Supports nested parameter mapping and field transformation for tools
- Session lifecycle management with automatic 30-minute timeout cleanup

---

## Architecture

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
│  HTTP / Kafka backend services      │
└─────────────────────────────────────┘
```

Layer dependency (outer → inner; inner layers have no dependency on outer layers):

```
trigger  (HTTP entry point)
    └── case  (use-case orchestration / state machine)
            └── domain  (business logic / handlers)
                    └── infrastructure  (DB / HTTP / Kafka)
```

---

## Modules

| Module | Responsibility |
|--------|---------------|
| `ai-mcp-gateway-app` | Spring Boot entry point, configuration, port 8777 |
| `ai-mcp-gateway-trigger` | HTTP Controller — SSE connection and message ingestion |
| `ai-mcp-gateway-api` | Public service interface definitions |
| `ai-mcp-gateway-case` | Use-case layer — state-machine-driven session orchestration |
| `ai-mcp-gateway-domain` | Core business logic, MCP handlers, session management |
| `ai-mcp-gateway-infrastructure` | MyBatis DAOs, Retrofit HTTP client, Kafka producer |
| `ai-mcp-gateway-types` | Shared enums, exceptions, constants |

### Domain Handlers

| Handler | MCP Method | Description |
|---------|-----------|-------------|
| `InitializeHandler` | `initialize` | Handshake — returns server capability declaration |
| `ToolsListHandler` | `tools/list` | Returns all tools and their JSON Schema for the gateway |
| `ToolsCallHandler` | `tools/call` | Executes a tool call over HTTP or Kafka |
| `ResourcesListHandler` | `resources/list` | Resource listing (currently returns empty list) |

---

## Core Flow

### 1. Establish Connection

```
Client  ──GET /api-gateway/{gatewayId}/mcp/sse──▶  Gateway
        ◀── SSE stream ──────────────────────────
        ◀── event: endpoint  (contains sessionId)
```

### 2. Initialize Handshake

```
Client  ──POST /mcp/sse?sessionId=xxx──▶  Gateway
        Body: { "method": "initialize", ... }

        ◀── SSE message: InitializeResult ───────
        (protocol version + server capabilities)
```

### 3. List Tools

```
Client  ──POST──▶  { "method": "tools/list" }
        ◀── SSE: tool names, descriptions, JSON Schema parameter structure
```

### 4. Call a Tool

```
Client  ──POST──▶  { "method": "tools/call", "params": { "name": "...", "arguments": {...} } }
        Gateway queries DB for protocol config (HTTP or Kafka)
        ├── HTTP:  sends GET/POST via Retrofit
        └── Kafka: publishes message to configured topic via KafkaProducer
        ◀── SSE: tool execution result
```

---

## Database Design

Database name: `ai_mcp_gateway` — init script: `docs/dev-ops/mysql/sql/ai_mcp_gateway.sql`

| Table | Description |
|-------|-------------|
| `mcp_gateway` | Gateway registry (gatewayId, name, version) |
| `mcp_gateway_auth` | API key and rate-limit configuration |
| `mcp_gateway_tool` | Tool definitions, linked to gateway and protocol |
| `mcp_protocol_http` | HTTP protocol config (URL, method, headers, timeout) |
| `mcp_protocol_kafka` | Kafka protocol config (broker, topic, serializers, etc.) |
| `mcp_protocol_mapping` | Field mapping config with nested structure support (`parent_path`) |

---

## API Reference

### Establish SSE Connection

```
GET /api-gateway/{gatewayId}/mcp/sse
```

Returns a `text/event-stream` response. The first event carries the message endpoint URL (including `sessionId`).

### Send an MCP Message

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

Returns `202 Accepted`. The actual result is pushed asynchronously to the client's SSE stream.

---

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0 (default port 13306, configurable)
- Kafka (port 9092, only required for Kafka-protocol tools)

### Initialize Database

```bash
mysql -u root -p < docs/dev-ops/mysql/sql/ai_mcp_gateway.sql
```

### Configure

Edit `ai-mcp-gateway-app/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:13306/ai_mcp_gateway
    username: root
    password: 123456
  kafka:
    bootstrap-servers: localhost:9092
```

### Build & Run

```bash
mvn clean package -DskipTests
java -jar ai-mcp-gateway-app/target/ai-mcp-gateway-app.jar
```

Service starts at: `http://localhost:8777/api-gateway`

---

## Tech Stack

| Category | Technology |
|----------|-----------|
| Framework | Spring Boot 3.4.3, Spring WebFlux |
| Reactive | Project Reactor (Flux / Mono / Sinks) |
| ORM | MyBatis 3.0.4 |
| HTTP Client | Retrofit 2.9.0 + OkHttp 4.9.3 |
| Messaging | Apache Kafka 3.2.0 |
| Database | MySQL 8.0 |
| AI Integration | Spring AI 1.0.0 |
| Design Patterns | xfg-wrench 3.0.0 (state machine, etc.) |
| Utilities | Lombok, FastJSON 2, Guava |

---

## MCP Protocol

Protocol version: `2024-11-05`, JSON-RPC 2.0 compatible.

Error codes follow the MCP specification:

| Code | Meaning |
|------|---------|
| -32700 | Parse error |
| -32601 | Method not found |
| -32000 | Session not found |
| -32001 | Session expired |
| -32003 | Tool not found |
| -32007 | Unsupported protocol version |
