package github.yuanlin.cases.mcp.session;

import github.yuanlin.cases.mcp.IMcpSessionService;
import github.yuanlin.cases.mcp.session.factory.DefaultMcpSessionFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author yuanlin.zhou
 * @date 2026/3/5 21:58
 * @description TODO
 */
@Slf4j
@Service
public class McpSessionService implements IMcpSessionService {

    @Resource
    private DefaultMcpSessionFactory defaultMcpSessionFactory;

    @Override
    public Flux<ServerSentEvent<String>> createMcpSession(String gatewayId) throws Exception {
        return defaultMcpSessionFactory.strategyHandler().apply(gatewayId, new DefaultMcpSessionFactory.DynamicContext());
    }
}
