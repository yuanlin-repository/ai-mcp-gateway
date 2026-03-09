package github.yuanlin.infrastructure.adapter.repository;

import github.yuanlin.domain.session.adapter.repository.ISessionRepository;
import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayConfigVO;
import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayProtocolConfigVO;
import github.yuanlin.domain.session.model.valobj.gateway.McpGatewayToolConfigVO;
import github.yuanlin.infrastructure.dao.*;
import github.yuanlin.infrastructure.dao.po.*;
import github.yuanlin.types.exception.AppException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static github.yuanlin.types.enums.ResponseCode.ILLEGAL_PARAMETER;

/**
 * @author yuanlin.zhou
 * @date 2026/3/7 15:43
 * @description 会话仓储服务
 */
@Repository
public class SessionRepository implements ISessionRepository {

    @Resource
    private IMcpGatewayDao mcpGatewayDao;

//    @Resource
//    private IMcpProtocolRegistryDao mcpProtocolRegistryDao;

    @Resource
    private IMcpGatewayToolDao mcpGatewayToolDao;

    @Resource
    private IMcpProtocolHttpDao mcpProtocolHttpDao;

    @Resource
    private IMcpProtocolKafkaDao mcpProtocolKafkaDao;

    @Resource
    private IMcpProtocolMappingDao mcpProtocolMappingDao;

    @Override
    public McpGatewayConfigVO queryMcpGatewayConfigByGatewayId(String gatewayId) {
        // 1. 查询网关配置
        McpGatewayPO mcpGatewayPO = mcpGatewayDao.queryMcpGatewayByGatewayId(gatewayId);
        if (null == mcpGatewayPO) return null;

        return McpGatewayConfigVO.builder()
                .gatewayId(mcpGatewayPO.getGatewayId())
                .gatewayName(mcpGatewayPO.getGatewayName())
                .gatewayDesc(mcpGatewayPO.getGatewayDesc())
                .version(mcpGatewayPO.getVersion())
                .build();
    }

    @Override
    public List<McpGatewayToolConfigVO> queryMcpGatewayToolConfigListByGatewayId(String gatewayId) {

        // 1.查询 gateway 的所有工具
        List<McpGatewayToolPO> mcpGatewayToolPOS = mcpGatewayToolDao.queryMcpGatewayToolByGatewayId(gatewayId);

        // 2.根据不同的协议分组为列表
        Map<String, List<McpGatewayToolPO>> toolMap = mcpGatewayToolPOS.stream().
                collect(Collectors.groupingBy(McpGatewayToolPO::getProtocolType));

        // 3.结果列表
        List<McpGatewayToolConfigVO> mcpGatewayToolConfigVOS = new ArrayList<>();

        for (Map.Entry<String, List<McpGatewayToolPO>> entry : toolMap.entrySet()) {
            String protocolType = entry.getKey().toLowerCase();
            List<McpGatewayToolPO> toolPOS = entry.getValue();

            switch (protocolType) {
                case "http": {
                    for (McpGatewayToolPO mcpGatewayToolPO : toolPOS) {
                        // 查询每个 tool 的 http 协议映射
                        McpProtocolMappingPO reqPo = new McpProtocolMappingPO();
                        reqPo.setProtocolId(mcpGatewayToolPO.getProtocolId());
                        List<McpProtocolMappingPO> poList = mcpProtocolMappingDao.queryMcpGatewayToolConfigList(reqPo);

                        // 构造 tool 的协议映射
                        List<McpGatewayToolConfigVO.MappingConfig> mappingConfigs = new ArrayList<>();
                        for (McpProtocolMappingPO po : poList) {
                            mappingConfigs.add(McpGatewayToolConfigVO.MappingConfig.builder()
                                    .mappingType(po.getMappingType())
                                    .parentPath(po.getParentPath())
                                    .fieldName(po.getFieldName())
                                    .mcpPath(po.getMcpPath())
                                    .mcpType(po.getMcpType())
                                    .mcpDesc(po.getMcpDesc())
                                    .isRequired(po.getIsRequired())
                                    .sortOrder(po.getSortOrder())
                                    .build());
                        }
                        mcpGatewayToolConfigVOS.add(McpGatewayToolConfigVO.builder()
                                .toolId(mcpGatewayToolPO.getToolId())
                                .toolName(mcpGatewayToolPO.getToolName())
                                .toolDescription(mcpGatewayToolPO.getToolDescription())
                                .toolType(mcpGatewayToolPO.getToolType())
                                .toolVersion(mcpGatewayToolPO.getToolVersion())
                                .mappingConfigs(mappingConfigs)
                                .build());
                    }
                    break;
                }
                default: {
                    throw new AppException(ILLEGAL_PARAMETER.getCode(), ILLEGAL_PARAMETER.getInfo());
                }
            }
        }

        return mcpGatewayToolConfigVOS;
    }

