package github.yuanlin.domain.session.service.message.handler.impl;


import github.yuanlin.domain.session.model.valobj.McpSchemaVO;
import github.yuanlin.domain.session.service.message.handler.IRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 返回可用资源列表
 */
@Slf4j
@Service("resourcesListHandler")
public class ResourcesListHandler implements IRequestHandler {

    @Override
    public McpSchemaVO.JSONRPCResponse handle(McpSchemaVO.JSONRPCRequest message) {
        return null;
    }

}
