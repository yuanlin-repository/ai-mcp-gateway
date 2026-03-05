package github.yuanlin.cases.mcp.session.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import github.yuanlin.cases.mcp.session.AbstractMcpSessionSupport;
import github.yuanlin.cases.mcp.session.factory.DefaultMcpSessionFactory;
import github.yuanlin.domain.session.model.valobj.SessionConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

/**
 * @author yuanlin.zhou
 * @date 2026/3/5 21:32
 * @description TODO
 */
@Slf4j
@Service
public class EndNode extends AbstractMcpSessionSupport {
    @Override
    protected Flux<ServerSentEvent<String>> doApply(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        log.info("创建会话-EndNode:{}", requestParameter);

        SessionConfigVO sessionConfigVO = dynamicContext.getSessionConfigVO();
        String sessionId = sessionConfigVO.getSessionId();

        Sinks.Many<ServerSentEvent<String>> sink = sessionConfigVO.getSink();

        return sink.asFlux()
                .mergeWith(
                        Flux.interval(Duration.ofSeconds(60))
                                .map(i -> ServerSentEvent.<String>builder()
                                        .event("ping")
                                        .data("ping")
                                        .build())
                ).doOnCancel(() -> {
                    log.info("SSE连接取消，会话ID: {}", sessionId);
                    sessionManagementService.removeSession(sessionId);
                })
                .doOnTerminate(() -> {
                    log.info("SSE连接终止，会话ID: {}", sessionId);
                    sessionManagementService.removeSession(sessionId);
                });
    }

    @Override
    public StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> get(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
