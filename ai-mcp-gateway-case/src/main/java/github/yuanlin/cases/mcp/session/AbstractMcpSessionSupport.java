package github.yuanlin.cases.mcp.session;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import github.yuanlin.cases.mcp.session.factory.DefaultMcpSessionFactory;
import github.yuanlin.domain.session.service.ISessionManagementService;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author yuanlin.zhou
 * @date 2026/3/5 21:23
 * @description TODO
 */
public abstract class AbstractMcpSessionSupport extends AbstractMultiThreadStrategyRouter<String, DefaultMcpSessionFactory.DynamicContext, Flux<ServerSentEvent<String>>> {

    @Resource
    protected ISessionManagementService sessionManagementService;

    @Override
    protected void multiThread(String requestParameter, DefaultMcpSessionFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {

    }

}
