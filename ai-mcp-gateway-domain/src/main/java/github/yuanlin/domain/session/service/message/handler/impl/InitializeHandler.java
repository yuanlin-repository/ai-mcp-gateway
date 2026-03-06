package github.yuanlin.domain.session.service.message.handler.impl;

import github.yuanlin.domain.session.model.valobj.McpSchemaVO;
import github.yuanlin.domain.session.service.message.handler.IRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author yuanlin.zhou
 * @date 2026/3/6 12:07
 * @description 协议握手，建立客户端与服务器的连接
 */
@Slf4j
@Service("initializeHandler")
public class InitializeHandler implements IRequestHandler {

    @Override
    public McpSchemaVO.JSONRPCResponse handle(McpSchemaVO.JSONRPCRequest message) {

        log.info("模拟处理初始化请求");

        return new McpSchemaVO.JSONRPCResponse("2.0", message.id(), Map.of(
                "protocolVersion", "2024-11-05",
                "capabilities", Map.of(
                        "tools", Map.of(),
                        "resources", Map.of()
                ),
                "serverInfo", Map.of(
                        "name", "MCP Weather Proxy Server",
                        "version", "1.0.0"
                )
        ), null);

    }

}
