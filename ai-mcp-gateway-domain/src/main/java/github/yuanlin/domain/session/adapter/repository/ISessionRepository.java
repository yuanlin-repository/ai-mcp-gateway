package github.yuanlin.domain.session.adapter.repository;

import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayConfigVO;

/**
 * 会话仓储接口
 */
public interface ISessionRepository {
    McpGatewayConfigVO queryMcpGatewayConfigByGatewayId(String gatewayId);

}
