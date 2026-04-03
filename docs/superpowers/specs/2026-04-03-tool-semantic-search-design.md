# Tool Semantic Search Feature Design

**Date:** 2026-04-03
**Status:** Approved

---

## Overview

Add a REST API endpoint that accepts a free-text query and returns the top 5 most semantically relevant tools under a given `gatewayId`, using OpenAI Embedding vectors and cosine similarity.

---

## API Interface

### Endpoint

```
POST /api-gateway/{gatewayId}/tools/search
Content-Type: application/json
```

### Request Body

```json
{
  "query": "查询某个员工的信息"
}
```

### Response Body

```json
{
  "code": "0000",
  "info": "成功",
  "data": [
    {
      "toolName": "getCompanyEmployee",
      "toolDescription": "根据员工ID查询员工详细信息",
      "score": 0.92
    }
  ]
}
```

- Returns at most 5 results, ordered by `score` descending (0.0–1.0 cosine similarity)
- Uses the existing `Response<T>` wrapper

---

## Architecture

### Layered Components

```
trigger      McpGatewayController.searchTools()          [new endpoint]
api          IMcpGatewayService.searchTools()             [new method]
case         IToolSearchService / ToolSearchService       [new]
domain       IToolSearchDomainService                     [new interface]
             ToolSearchDomainService                      [new impl]
             IEmbeddingPort                               [new port interface]
             ToolSearchResultVO                           [new value object]
infra        EmbeddingPort (implements IEmbeddingPort)    [new, wraps Spring AI EmbeddingModel]
```

### Data Flow

```
Controller
  → ToolSearchService (case)
    → ToolSearchDomainService (domain)
        ├── ISessionRepository.queryMcpGatewayToolConfigListByGatewayId()  [existing]
        └── IEmbeddingPort.embed(texts)
              → EmbeddingPort (infra)
                → Spring AI EmbeddingModel
                  → OpenAI Embedding API
```

---

## Core Logic

### Embedding Strategy

On each search request:
1. Build a list of texts: `[query, "toolName1: desc1", "toolName2: desc2", ...]`
2. Send all texts in a **single batch** call to `EmbeddingModel.embedAll()` to minimize API round-trips
3. The first element of the result is the query vector; the rest are tool vectors

### Cosine Similarity

```
score(A, B) = (A · B) / (|A| × |B|)
```

Pure Java implementation, no extra dependencies. Scores are computed for each tool against the query vector, sorted descending, top 5 returned.

### Edge Cases

| Condition | Behavior |
|-----------|----------|
| Tool count < 5 | Return all tools |
| Tool list empty | Return empty list (no error) |
| Embedding API failure | Throw `AppException`, returns error response via global handler |

---

## New Files

| Module | File | Purpose |
|--------|------|---------|
| `api` | `IMcpGatewayService` | Add `searchTools()` method |
| `trigger` | `McpGatewayController` | Add `searchTools()` endpoint |
| `case` | `IToolSearchService` | Use case interface |
| `case` | `ToolSearchService` | Use case implementation |
| `domain` | `IToolSearchDomainService` | Domain service interface |
| `domain` | `ToolSearchDomainService` | Domain service implementation |
| `domain` | `IEmbeddingPort` | Port interface for embedding |
| `domain` | `ToolSearchResultVO` | Value object for search result |
| `infra` | `EmbeddingPort` | Spring AI EmbeddingModel adapter |
| `app` (test) | `ToolSearchServiceTest` | Integration test |

---

## Dependency Change

`spring-ai-starter-model-openai` is currently `test` scope in `ai-mcp-gateway-app/pom.xml`.
It must be added as `compile` scope to `ai-mcp-gateway-infrastructure/pom.xml` so that `EmbeddingPort` can inject `EmbeddingModel` at runtime.

---

## Testing

Integration test in `ai-mcp-gateway-app/src/test/java/github/yuanlin/test/`:

- **Test 1:** Normal search — result count ≤ 5, ordered by score descending
- **Test 2:** Semantically relevant tool ranks higher than an unrelated tool
- **Test 3:** Empty tool list under gatewayId — returns empty list without error

Uses real OpenAI API via existing `application-dev.yml` configuration (no mocking).
