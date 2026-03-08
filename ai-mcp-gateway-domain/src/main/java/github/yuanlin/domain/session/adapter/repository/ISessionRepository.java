package github.yuanlin.domain.session.adapter.repository;

import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayProtocolConfigVO;
import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayToolConfigVO;

import java.util.List;

/**
 * 会话仓储接口
 */
public interface ISessionRepository {
    McpGatewayConfigVO queryMcpGatewayConfigByGatewayId(String gatewayId);

    List<McpGatewayToolConfigVO> queryMcpGatewayToolConfigListByGatewayId(String gatewayId);

    McpGatewayProtocolConfigVO queryMcpGatewayProtocolConfig(String gatewayId);

}
