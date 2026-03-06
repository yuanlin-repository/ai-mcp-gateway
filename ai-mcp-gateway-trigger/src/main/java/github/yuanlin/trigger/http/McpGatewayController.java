package github.yuanlin.trigger.http;

import com.alibaba.fastjson.JSON;
import github.yuanlin.api.IMcpGatewayService;
import github.yuanlin.cases.mcp.IMcpSessionService;
import github.yuanlin.domain.session.model.valobj.McpSchemaVO;
import github.yuanlin.domain.session.service.ISessionMessageService;
import github.yuanlin.types.enums.ResponseCode;
import github.yuanlin.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * MCP 网关服务接口管理
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/12/13 08:54
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/")
public class McpGatewayController implements IMcpGatewayService {

    @Resource
    private IMcpSessionService mcpSessionService;

    @Resource
    private ISessionMessageService serviceMessageService;

    public McpGatewayController() {
        System.out.println("xxxx");
    }

    /**
     * 建立sse连接，创建会话
     * <br/>
         * <a href="http://localhost:8777/api-gateway/test10001/mcp/sse">http://localhost:8777/api-gateway/test10001/mcp/sse</a>
     *
     * @param gatewayId 网关ID
     */
    @GetMapping(value = "{gatewayId}/mcp/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public Flux<ServerSentEvent<String>> establishSSEConnection(@PathVariable("gatewayId") String gatewayId) throws Exception {
        try {
            log.info("建立 MCP SSE 连接，gatewayId:{}", gatewayId);
            if (StringUtils.isBlank(gatewayId)) {
                log.info("非法参数，gateway is null");
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }

            return mcpSessionService.createMcpSession(gatewayId);
        } catch (Exception e) {
            log.error("建立 MCP SSE 连接失败，gatewayId: {}", gatewayId, e);
            throw e;
        }
    }

    /*
        {
            "jsonrpc": "2.0",
            "method": "initialize",
            "id": "e034b37b-0",
            "params": {
                "protocolVersion": "2024-11-05",
                "capabilities": {},
                "clientInfo": {
                    "name": "Java SDK MCP Client",
                    "version": "1.0.0"
                }
            }
        }
     */
    @PostMapping(value = "{gatewayId}/mcp/sse", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Mono<ResponseEntity<Object>> handleMessage(@PathVariable("gatewayId") String gatewayId,
                                                      @RequestParam String sessionId,
                                                      @RequestBody String messageBody) {

        try {
            log.info("处理 MCP SSE 消息，gatewayId:{} sessionId:{} messageBody:{}", gatewayId, sessionId, messageBody);

            McpSchemaVO.JSONRPCMessage jsonrpcMessage = McpSchemaVO.deserializeJsonRpcMessage(messageBody);
            log.info("序列化消息:{}", jsonrpcMessage.jsonrpc());

            McpSchemaVO.JSONRPCResponse jsonrpcResponse = serviceMessageService.processHandlerMessage((McpSchemaVO.JSONRPCRequest) jsonrpcMessage);

            log.info("调用结果:{}", JSON.toJSONString(jsonrpcResponse));

            return Mono.just(ResponseEntity.ok(Map.of("status", "sent via SSE")));
        } catch (Exception e) {
            log.info("处理 MCP SSE 消息失败，gatewayId:{} sessionId:{} messageBody:{}", gatewayId, sessionId, messageBody, e);
            return Mono.empty();
        }
    }
}

