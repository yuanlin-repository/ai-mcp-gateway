package github.yuanlin.infrastructure.dao;

import github.yuanlin.infrastructure.dao.po.McpProtocolKafkaPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IMcpProtocolKafkaDao {

    int insert(McpProtocolKafkaPO po);

    int deleteById(Long id);

    int updateById(McpProtocolKafkaPO po);

    McpProtocolKafkaPO queryById(Long id);

    List<McpProtocolKafkaPO> queryAll();

    McpProtocolKafkaPO queryByProtocolId(Long protocolId);
}
