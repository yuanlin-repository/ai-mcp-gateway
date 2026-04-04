package github.yuanlin.cases.mcp.message.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import github.yuanlin.cases.mcp.message.node.RootNode;
import github.yuanlin.cases.mcp.session.factory.DefaultMcpSessionFactory;
import github.yuanlin.domain.session.model.entity.HandleMessageCommandEntity;
import github.yuanlin.domain.session.model.valobj.SessionConfigVO;
import github.yuanlin.domain.session.model.entity.HandleMessageCommandEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * MCP会话消息工厂
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2026/2/20 07:39
 */
@Service
public class DefaultMcpMessageFactory {

    @Resource(name = "mcpMessageRootNode")
    private RootNode rootNode;

    public StrategyHandler<HandleMessageCommandEntity, DynamicContext, ResponseEntity<Void>> strategyHandler() {
        return rootNode;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {
        private SessionConfigVO sessionConfigVO;
    }

}
