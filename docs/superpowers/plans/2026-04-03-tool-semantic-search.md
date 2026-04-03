# Tool Semantic Search Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `POST /api-gateway/{gatewayId}/tools/search` REST endpoint that accepts a free-text query and returns the top 5 semantically relevant tools using OpenAI Embedding vectors and cosine similarity.

**Architecture:** The feature follows the existing DDD layering (trigger → case → domain → infrastructure). A new `IEmbeddingPort` interface in the domain layer abstracts the Spring AI `EmbeddingModel`; its implementation lives in infrastructure. The domain service handles all business logic (fetching tools, batch embedding, cosine similarity, top-5 selection). The case layer provides a thin use-case wrapper; the controller converts domain VOs to API DTOs.

**Tech Stack:** Spring AI 1.0.0 `EmbeddingModel` (OpenAI), MyBatis (existing), Lombok, JUnit 4 + Spring Boot Test

---

## File Map

| Action | File | Responsibility |
|--------|------|----------------|
| Modify | `ai-mcp-gateway-infrastructure/pom.xml` | Add Spring AI OpenAI starter dependency (compile scope) |
| Create | `ai-mcp-gateway-api/src/main/java/github/yuanlin/api/dto/ToolSearchResultDTO.java` | API-layer response DTO |
| Modify | `ai-mcp-gateway-api/src/main/java/github/yuanlin/api/IMcpGatewayService.java` | Add `searchTools()` method signature |
| Create | `ai-mcp-gateway-domain/src/main/java/github/yuanlin/domain/session/model/valobj/ToolSearchResultVO.java` | Domain value object holding name, description, score |
| Create | `ai-mcp-gateway-domain/src/main/java/github/yuanlin/domain/session/adapter/port/IEmbeddingPort.java` | Port interface for batch text embedding |
| Create | `ai-mcp-gateway-infrastructure/src/main/java/github/yuanlin/infrastructure/adapter/port/EmbeddingPort.java` | Spring AI adapter implementing `IEmbeddingPort` |
| Create | `ai-mcp-gateway-domain/src/main/java/github/yuanlin/domain/session/service/IToolSearchDomainService.java` | Domain service interface |
| Create | `ai-mcp-gateway-domain/src/main/java/github/yuanlin/domain/session/service/search/ToolSearchDomainService.java` | Domain service: loads tools, calls embedding port, computes cosine similarity, returns top 5 |
| Create | `ai-mcp-gateway-case/src/main/java/github/yuanlin/cases/mcp/IToolSearchService.java` | Use-case interface |
| Create | `ai-mcp-gateway-case/src/main/java/github/yuanlin/cases/mcp/search/ToolSearchService.java` | Use-case implementation, delegates to domain service |
| Modify | `ai-mcp-gateway-trigger/src/main/java/github/yuanlin/trigger/http/McpGatewayController.java` | Add `searchTools()` endpoint, VO→DTO conversion |
| Create | `ai-mcp-gateway-app/src/test/java/github/yuanlin/test/search/ToolSearchServiceTest.java` | Integration test |

---

## Task 1: Add Spring AI dependency to infrastructure module

**Files:**
- Modify: `ai-mcp-gateway-infrastructure/pom.xml`

- [ ] **Step 1: Add dependency**

Open `ai-mcp-gateway-infrastructure/pom.xml` and add inside `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

- [ ] **Step 2: Verify the project still compiles**

```bash
cd /Users/yuanlin.zhou/workspace/ai-mcp-gateway
mvn compile -pl ai-mcp-gateway-infrastructure -am -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add ai-mcp-gateway-infrastructure/pom.xml
git commit -m "build: add spring-ai-starter-model-openai to infrastructure"
```

---

## Task 2: Create `ToolSearchResultDTO` in api layer

**Files:**
- Create: `ai-mcp-gateway-api/src/main/java/github/yuanlin/api/dto/ToolSearchResultDTO.java`

- [ ] **Step 1: Create the DTO**

```java
package github.yuanlin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolSearchResultDTO {
    private String toolName;
    private String toolDescription;
    private double score;
}
```

- [ ] **Step 2: Compile to verify**

```bash
mvn compile -pl ai-mcp-gateway-api -am -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add ai-mcp-gateway-api/src/main/java/github/yuanlin/api/dto/ToolSearchResultDTO.java
git commit -m "feat: add ToolSearchResultDTO to api layer"
```

---

## Task 3: Add `searchTools` to `IMcpGatewayService`

**Files:**
- Modify: `ai-mcp-gateway-api/src/main/java/github/yuanlin/api/IMcpGatewayService.java`

- [ ] **Step 1: Add import and method**

The full updated file:

```java
package github.yuanlin.api;

