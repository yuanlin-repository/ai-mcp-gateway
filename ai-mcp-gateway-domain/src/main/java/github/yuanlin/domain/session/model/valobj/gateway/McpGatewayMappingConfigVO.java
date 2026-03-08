package github.yuanlin.domain.session.model.valobj.gateway;

/**
 * @author yuanlin.zhou
 * @date 2026/3/8 16:22
 * @description TODO
 */
public class McpGatewayMappingConfigVO {

    /**
     * 协议 id
     */
    private String protocolId;
    /**
     * 映射类型：request-请求参数映射，response-响应数据映射
     */
    private String mappingType;
    /**
     * 父级路径（如：xxxRequest01，用于构建嵌套结构，根节点为NULL）
     */
    private String parentPath;
    /**
     * 字段名称（如：city、company、name）
     */
    private String fieldName;
    /**
     * MCP完整路径（如：xxxRequest01.city、xxxRequest01.company.name）
     */
    private String mcpPath;
    /**
     * MCP数据类型：string/number/boolean/object/array
     */
    private String mcpType;
    /**
     * MCP字段描述
     */
    private String mcpDesc;
    /**
     * 是否必填：0-否，1-是（用于生成required数组）
     */
    private Integer isRequired;
    /**
     * 排序顺序（同级字段排序）
     */
    private Integer sortOrder;
}
