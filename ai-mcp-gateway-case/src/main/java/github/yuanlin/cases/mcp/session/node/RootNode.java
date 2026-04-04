package github.yuanlin.cases.mcp.session.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import github.yuanlin.cases.mcp.session.AbstractMcpSessionSupport;
import github.yuanlin.cases.mcp.session.factory.DefaultMcpSessionFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author yuanlin.zhou
 * @date 2026/3/5 21:31
 * @description TODO
 */
@Slf4j
@Service("mcpSessionRootNode")
public class RootNode extends AbstractMcpSessionSupport {

    @Resource(name = "mcpSessionVerifyNode")
    private VerifyNode verifyNode;

    @Override
    protected Flux<ServerSentEvent<String>> doApply(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        try {
            log.info("创建会话 mcp session RootNode:{}", requestParameter);

            return router(requestParameter, dynamicContext);
        } catch (Exception e) {
            log.error("创建会话 mcp session RootNode 异常:{}", requestParameter, e);
            throw e;
        }
    }

    @Override
    public StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> get(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        return verifyNode;
    }
}
