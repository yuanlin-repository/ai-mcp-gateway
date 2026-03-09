package github.yuanlin.infrastructure.adapter.port;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import github.yuanlin.domain.session.adapter.port.ISessionPort;
import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayProtocolConfigVO;
import github.yuanlin.infrastructure.gateway.GenericHttpGateway;
import github.yuanlin.types.enums.ResponseCode;
import github.yuanlin.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yuanlin.zhou
 * @date 2026/3/8 12:57
 * @description TODO
 */
@Slf4j
@Component
public class SessionPort implements ISessionPort {

    @Resource
    private GenericHttpGateway gateway;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ConcurrentHashMap<String, KafkaProducer<String, String>> producerPool = new ConcurrentHashMap<>();

    @Override
    public Object toolCall(McpGatewayProtocolConfigVO.HTTPConfig httpConfig, Object params) throws IOException {

        // 1.构建请求头
        String httpHeadersJson = httpConfig.getHttpHeaders();


        Map<String, Object> headers = objectMapper.readValue(httpHeadersJson, Map.class);

        // 2.判断请求方法
        String httpMethod = httpConfig.getHttpMethod().toLowerCase();

        // 3.参数校验
        if (!(params instanceof Map<?, ?> arguments)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        switch (httpMethod) {
            case "post": {
                RequestBody body = RequestBody.create(JSON.toJSONString(arguments.values().toArray()[0]),
                        MediaType.parse("application/json"));
                Call<ResponseBody> call = gateway.post(httpConfig.getHttpUrl(), headers, body);
                ResponseBody responseBody = call.execute().body();

                assert responseBody != null;

                return responseBody.string();
            }

            case "get": {
                Map<String, Object> objMapRequest = new java.util.HashMap<>((Map<String, Object>) arguments.values().toArray()[0]);

                String url = httpConfig.getHttpUrl();
                // 替换路径参数
                Matcher matcher = Pattern.compile("\\{([^}]+)\\}").matcher(url);
                while (matcher.find()) {
                    String name = matcher.group(1);
                    if (objMapRequest.containsKey(name)) {
                        url = url.replace("{" + name + "}", String.valueOf(objMapRequest.get(name)));
                        objMapRequest.remove(name);
                    }
                }

                Call<ResponseBody> call = gateway.get(url, headers, objMapRequest);

                ResponseBody responseBody = call.execute().body();

                assert responseBody != null;

                return responseBody.string();
            }
        }

        throw new AppException(ResponseCode.METHOD_NOT_FOUND.getCode(), ResponseCode.METHOD_NOT_FOUND.getInfo());
    }

    @Override
    public Object toolCall(McpGatewayProtocolConfigVO.KafkaConfig kafkaConfig, Object params) {
        if (!(params instanceof Map<?, ?> arguments)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        try {
            Object payload = arguments.values().toArray()[0];
            String messageValue = JSON.toJSONString(payload);

            String bootstrapServers = kafkaConfig.getBootstrapServers();
            KafkaProducer<String, String> producer = getOrCreateProducer(bootstrapServers, kafkaConfig);

            ProducerRecord<String, String> record = new ProducerRecord<>(
                    kafkaConfig.getTopic(),
                    messageValue
            );

            Future<RecordMetadata> future = producer.send(record);

            RecordMetadata metadata = future.get(30, TimeUnit.SECONDS);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("topic", metadata.topic());
            result.put("partition", metadata.partition());
            result.put("offset", metadata.offset());
            result.put("timestamp", metadata.timestamp());
            result.put("message", "消息已发送到Kafka队列");
            result.put("bootstrapServers", bootstrapServers);

            return result;
        } catch (Exception e) {
            throw new AppException(ResponseCode.KAFKA_SEND_FAILED.getCode(),
                    "Kafka消息发送失败: " + e.getMessage());
        }
    }

    private KafkaProducer<String, String> getOrCreateProducer(String bootstrapServers, McpGatewayProtocolConfigVO.KafkaConfig kafkaConfig) {
        return producerPool.computeIfAbsent(bootstrapServers, key -> createProducer(kafkaConfig));
    }

    private KafkaProducer<String, String> createProducer(McpGatewayProtocolConfigVO.KafkaConfig kafkaConfig) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaConfig.getKeySerializer() != null ? 
                kafkaConfig.getKeySerializer() : StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaConfig.getValueSerializer() != null ? 
                kafkaConfig.getValueSerializer() : StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, kafkaConfig.getAcks() != null ? kafkaConfig.getAcks() : "1");
        props.put(ProducerConfig.RETRIES_CONFIG, kafkaConfig.getRetries() != null ? kafkaConfig.getRetries() : 0);
        
        if (kafkaConfig.getBatchSize() != null) {
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaConfig.getBatchSize());
        }
        if (kafkaConfig.getLingerMs() != null) {
            props.put(ProducerConfig.LINGER_MS_CONFIG, kafkaConfig.getLingerMs());
        }
        if (kafkaConfig.getBufferMemory() != null) {
            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafkaConfig.getBufferMemory());
        }

        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30000);

        log.info("Creating new KafkaProducer for bootstrapServers: {}", kafkaConfig.getBootstrapServers());
        
        return new KafkaProducer<>(props);
    }
}
