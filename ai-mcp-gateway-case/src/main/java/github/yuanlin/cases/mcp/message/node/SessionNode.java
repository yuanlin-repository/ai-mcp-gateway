package github.yuanlin.cases.mcp.message.node;

import github.yuanlin.cases.mcp.message.AbstractMcpMessageServiceSupport;
import github.yuanlin.cases.mcp.message.factory.DefaultMcpMessageFactory;
import github.yuanlin.domain.session.model.entity.HandleMessageCommandEntity;
import github.yuanlin.domain.session.model.valobj.SessionConfigVO;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 会话处理
 */
@Slf4j
@Service("mcpMessageSessionNode")
public class SessionNode extends AbstractMcpMessageServiceSupport {

    @Resource(name = "mcpMessageMessageHandlerNode")
    private MessageHandlerNode messageHandlerNode;

    @Override
    protected ResponseEntity<Void> doApply(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        log.info("消息处理 mcp message SessionNode:{}", requestParameter);

        SessionConfigVO sessionConfigVO = sessionManagementService.getSession(requestParameter.getSessionId());
        if (null == sessionConfigVO) {
            log.warn("会话不存在或已过期，gatewayId:{} sessionId:{}", requestParameter.getGatewayId(), requestParameter.getSessionId());
            return ResponseEntity.notFound().build();
        }

        dynamicContext.setSessionConfigVO(sessionConfigVO);

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<HandleMessageCommandEntity, DefaultMcpMessageFactory.DynamicContext, ResponseEntity<Void>> get(HandleMessageCommandEntity requestParameter, DefaultMcpMessageFactory.DynamicContext dynamicContext) throws Exception {
        return messageHandlerNode;
    }

}
