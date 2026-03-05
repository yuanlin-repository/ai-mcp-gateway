package github.yuanlin.domain.session.service;

import github.yuanlin.domain.session.model.valobj.SessionConfigVO;

public interface ISessionManagementService {

    SessionConfigVO createSession(String gatewayId);

    void removeSession(String sessionId);

    SessionConfigVO getSession(String sessionId);

    void cleanupExpiredSessions();

    void shutdown();

}
