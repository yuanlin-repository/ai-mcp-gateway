package github.yuanlin.domain.session.model.entity;

import github.yuanlin.domain.session.model.valobj.McpSchemaVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 处理消息命令实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HandleMessageCommandEntity {

    private String gatewayId;

    private String sessionId;

    private McpSchemaVO.JSONRPCMessage jsonrpcMessage;

    public HandleMessageCommandEntity(String gatewayId, String sessionId, String messageBody) throws Exception {
        this.gatewayId = gatewayId;
        this.sessionId = sessionId;
        this.jsonrpcMessage = McpSchemaVO.deserializeJsonRpcMessage(messageBody);
    }

}
