package github.yuanlin.infrastructure.adapter.repository;

import github.yuanlin.domain.session.adapter.repository.ISessionRepository;
import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import github.yuanlin.infrastructure.dao.IMcpGatewayDao;
import github.yuanlin.infrastructure.dao.IMcpProtocolRegistryDao;
import github.yuanlin.infrastructure.dao.po.McpGatewayPO;
import github.yuanlin.infrastructure.dao.po.McpProtocolRegistryPO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * @author yuanlin.zhou
 * @date 2026/3/7 15:43
 * @description 会话仓储服务
 */
@Repository
public class SessionRepository implements ISessionRepository {

    @Resource
    private IMcpGatewayDao mcpGatewayDao;

    @Resource
    private IMcpProtocolRegistryDao mcpProtocolRegistryDao;

    @Override
    public McpGatewayConfigVO queryMcpGatewayConfigByGatewayId(String gatewayId) {
        // 1. 查询网关配置
        McpGatewayPO mcpGatewayPO = mcpGatewayDao.queryMcpGatewayByGatewayId(gatewayId);
        if (null == mcpGatewayPO) return null;

        // 2. 查询协议注册（1:1 -> gatewayId:toolId）
        McpProtocolRegistryPO mcpProtocolRegistryPO = mcpProtocolRegistryDao.queryMcpProtocolRegistryByGatewayId(gatewayId);
        if (null == mcpProtocolRegistryPO) return null;

        return McpGatewayConfigVO.builder()
                .gatewayId(mcpGatewayPO.getGatewayId())
                .gatewayName(mcpGatewayPO.getGatewayName())
                .toolId(mcpProtocolRegistryPO.getToolId())
                .toolName(mcpProtocolRegistryPO.getToolName())
                .toolDesc(mcpProtocolRegistryPO.getToolDescription())
                .toolVersion(mcpProtocolRegistryPO.getToolVersion())
                .build();
    }
}
