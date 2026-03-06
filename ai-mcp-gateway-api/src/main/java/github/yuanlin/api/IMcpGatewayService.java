package github.yuanlin.api;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IMcpGatewayService {

    /**
     * 建立 SSE 连接
     * @param gatewayId 网关ID
     * @return 流式响应
     */
    Flux<ServerSentEvent<String>> establishSSEConnection(String gatewayId) throws Exception;

    /**
     * 处理 SSE 消息
     * @param sessionId 会话ID
     * @param messageBody 请求消息
     * @return 响应结果
     */
    Mono<ResponseEntity<Void>> handleMessage(String gatewayId, String sessionId, String messageBody);
}
