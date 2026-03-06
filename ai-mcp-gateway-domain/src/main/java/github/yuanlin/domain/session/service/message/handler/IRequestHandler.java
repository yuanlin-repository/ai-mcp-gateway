package github.yuanlin.domain.session.service.message.handler;

import github.yuanlin.domain.session.model.valobj.McpSchemaVO;

/**
 * @author yuanlin.zhou
 * @date 2026/3/6 12:06
 * @description TODO
 */
public interface IRequestHandler {

    McpSchemaVO.JSONRPCResponse handle(McpSchemaVO.JSONRPCRequest message);

}
