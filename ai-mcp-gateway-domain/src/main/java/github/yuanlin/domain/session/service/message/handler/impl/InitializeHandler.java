package github.yuanlin.domain.session.service.message.handler.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import github.yuanlin.domain.session.adapter.repository.ISessionRepository;
import github.yuanlin.domain.session.model.valobj.McpSchemaVO;
import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import github.yuanlin.domain.session.service.message.handler.IRequestHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yuanlin.zhou
 * @date 2026/3/6 12:07
 * @description 协议握手，建立客户端与服务器的连接
 */
@Slf4j
@Service("initializeHandler")
public class InitializeHandler implements IRequestHandler {

    @Resource
    private ISessionRepository sessionRepository;

    /**
     * 对照 io.modelcontextprotocol.spec.McpServerSession
     * <br/>     * <br/>
     * McpServerSession.handle -> McpSchema.JSONRPCRequest -> handleIncomingRequest
     * -> McpSchema.METHOD_INITIALIZE -> McpAsyncServer.asyncInitializeRequestHandler
     * -> result -> new McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION, request.id(), result, null)
     * <br/>
     * {
     * "id": "a355a5f7-0",
     * "jsonrpc": "2.0",
     * "result": {
     * "capabilities": {
     * "completions": {},
     * "logging": {},
     * "prompts": {
     * "listChanged": true
     * },
     * "resources": {
     * "listChanged": true,
     * "subscribe": false
     * },
     * "tools": {
     * "listChanged": true
     * }
     * },
     * "instructions": "This server provides weather information tools and resources",
     * "protocolVersion": "2024-11-05",
     * "serverInfo": {
     * "name": "ai-mcp-gateway-demo-mcp-server-test",
     * "version": "1.0.0"
     * }
     * }
     * }
     */
    @Override
    public McpSchemaVO.JSONRPCResponse handle(String gatewayId, McpSchemaVO.JSONRPCRequest message) {
        log.info("消息处理服务-initialize gatewayId:{} request.params:{}", gatewayId, JSON.toJSONString(message.params()));
        McpSchemaVO.InitializeRequest initializeRequest = McpSchemaVO.unmarshalFrom(message.params(), new TypeReference<>() {
        });

        McpGatewayConfigVO mcpGatewayConfigVO = sessionRepository.queryMcpGatewayConfigByGatewayId(gatewayId);
        McpSchemaVO.InitializeResult initializeResult = new McpSchemaVO.InitializeResult(
                initializeRequest.protocolVersion(),
                new McpSchemaVO.ServerCapabilities(
                        new McpSchemaVO.ServerCapabilities.CompletionCapabilities(),
                        new HashMap<>(),
                        new McpSchemaVO.ServerCapabilities.LoggingCapabilities(),
                        new McpSchemaVO.ServerCapabilities.PromptCapabilities(true),
                        new McpSchemaVO.ServerCapabilities.ResourceCapabilities(false, true),
                        new McpSchemaVO.ServerCapabilities.ToolCapabilities(true)
                ),
                new McpSchemaVO.Implementation(mcpGatewayConfigVO.getGatewayName(), mcpGatewayConfigVO.getVersion()),
                mcpGatewayConfigVO.getGatewayDesc()
        );

        return new McpSchemaVO.JSONRPCResponse(McpSchemaVO.JSONRPC_VERSION, message.id(), initializeResult, null);
    }

}
