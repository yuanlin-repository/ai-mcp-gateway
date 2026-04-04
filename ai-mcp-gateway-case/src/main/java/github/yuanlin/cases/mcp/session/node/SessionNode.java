package github.yuanlin.cases.mcp.session.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import github.yuanlin.cases.mcp.session.AbstractMcpSessionSupport;
import github.yuanlin.cases.mcp.session.factory.DefaultMcpSessionFactory;
import github.yuanlin.domain.session.model.valobj.SessionConfigVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author yuanlin.zhou
 * @date 2026/3/5 21:32
 * @description TODO
 */
@Slf4j
@Service("mcpSessionSessionNode")
public class SessionNode extends AbstractMcpSessionSupport {

    @Resource(name = "mcpSessionEndNode")
    private EndNode endNode;

    @Override
    protected Flux<ServerSentEvent<String>> doApply(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        log.info("创建会话-SessionNode:{}", requestParameter);

        SessionConfigVO sessionConfigVO = sessionManagementService.createSession(requestParameter);

        dynamicContext.setSessionConfigVO(sessionConfigVO);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> get(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        return endNode;
    }
}
