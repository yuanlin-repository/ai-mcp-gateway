package github.yuanlin.domain.session.service.impl;

import github.yuanlin.domain.session.model.valobj.SessionConfigVO;
import github.yuanlin.domain.session.service.ISessionManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yuanlin.zhou
 * @date 2026/3/5 18:17
 * @description TODO
 */
@Slf4j
@Service
public class SessionManagementService implements ISessionManagementService {

    // 存储 SessionId -> Session 的映射数据
    private Map<String, SessionConfigVO> activeSessions = new ConcurrentHashMap<>();
    private static final long SESSION_TIMEOUT_MINUTES = 30;

    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    public SessionManagementService() {
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredSessions, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public SessionConfigVO createSession(String gatewayId) {

        String sessionId = UUID.randomUUID().toString();

        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().multicast().onBackpressureBuffer();

        String messageEndpoint = "/" + gatewayId + "/mcp/message?sessionId=" + sessionId;
        sink.tryEmitNext(ServerSentEvent.<String>builder()
                .event("endpoint")
                .data(messageEndpoint)
                .build());

        SessionConfigVO sessionConfigVO = new SessionConfigVO(sessionId, sink);

        activeSessions.put(sessionId, sessionConfigVO);

        log.info("创建会话 gatewayId:{}", gatewayId);

        return sessionConfigVO;
    }

    @Override
    public void removeSession(String sessionId) {
        SessionConfigVO sessionConfigVO = activeSessions.remove(sessionId);
        if (null == sessionConfigVO) return;

        sessionConfigVO.markInactive();

        try {

        } catch (Exception e) {
            log.warn("关闭会话失败 sessionId:{}", sessionId, e);
        }
    }

    @Override
    public SessionConfigVO getSession(String sessionId) {
        if (null == sessionId || sessionId.isEmpty()) {
            return null;
        }
        SessionConfigVO sessionConfigVO = activeSessions.get(sessionId);

        if (null != sessionConfigVO && sessionConfigVO.isActive()) {
            sessionConfigVO.updateLastAccessed();
            return sessionConfigVO;
        }

        return null;
    }

    @Override
    public void cleanupExpiredSessions() {
        int cleanedCount = 0;

        for (Map.Entry<String, SessionConfigVO> entry : activeSessions.entrySet()) {
            SessionConfigVO sessionConfigVO = entry.getValue();
            if (!sessionConfigVO.isActive() || sessionConfigVO.isExpired(SESSION_TIMEOUT_MINUTES)) {
                removeSession(sessionConfigVO.getSessionId());
                cleanedCount++;
            }
        }

        log.info("清理了{}个过期会话", cleanedCount);
    }

    @Override
    public void shutdown() {
        for (String sessionId : activeSessions.keySet()) {
            removeSession(sessionId);
        }

         cleanupScheduler.shutdown();

        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdown();
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdown();
            Thread.currentThread().interrupt();
        }
    }
}
