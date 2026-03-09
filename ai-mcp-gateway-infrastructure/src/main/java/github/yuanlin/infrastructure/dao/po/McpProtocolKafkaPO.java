package github.yuanlin.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * MCP工具Kafka协议表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpProtocolKafkaPO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 协议ID
     */
    private Long protocolId;

    /**
     * Kafka集群地址
     */
    private String bootstrapServers;

    /**
     * 目标Topic
     */
    private String topic;

    /**
     * Key序列化器
     */
    private String keySerializer;

    /**
     * Value序列化器
     */
    private String valueSerializer;

    /**
     * 确认机制：0/1/all
     */
    private String acks;

    /**
     * 重试次数
     */
    private Integer retries;

    /**
     * 批处理大小
     */
    private Integer batchSize;

    /**
     * 批次等待时间（毫秒）
     */
    private Integer lingerMs;

    /**
     * 缓冲区大小
     */
    private Integer bufferMemory;

    /**
     * 消息头（JSON格式）
     */
    private String headers;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
