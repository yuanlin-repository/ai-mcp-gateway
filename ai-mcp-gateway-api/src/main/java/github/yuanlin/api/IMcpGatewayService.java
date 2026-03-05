package github.yuanlin.api;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface IMcpGatewayService {

    /**
     * 建立 SSE 连接
     * @param gatewayId 网关ID
     * @return 流式响应
     */
    Flux<ServerSentEvent<String>> establishSSEConnection(String gatewayId) throws Exception;
}
