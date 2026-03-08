package github.yuanlin.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * MCP网关工具表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpGatewayToolPO {

    /**
     * 自增ID
     */
    private Long id;
    /**
     * 网关ID
     */
    private String gatewayId;
    /**
     * 工具ID
     */
    private Long toolId;
    /**
     * MCP工具名称（如：JavaSDKMCPClient_getCompanyEmployee）
     */
    private String toolName;
    /**
     * 工具类型：function/resource
     */
    private String toolType;
    /**
     * 工具描述
     */
    private String toolDescription;
    /**
     * 工具版本
     */
    private String toolVersion;
    /**
     * 协议ID
     */
    private Long protocolId;
    /**
     * 协议类型；http、dubbo、rabbitmq
     */
    private String protocolType;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
}

