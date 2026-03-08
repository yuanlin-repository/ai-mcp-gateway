package github.yuanlin.infrastructure.dao;

import github.yuanlin.infrastructure.dao.po.McpGatewayToolPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMcpGatewayToolDao {

    int insert(McpGatewayToolPO po);

    int deleteById(Long id);

    int updateById(McpGatewayToolPO po);

    McpGatewayToolPO queryById(Long id);

    List<McpGatewayToolPO> queryAll();

    List<McpGatewayToolPO> queryMcpGatewayToolByGatewayId(String gatewayId);

    McpGatewayToolPO queryMcpGatewayToolByGatewayIdAndToolName(@Param("gatewayId") String gatewayId,
                                                               @Param("toolName") String toolName);
}