import github.yuanlin.api.dto.ToolSearchResultDTO;
import github.yuanlin.api.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IMcpGatewayService {

    /**
     * 建立 SSE 连接
     */
    Flux<ServerSentEvent<String>> establishSSEConnection(String gatewayId) throws Exception;

    /**
     * 处理 SSE 消息
     */
    Mono<ResponseEntity<Void>> handleMessage(String gatewayId, String sessionId, String messageBody);

    /**
     * 语义搜索工具
     * @param gatewayId 网关ID
     * @param query     查询文本
     * @return 最多5条最相关的工具，按相似度降序
     */
    Response<List<ToolSearchResultDTO>> searchTools(String gatewayId, String query);
}
```

- [ ] **Step 2: Compile**

```bash
mvn compile -pl ai-mcp-gateway-api -am -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add ai-mcp-gateway-api/src/main/java/github/yuanlin/api/IMcpGatewayService.java
git commit -m "feat: add searchTools method to IMcpGatewayService"
```

---

## Task 4: Create `ToolSearchResultVO` in domain layer

**Files:**
- Create: `ai-mcp-gateway-domain/src/main/java/github/yuanlin/domain/session/model/valobj/ToolSearchResultVO.java`

- [ ] **Step 1: Create the value object**

```java
package github.yuanlin.domain.session.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolSearchResultVO {
    private String toolName;
    private String toolDescription;
    private double score;
}
```

- [ ] **Step 2: Compile**

```bash
mvn compile -pl ai-mcp-gateway-domain -am -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add ai-mcp-gateway-domain/src/main/java/github/yuanlin/domain/session/model/valobj/ToolSearchResultVO.java
git commit -m "feat: add ToolSearchResultVO to domain layer"
```

---

## Task 5: Create `IEmbeddingPort` and `EmbeddingPort`

**Files:**
- Create: `ai-mcp-gateway-domain/src/main/java/github/yuanlin/domain/session/adapter/port/IEmbeddingPort.java`
- Create: `ai-mcp-gateway-infrastructure/src/main/java/github/yuanlin/infrastructure/adapter/port/EmbeddingPort.java`

- [ ] **Step 1: Create domain port interface**

```java
package github.yuanlin.domain.session.adapter.port;

import java.util.List;

/**
 * Embedding 端口：批量将文本转换为向量
 */
public interface IEmbeddingPort {
    /**
     * @param texts 文本列表
     * @return 与输入顺序一一对应的向量列表
     */
    List<float[]> embed(List<String> texts);
}
```

- [ ] **Step 2: Create infrastructure adapter**

```java
package github.yuanlin.infrastructure.adapter.port;

import github.yuanlin.domain.session.adapter.port.IEmbeddingPort;
import jakarta.annotation.Resource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmbeddingPort implements IEmbeddingPort {

    @Resource
    private EmbeddingModel embeddingModel;

    @Override
    public List<float[]> embed(List<String> texts) {
        return embeddingModel.embed(texts);
    }
}
```

- [ ] **Step 3: Compile**

```bash
mvn compile -pl ai-mcp-gateway-infrastructure -am -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add ai-mcp-gateway-domain/src/main/java/github/yuanlin/domain/session/adapter/port/IEmbeddingPort.java
git add ai-mcp-gateway-infrastructure/src/main/java/github/yuanlin/infrastructure/adapter/port/EmbeddingPort.java
git commit -m "feat: add IEmbeddingPort and Spring AI EmbeddingPort adapter"
```

---

## Task 6: Create `IToolSearchDomainService` and `ToolSearchDomainService`

**Files:**
- Create: `ai-mcp-gateway-domain/src/main/java/github/yuanlin/domain/session/service/IToolSearchDomainService.java`
- Create: `ai-mcp-gateway-domain/src/main/java/github/yuanlin/domain/session/service/search/ToolSearchDomainService.java`

- [ ] **Step 1: Create domain service interface**

```java
package github.yuanlin.domain.session.service;

import github.yuanlin.domain.session.model.valobj.ToolSearchResultVO;

import java.util.List;

