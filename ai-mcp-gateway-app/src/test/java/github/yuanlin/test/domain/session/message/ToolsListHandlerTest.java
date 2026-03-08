package github.yuanlin.test.domain.session.message;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import github.yuanlin.domain.session.model.valobj.McpSchemaVO;
import github.yuanlin.domain.session.service.message.handler.impl.ToolsCallHandler;
import github.yuanlin.domain.session.service.message.handler.impl.ToolsListHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author yuanlin.zhou
 * @date 2026/3/8 18:05
 * @description TODO
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ToolsListHandlerTest {

    @Resource
    private ToolsListHandler toolsListHandler;

    @Test
    public void test_toollist() throws JsonProcessingException {
        McpSchemaVO.JSONRPCResponse handle = toolsListHandler.
                handle("gateway_001",
                        new McpSchemaVO.JSONRPCRequest("2.0","tool/list","a355a5f7-0",""));
        log.info("测试结果:{}", JSON.toJSONString(handle.result()));
    }
}
