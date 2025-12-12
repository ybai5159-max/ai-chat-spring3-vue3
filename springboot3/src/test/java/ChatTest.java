import com.ai.ai.Service.ChatService;
import com.ai.aiEasyApplication;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = aiEasyApplication.class)
public class ChatTest {

    @Autowired
    private ChatService chatService;

    @Test
    void chat(){
        String msg = chatService.chat("我想在java和python之间选择java？");
        System.out.println(msg);
        String newmsg = chatService.chat("我之前在java和python中选择了哪个？");
        System.out.println(newmsg);
    }
}
