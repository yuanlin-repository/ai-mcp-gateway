package github.yuanlin.infrastructure.dao;

import github.yuanlin.infrastructure.dao.po.McpGatewayPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IMcpGatewayDao {

    int insert(McpGatewayPO po);

    int deleteById(Long id);

    int updateById(McpGatewayPO po);

    McpGatewayPO queryById(Long id);

    List<McpGatewayPO> queryAll();
}

