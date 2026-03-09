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

    private String protocolType;

    private HTTPConfig httpConfig;

    private KafkaConfig kafkaConfig;

    @Data
    public static class HTTPConfig {
        private String httpUrl;
        private String httpHeaders;
        private String httpMethod;
        private Integer timeout;
    }

    @Data
    public static class KafkaConfig {
        private String bootstrapServers;
        private String topic;
        private String keySerializer;
        private String valueSerializer;
        private String acks;
        private Integer retries;
        private String headers;
        private Integer batchSize;
        private Integer lingerMs;
        private Integer bufferMemory;
    }
}
