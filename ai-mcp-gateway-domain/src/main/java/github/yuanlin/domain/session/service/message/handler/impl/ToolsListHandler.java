package github.yuanlin.domain.session.service.message.handler.impl;

import github.yuanlin.domain.session.adapter.repository.ISessionRepository;
import github.yuanlin.domain.session.model.valobj.McpSchemaVO;
import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayToolConfigVO;
import github.yuanlin.domain.session.service.message.handler.IRequestHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 返回服务器支持的工具列表
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/12/20 11:29
 */
@Slf4j
@Service("toolsListHandler")
public class ToolsListHandler implements IRequestHandler {

    @Resource
    private ISessionRepository sessionRepository;

    @Override
    public McpSchemaVO.JSONRPCResponse handle(String gatewayId, McpSchemaVO.JSONRPCRequest message) {
        // 1. 网关配置
        McpGatewayConfigVO mcpGatewayConfigVO = sessionRepository.queryMcpGatewayConfigByGatewayId(gatewayId);
        // 2. 查询网关（gatewayId）下的工具列表配置
        List<McpGatewayToolConfigVO> mcpGatewayToolConfigVOS = sessionRepository.queryMcpGatewayToolConfigListByGatewayId(gatewayId);
        // 3. 构建工具列表
        List<McpSchemaVO.Tool> tools = buildTools(mcpGatewayConfigVO, mcpGatewayToolConfigVOS);


        return new McpSchemaVO.JSONRPCResponse("2.0", message.id(), Map.of("tools", tools), null);
    }

    private List<McpSchemaVO.Tool> buildTools(McpGatewayConfigVO gatewayConfig, List<McpGatewayToolConfigVO> toolConfigs) {
        Map<Long, List<McpGatewayToolConfigVO>> toolsMap = toolConfigs.stream()
                .collect(Collectors.groupingBy(McpGatewayToolConfigVO::getToolId));

        List<McpSchemaVO.Tool> tools = new ArrayList<>();

        for (Map.Entry<Long, List<McpGatewayToolConfigVO>> entry : toolsMap.entrySet()) {
            Long toolId = entry.getKey();
            List<McpGatewayToolConfigVO> configs = entry.getValue();

            List<McpGatewayToolConfigVO> roots = new ArrayList<>();

            configs.sort((o1, o2) -> {
                int s1 = o1.getSortOrder() != null ? o1.getSortOrder() : 0;
                int s2 = o2.getSortOrder() != null ? o2.getSortOrder() : 0;
                return Integer.compare(s1, s2);
            });

            // 1.生成 parent -> List<child>
            // 1.拿到根节点列表
            Map<String, List<McpGatewayToolConfigVO>> childrenMap = new HashMap<>();
            for (McpGatewayToolConfigVO config : configs) {
                if (config.getParentPath() == null) {
                    roots.add(config);
                } else {
                    childrenMap.computeIfAbsent(config.getParentPath(), k -> new ArrayList()).add(config);
                }
            }


            // 2.遍历每个根节点, 递归构建根节点的 properties
            List<String> required = new ArrayList<>();
            Map<String, Object> properties = new LinkedHashMap<>();
            for (McpGatewayToolConfigVO root : roots) {
                properties.put(root.getFieldName(), buildProperty(root, childrenMap));
                if (Integer.valueOf(1).equals(root.getIsRequired())) {
                    required.add(root.getFieldName());
                }
            }

            // 获取类型
            String type = roots.size() == 1 ? roots.get(0).getMcpType() : "object";

            // 构造函数
            McpSchemaVO.JsonSchema inputSchema = new McpSchemaVO.JsonSchema(
                    type,
                    properties,
                    required.isEmpty() ? null : required,
                    false,
                    null,
                    null
            );

            // 工具描述
            String name = "unknown-tool-" + toolId;
            String desc = "";
            if (gatewayConfig != null && Objects.equals(gatewayConfig.getToolId(), toolId)) {
                name = gatewayConfig.getToolName();
                desc = gatewayConfig.getToolDesc();
            }

            tools.add(new McpSchemaVO.Tool(name, desc, inputSchema));
        }

        return tools;
    }

    private Object buildProperty(McpGatewayToolConfigVO current, Map<String, List<McpGatewayToolConfigVO>> childrenMap) {
        Map<String, Object> property = new HashMap<>();
        property.put("type", current.getMcpType());
        if (null != current.getMcpDesc()) {
            property.put("description", current.getMcpDesc());
        }
        // 1.生成 parent -> List<child>
        // 1.拿到根节点列表

        List<McpGatewayToolConfigVO> childrenList = childrenMap.get(current.getMcpPath());

        if (childrenList != null && !childrenList.isEmpty()) {
            List<String> required = new ArrayList<>();
            Map<String, Object> properties = new LinkedHashMap<>();

            childrenList.sort((o1, o2) -> {
                int s1 = o1.getSortOrder() != null ? o1.getSortOrder() : 0;
                int s2 = o2.getSortOrder() != null ? o2.getSortOrder() : 0;
                return Integer.compare(s1, s2);
            });

            // 2.遍历每个根节点, 递归构建节点的 properties
            for (McpGatewayToolConfigVO children : childrenList) {
                properties.put(children.getFieldName(), buildProperty(children, childrenMap));
                if (Integer.valueOf(1).equals(children.getIsRequired())) {
                    required.add(children.getFieldName());
                }
            }

            property.put("properties", properties);
            if (!required.isEmpty()) {
                property.put("required", required);
            }
        }

        return property;
    }

}
