package github.yuanlin.domain.session.service;

import github.yuanlin.domain.session.model.entity.HandleMessageCommandEntity;
import github.yuanlin.domain.session.model.valobj.McpSchemaVO;

/**
 * @author yuanlin.zhou
 * @date 2026/3/6 12:03
 * @description TODO
 */
public interface ISessionMessageService {

    McpSchemaVO.JSONRPCResponse processHandlerMessage(String gatewayId, McpSchemaVO.JSONRPCMessage message);

    McpSchemaVO.JSONRPCResponse processHandlerMessage(HandleMessageCommandEntity commandEntity);
}