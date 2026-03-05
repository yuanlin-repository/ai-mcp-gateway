package github.yuanlin.cases.mcp.session.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import github.yuanlin.cases.mcp.session.AbstractMcpSessionSupport;
import github.yuanlin.cases.mcp.session.factory.DefaultMcpSessionFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author yuanlin.zhou
 * @date 2026/3/5 21:32
 * @description TODO
 */
@Slf4j
@Service
public class VerifyNode extends AbstractMcpSessionSupport {

    @Resource
    private SessionNode sessionNode;

    @Override
    protected Flux<ServerSentEvent<String>> doApply(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        if (StringUtils.isBlank(requestParameter)) {
            log.error("校验请求参数失败");
            throw new IllegalArgumentException("Session VerifyNode 校验请求参数失败");
        }
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> get(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws Exception {
        return sessionNode;
    }
}
