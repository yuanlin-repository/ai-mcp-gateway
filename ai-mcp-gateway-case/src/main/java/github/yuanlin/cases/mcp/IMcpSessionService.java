package github.yuanlin.cases.mcp;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface IMcpSessionService {

    /**
     * 创建 MCP 会话服务
     *
     * @param gatewayId 网关ID（后续还要扩展 apiKey 验证字段）
     * @return 流式响应
     */
    Flux<ServerSentEvent<String>> createMcpSession(String gatewayId) throws Exception;
}
