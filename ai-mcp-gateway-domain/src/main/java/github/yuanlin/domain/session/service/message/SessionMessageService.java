package github.yuanlin.domain.session.service.message;

import com.alibaba.fastjson.JSON;
import github.yuanlin.domain.session.model.entity.HandleMessageCommandEntity;
import github.yuanlin.domain.session.model.valobj.McpSchemaVO;
import github.yuanlin.domain.session.model.valobj.enums.SessionMessageHandlerMethodEnum;
import github.yuanlin.domain.session.service.ISessionMessageService;
import github.yuanlin.domain.session.service.message.handler.IRequestHandler;
import github.yuanlin.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static github.yuanlin.types.enums.ResponseCode.METHOD_NOT_FOUND;

/**
 * @author yuanlin.zhou
 * @date 2026/3/6 12:10
 * @description TODO
 */
@Slf4j
@Service
public class SessionMessageService implements ISessionMessageService {

    @Resource
    Map<String, IRequestHandler> requestHandlerMap = new HashMap<>();

    @Override
    public McpSchemaVO.JSONRPCResponse processHandlerMessage(String gatewayId, McpSchemaVO.JSONRPCMessage message) {
        if (message instanceof McpSchemaVO.JSONRPCResponse response) {
            log.info("收到结果消息");
        }

        if (message instanceof McpSchemaVO.JSONRPCRequest request) {
            String method = request.method();
            log.info("开始处理请求，方法: {}", method);

            SessionMessageHandlerMethodEnum sessionMessageHandlerMethodEnum = SessionMessageHandlerMethodEnum.getByMethod(method);
            if (null == sessionMessageHandlerMethodEnum) {
                throw new AppException(METHOD_NOT_FOUND.getCode(), METHOD_NOT_FOUND.getInfo());
            }

            String handlerName = sessionMessageHandlerMethodEnum.getHandlerName();
            IRequestHandler requestHandler = requestHandlerMap.get(handlerName);

            if (null == requestHandler) {
                throw new AppException(METHOD_NOT_FOUND.getCode(), METHOD_NOT_FOUND.getInfo());
            }

            return requestHandler.handle(gatewayId, request);
        }

        if (message instanceof McpSchemaVO.JSONRPCNotification notification) {
            log.info("收到即将处理的通知 {} {}", notification.method(), JSON.toJSONString(notification.params()));
        }
        return null;
    }

    @Override
    public McpSchemaVO.JSONRPCResponse processHandlerMessage(HandleMessageCommandEntity commandEntity) {
        return processHandlerMessage(commandEntity.getGatewayId(), commandEntity.getJsonrpcMessage());
    }
}
