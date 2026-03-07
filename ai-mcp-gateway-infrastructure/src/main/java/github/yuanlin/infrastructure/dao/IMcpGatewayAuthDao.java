package github.yuanlin.infrastructure.dao;

import github.yuanlin.infrastructure.dao.po.McpGatewayAuthPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IMcpGatewayAuthDao {

    int insert(McpGatewayAuthPO po);

    int deleteById(Long id);

    int updateById(McpGatewayAuthPO po);

    McpGatewayAuthPO queryById(Long id);

    List<McpGatewayAuthPO> queryAll();
}

