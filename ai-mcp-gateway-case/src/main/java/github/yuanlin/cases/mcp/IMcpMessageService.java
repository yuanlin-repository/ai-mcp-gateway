package github.yuanlin.cases.mcp;

import github.yuanlin.domain.session.model.entity.HandleMessageCommandEntity;
import org.springframework.http.ResponseEntity;

public interface IMcpMessageService {

    ResponseEntity<Void> handleMessage(HandleMessageCommandEntity commandEntity) throws Exception;

}
