package github.yuanlin.domain.session.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author yuanlin.zhou
 * @date 2026/3/5 18:03
 * @description 会话配置对象
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionConfigVO {

    private String sessionId;

    private Sinks.Many<ServerSentEvent<String>> sink;

    private Instant createTime;

    private volatile Instant lastAccessedTime;

    private volatile boolean active;

    public SessionConfigVO(String sessionId, Sinks.Many<ServerSentEvent<String>> sink) {
        this.sessionId = sessionId;
        this.sink = sink;
        this.createTime = Instant.now();
        this.lastAccessedTime = Instant.now();
        this.active = true;
    }

    public void markInactive() {
        this.active = false;
    }

    public void updateLastAccessed() {
        this.lastAccessedTime = Instant.now();
    }

    public boolean isExpired(long timeoutMinutes) {
        return lastAccessedTime.isBefore(Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES));
    }
}
