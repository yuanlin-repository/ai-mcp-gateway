package github.yuanlin.domain.session.service.message.handler.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import github.yuanlin.domain.session.adapter.port.ISessionPort;
import github.yuanlin.domain.session.adapter.repository.ISessionRepository;
import github.yuanlin.domain.session.model.valobj.McpSchemaVO;
import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayProtocolConfigVO;
import github.yuanlin.domain.session.service.message.handler.IRequestHandler;
import github.yuanlin.types.enums.McpErrorCodes;
import github.yuanlin.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * 执行指定的工具调用
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/12/20 11:30
 */
@Slf4j
@Service("toolsCallHandler")
public class ToolsCallHandler implements IRequestHandler {

    @Resource
    private ISessionRepository repository;

    @Resource
    private ISessionPort port;

    @Override
    public McpSchemaVO.JSONRPCResponse handle(String gatewayId, McpSchemaVO.JSONRPCRequest message) {
        try {
            McpGatewayProtocolConfigVO mcpGatewayProtocolConfigVO = repository.queryMcpGatewayProtocolConfig(gatewayId);

            McpSchemaVO.CallToolRequest callToolRequest = McpSchemaVO.unmarshalFrom(message.params(), new TypeReference<>() {
            });

            Map<String, Object> argumentsObj = callToolRequest.arguments();

            String name = callToolRequest.name();
            Object result = port.toolCall(mcpGatewayProtocolConfigVO.getHttpConfig(), argumentsObj);

            return new McpSchemaVO.JSONRPCResponse(McpSchemaVO.JSONRPC_VERSION, message.id(),
                    Map.of("content", new Object[]{
                                    Map.of(
                                            "type", "text",
                                            "text", result
                                    ),
                            },
                            "isError", "false"
                    ), null);
        } catch (IOException e) {
            return new McpSchemaVO.JSONRPCResponse(McpSchemaVO.JSONRPC_VERSION,
                    message.id(),
                    null,
                    new McpSchemaVO.JSONRPCResponse.JSONRPCError(McpErrorCodes.METHOD_NOT_FOUND, "方法未找到 - 方法不存在或不可用", null));
        }
    }

}
