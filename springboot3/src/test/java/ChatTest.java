import com.ai.ai.service.impl.ChatServiceImpl;
import com.ai.aiEasyApplication;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = aiEasyApplication.class)
public class ChatTest {

    @Autowired
    ChatServiceImpl chatService;

    @Test
    void chat(){
        UserMessage userMessage = UserMessage.from(
                TextContent.from("你能帮助我解决哪些编程问题？")
        );
        String chat = chatService.chat(userMessage);
        System.out.println(chat);
    }
}
