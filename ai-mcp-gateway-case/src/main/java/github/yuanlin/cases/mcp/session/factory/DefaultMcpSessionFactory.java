package github.yuanlin.cases.mcp.session.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import github.yuanlin.cases.mcp.session.node.RootNode;
import github.yuanlin.domain.session.model.valobj.SessionConfigVO;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author yuanlin.zhou
 * @date 2026/3/5 21:31
 * @description MCP 会话服务工厂
 */
@Service
public class DefaultMcpSessionFactory {

    @Resource
    private RootNode rootNode;

    public StrategyHandler<String, DynamicContext, Flux<ServerSentEvent<String>>> strategyHandler() {
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
