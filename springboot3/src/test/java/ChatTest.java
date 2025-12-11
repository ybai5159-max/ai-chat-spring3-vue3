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
        String msg = chatService.chat("如何在Java中创建一个线程安全的单例模式？");
        System.out.println(msg);
    }
}
