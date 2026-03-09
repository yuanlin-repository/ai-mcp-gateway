package github.yuanlin.domain.session.adapter.port;

import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayProtocolConfigVO;

import java.io.IOException;

/**
 * 回话端口
 */
public interface ISessionPort {
    Object toolCall(McpGatewayProtocolConfigVO.HTTPConfig httpConfig, Object params) throws IOException;

    Object toolCall(McpGatewayProtocolConfigVO.KafkaConfig kafkaConfig, Object params);
}