public interface IToolSearchDomainService {
    /**
     * 在 gatewayId 的工具列表中，用语义搜索找出与 query 最相关的前5个工具
     */
    List<ToolSearchResultVO> searchTools(String gatewayId, String query);
}
```

- [ ] **Step 2: Create domain service implementation**

```java
package github.yuanlin.domain.session.service.search;

import github.yuanlin.domain.session.adapter.port.IEmbeddingPort;
import github.yuanlin.domain.session.adapter.repository.ISessionRepository;
import github.yuanlin.domain.session.model.valobj.ToolSearchResultVO;
import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayToolConfigVO;
import github.yuanlin.domain.session.service.IToolSearchDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ToolSearchDomainService implements IToolSearchDomainService {

    private static final int TOP_N = 5;

    @Resource
    private ISessionRepository sessionRepository;

    @Resource
    private IEmbeddingPort embeddingPort;

    @Override
    public List<ToolSearchResultVO> searchTools(String gatewayId, String query) {
        List<McpGatewayToolConfigVO> tools = sessionRepository.queryMcpGatewayToolConfigListByGatewayId(gatewayId);
        if (tools == null || tools.isEmpty()) {
            return Collections.emptyList();
        }

        // 构建批量嵌入文本列表：第0个为 query，其余为工具文本
        List<String> texts = new ArrayList<>();
        texts.add(query);
        for (McpGatewayToolConfigVO tool : tools) {
            texts.add(tool.getToolName() + ": " + tool.getToolDescription());
        }

        log.info("开始批量 Embedding，gatewayId:{} 工具数量:{}", gatewayId, tools.size());
        List<float[]> embeddings = embeddingPort.embed(texts);
        float[] queryVec = embeddings.get(0);

        List<ToolSearchResultVO> results = new ArrayList<>();
        for (int i = 0; i < tools.size(); i++) {
            double score = cosineSimilarity(queryVec, embeddings.get(i + 1));
            results.add(ToolSearchResultVO.builder()
                    .toolName(tools.get(i).getToolName())
                    .toolDescription(tools.get(i).getToolDescription())
                    .score(score)
                    .build());
        }

        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results.stream().limit(TOP_N).collect(Collectors.toList());
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += (double) a[i] * b[i];
            normA += (double) a[i] * a[i];
            normB += (double) b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
```

- [ ] **Step 3: Compile**

```bash
mvn compile -pl ai-mcp-gateway-domain -am -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add ai-mcp-gateway-domain/src/main/java/github/yuanlin/domain/session/service/IToolSearchDomainService.java
git add ai-mcp-gateway-domain/src/main/java/github/yuanlin/domain/session/service/search/ToolSearchDomainService.java
git commit -m "feat: add ToolSearchDomainService with cosine similarity ranking"
```

---

## Task 7: Create `IToolSearchService` and `ToolSearchService` in case layer

**Files:**
- Create: `ai-mcp-gateway-case/src/main/java/github/yuanlin/cases/mcp/IToolSearchService.java`
- Create: `ai-mcp-gateway-case/src/main/java/github/yuanlin/cases/mcp/search/ToolSearchService.java`

- [ ] **Step 1: Create use-case interface**

```java
package github.yuanlin.cases.mcp;

import github.yuanlin.domain.session.model.valobj.ToolSearchResultVO;

import java.util.List;

public interface IToolSearchService {
    List<ToolSearchResultVO> searchTools(String gatewayId, String query);
}
```

- [ ] **Step 2: Create use-case implementation**

```java
package github.yuanlin.cases.mcp.search;

import github.yuanlin.cases.mcp.IToolSearchService;
import github.yuanlin.domain.session.model.valobj.ToolSearchResultVO;
import github.yuanlin.domain.session.service.IToolSearchDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolSearchService implements IToolSearchService {

    @Resource
    private IToolSearchDomainService toolSearchDomainService;

    @Override
    public List<ToolSearchResultVO> searchTools(String gatewayId, String query) {
        return toolSearchDomainService.searchTools(gatewayId, query);
    }
}
```

- [ ] **Step 3: Compile**

```bash
mvn compile -pl ai-mcp-gateway-case -am -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add ai-mcp-gateway-case/src/main/java/github/yuanlin/cases/mcp/IToolSearchService.java
git add ai-mcp-gateway-case/src/main/java/github/yuanlin/cases/mcp/search/ToolSearchService.java
git commit -m "feat: add IToolSearchService and ToolSearchService in case layer"
```

---

## Task 8: Add `searchTools` endpoint to `McpGatewayController`

**Files:**
- Modify: `ai-mcp-gateway-trigger/src/main/java/github/yuanlin/trigger/http/McpGatewayController.java`

- [ ] **Step 1: Add `IToolSearchService` injection and `searchTools` endpoint**

Add the following field injection after the existing `@Resource` fields (around line 47):

```java
@Resource
private IToolSearchService toolSearchService;
```

Then add the following method at the end of the class (before the closing `}`):

```java
/**
 * 语义搜索工具
 * POST /api-gateway/{gatewayId}/tools/search
 * Body: { "query": "..." }
 */
@PostMapping(value = "{gatewayId}/tools/search", consumes = MediaType.APPLICATION_JSON_VALUE)
@Override
public Response<List<ToolSearchResultDTO>> searchTools(@PathVariable("gatewayId") String gatewayId,
                                                       @RequestBody Map<String, String> body) {
    String query = body.get("query");
    if (StringUtils.isBlank(query)) {
        return Response.<List<ToolSearchResultDTO>>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                .build();
    }
    List<ToolSearchResultVO> vos = toolSearchService.searchTools(gatewayId, query);
    List<ToolSearchResultDTO> dtos = vos.stream()
            .map(vo -> ToolSearchResultDTO.builder()
                    .toolName(vo.getToolName())
                    .toolDescription(vo.getToolDescription())
                    .score(vo.getScore())
                    .build())
            .collect(Collectors.toList());
    return Response.<List<ToolSearchResultDTO>>builder()
            .code(ResponseCode.SUCCESS.getCode())
            .info(ResponseCode.SUCCESS.getInfo())
            .data(dtos)
            .build();
}
```

Also add the following imports at the top of the file (after the existing imports):

```java
import github.yuanlin.api.dto.ToolSearchResultDTO;
import github.yuanlin.api.response.Response;
import github.yuanlin.cases.mcp.IToolSearchService;
import github.yuanlin.domain.session.model.valobj.ToolSearchResultVO;
import github.yuanlin.types.enums.ResponseCode;
import java.util.List;
import java.util.stream.Collectors;
```

**Important:** The `IMcpGatewayService.searchTools()` signature must match what you're implementing. The method signature in the interface is:
```java
Response<List<ToolSearchResultDTO>> searchTools(String gatewayId, String query);
```
But the controller receives a `Map<String, String>` body and extracts `query` from it before calling `IMcpGatewayService`. Update the controller method to NOT implement the interface method directly if parameter mapping differs — instead, define the endpoint as a standalone method and call the interface internally.

The cleanest approach: keep the controller method implementing the interface (which takes `String query` directly), and use `@RequestBody Map<String, String> body` only in the controller endpoint, then extract `query` and call `super` / delegate:

Replace the above with this cleaner version of the endpoint that does NOT try to match the interface method signature for the HTTP binding:

```java
@PostMapping(value = "{gatewayId}/tools/search", consumes = MediaType.APPLICATION_JSON_VALUE)
public Response<List<ToolSearchResultDTO>> searchToolsEndpoint(
        @PathVariable("gatewayId") String gatewayId,
        @RequestBody Map<String, String> body) {
    String query = body.get("query");
    if (StringUtils.isBlank(query)) {
        return Response.<List<ToolSearchResultDTO>>builder()
                .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                .build();
    }
    return searchTools(gatewayId, query);
}

@Override
public Response<List<ToolSearchResultDTO>> searchTools(String gatewayId, String query) {
    List<ToolSearchResultVO> vos = toolSearchService.searchTools(gatewayId, query);
    List<ToolSearchResultDTO> dtos = vos.stream()
            .map(vo -> ToolSearchResultDTO.builder()
                    .toolName(vo.getToolName())
                    .toolDescription(vo.getToolDescription())
                    .score(vo.getScore())
                    .build())
            .collect(Collectors.toList());
    return Response.<List<ToolSearchResultDTO>>builder()
            .code(ResponseCode.SUCCESS.getCode())
            .info(ResponseCode.SUCCESS.getInfo())
            .data(dtos)
            .build();
}
```

- [ ] **Step 2: Compile**

```bash
mvn compile -pl ai-mcp-gateway-trigger -am -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 3: Compile full project**

```bash
mvn compile -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add ai-mcp-gateway-trigger/src/main/java/github/yuanlin/trigger/http/McpGatewayController.java
git commit -m "feat: add POST /{gatewayId}/tools/search endpoint to McpGatewayController"
```

---

## Task 9: Integration test

**Files:**
- Create: `ai-mcp-gateway-app/src/test/java/github/yuanlin/test/search/ToolSearchServiceTest.java`

Prerequisites: MySQL running on `127.0.0.1:13306` with `gateway_001` data; OpenAI API key configured in `application-dev.yml`.

- [ ] **Step 1: Create integration test**

```java
package github.yuanlin.test.search;

import com.alibaba.fastjson.JSON;
import github.yuanlin.cases.mcp.IToolSearchService;
import github.yuanlin.domain.session.model.valobj.ToolSearchResultVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ToolSearchServiceTest {

    @Resource
    private IToolSearchService toolSearchService;

    private static final String GATEWAY_ID = "gateway_001";

    /**
     * 正常搜索：结果不超过5条，按 score 降序
     */
    @Test
    public void test_search_returns_at_most_5_results_ordered_by_score() {
        List<ToolSearchResultVO> results = toolSearchService.searchTools(GATEWAY_ID, "查询员工信息");

        log.info("搜索结果: {}", JSON.toJSONString(results));

        Assert.assertNotNull(results);
        Assert.assertTrue("结果不超过5条", results.size() <= 5);

        for (int i = 0; i < results.size() - 1; i++) {
            Assert.assertTrue("结果按score降序",
                    results.get(i).getScore() >= results.get(i + 1).getScore());
        }
    }

    /**
     * 所有结果的 score 都在 [0, 1] 之间
     */
    @Test
    public void test_scores_are_in_valid_range() {
        List<ToolSearchResultVO> results = toolSearchService.searchTools(GATEWAY_ID, "获取公司数据");

        log.info("搜索结果: {}", JSON.toJSONString(results));

        for (ToolSearchResultVO result : results) {
            Assert.assertTrue("score >= 0", result.getScore() >= 0.0);
            Assert.assertTrue("score <= 1", result.getScore() <= 1.0);
            Assert.assertNotNull("toolName 不为空", result.getToolName());
            Assert.assertNotNull("toolDescription 不为空", result.getToolDescription());
        }
    }

    /**
     * 空工具列表：不报错，返回空列表
     * 使用一个不存在的 gatewayId 模拟空工具列表（工具数为0的情况）
     */
    @Test
    public void test_empty_tool_list_returns_empty() {
        List<ToolSearchResultVO> results = toolSearchService.searchTools("non_existent_gateway", "任意查询");

        log.info("空网关搜索结果: {}", JSON.toJSONString(results));

        Assert.assertNotNull(results);
        Assert.assertTrue("工具列表为空时返回空列表", results.isEmpty());
    }
}
```

- [ ] **Step 2: Run the tests**

```bash
cd /Users/yuanlin.zhou/workspace/ai-mcp-gateway
mvn test -pl ai-mcp-gateway-app -Dtest=ToolSearchServiceTest -q
```

Expected: All 3 tests pass. If `test_empty_tool_list_returns_empty` fails because the gateway exists but has tools, use a truly non-existent gateway ID.

- [ ] **Step 3: Commit**

```bash
git add ai-mcp-gateway-app/src/test/java/github/yuanlin/test/search/ToolSearchServiceTest.java
git commit -m "test: add ToolSearchServiceTest integration test"
```

---

## Self-Review Checklist

- [x] All file paths are exact (verified against repo structure)
- [x] `ToolSearchResultDTO` (api layer) and `ToolSearchResultVO` (domain layer) are distinct — no cross-layer type leakage
- [x] `IEmbeddingPort` is in domain `adapter/port/`, `EmbeddingPort` is in infra `adapter/port/` — mirrors existing `ISessionPort` / `SessionPort` pattern
- [x] Cosine similarity handles zero-norm vectors (returns 0.0)
- [x] Empty tool list returns empty list (no NPE, no API call)
- [x] `embeddingModel.embed(List<String>)` is the correct Spring AI 1.0.0 batch API
- [x] Controller uses two methods: `searchToolsEndpoint` (HTTP binding) + `searchTools` (interface impl) to avoid `@RequestBody Map` vs `String query` signature mismatch
- [x] All tasks compile-verified before commit
