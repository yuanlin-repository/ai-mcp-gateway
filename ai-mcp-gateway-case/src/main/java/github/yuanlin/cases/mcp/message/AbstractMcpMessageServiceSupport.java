package github.yuanlin.cases.mcp.message;

import github.yuanlin.cases.mcp.message.factory.DefaultMcpMessageFactory;
import github.yuanlin.domain.session.model.entity.HandleMessageCommandEntity;
import github.yuanlin.domain.session.service.ISessionManagementService;
import github.yuanlin.domain.session.service.ISessionMessageService;
import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import org.springframework.http.ResponseEntity;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractMcpMessageServiceSupport extends AbstractMultiThreadStrategyRouter<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Void>> {

    @Resource
    protected ISessionMessageService serviceMessageService;

    @Resource
    protected ISessionManagementService sessionManagementService;

    @Override
    protected void multiThread(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {

    }

}