    @Override
    public McpGatewayProtocolConfigVO queryMcpGatewayProtocolConfig(String gatewayId, String toolName) {

        // 查询 protocol_id
        McpGatewayToolPO mcpGatewayToolPO = mcpGatewayToolDao.queryMcpGatewayToolByGatewayIdAndToolName(gatewayId, toolName);
        if (null == mcpGatewayToolPO) {
            return null;
        }
        switch (mcpGatewayToolPO.getProtocolType()) {
            case "http": {
                McpProtocolHttpPO mcpProtocolHttpPO = mcpProtocolHttpDao.queryByProtocolId(mcpGatewayToolPO.getProtocolId());
                McpGatewayProtocolConfigVO.HTTPConfig httpConfig = new McpGatewayProtocolConfigVO.HTTPConfig();
                httpConfig.setHttpUrl(mcpProtocolHttpPO.getHttpUrl());
                httpConfig.setHttpHeaders(mcpProtocolHttpPO.getHttpHeaders());
                httpConfig.setHttpMethod(mcpProtocolHttpPO.getHttpMethod());
                httpConfig.setTimeout(mcpProtocolHttpPO.getTimeout());

                return McpGatewayProtocolConfigVO.builder()
                        .protocolType("http")
                        .httpConfig(httpConfig)
                        .build();
            }
            case "kafka": {
                McpProtocolKafkaPO mcpProtocolKafkaPO = mcpProtocolKafkaDao.queryByProtocolId(mcpGatewayToolPO.getProtocolId());
                McpGatewayProtocolConfigVO.KafkaConfig kafkaConfig = new McpGatewayProtocolConfigVO.KafkaConfig();
                kafkaConfig.setBootstrapServers(mcpProtocolKafkaPO.getBootstrapServers());
                kafkaConfig.setTopic(mcpProtocolKafkaPO.getTopic());
                kafkaConfig.setKeySerializer(mcpProtocolKafkaPO.getKeySerializer());
                kafkaConfig.setValueSerializer(mcpProtocolKafkaPO.getValueSerializer());
                kafkaConfig.setAcks(mcpProtocolKafkaPO.getAcks());
                kafkaConfig.setRetries(mcpProtocolKafkaPO.getRetries());
                kafkaConfig.setHeaders(mcpProtocolKafkaPO.getHeaders());
                kafkaConfig.setBatchSize(mcpProtocolKafkaPO.getBatchSize());
                kafkaConfig.setLingerMs(mcpProtocolKafkaPO.getLingerMs());
                kafkaConfig.setBufferMemory(mcpProtocolKafkaPO.getBufferMemory());

                return McpGatewayProtocolConfigVO.builder()
                        .protocolType("kafka")
                        .kafkaConfig(kafkaConfig)
                        .build();
            }
            default:
                throw new AppException(ILLEGAL_PARAMETER.getCode(), ILLEGAL_PARAMETER.getInfo());
        }

    }

}
