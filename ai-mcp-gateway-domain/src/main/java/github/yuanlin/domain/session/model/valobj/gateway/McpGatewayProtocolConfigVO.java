package github.yuanlin.domain.session.model.valobj.gateway;

import lombok.*;

/**
 * @author yuanlin.zhou
 * @date 2026/3/8 12:38
 * @description TODO
 */

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class McpGatewayProtocolConfigVO {

    private HTTPConfig httpConfig;

    @Data
    public static class HTTPConfig {
        private String httpUrl;
        private String httpHeaders;
        private String httpMethod;
        private Integer timeout;
    }
}
