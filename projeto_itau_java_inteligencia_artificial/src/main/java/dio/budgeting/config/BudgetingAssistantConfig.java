package dio.budgeting.config;

import dio.budgeting.assistant.BudgetingAssistant;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class BudgetingAssistantConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, BudgetingAssistant assistant) throws IOException {
        String systemMessage = new ClassPathResource("templates/system-message.st")
                .getContentAsString(StandardCharsets.UTF_8);
        return builder
                .defaultSystem(systemMessage)
                .defaultTools(assistant)
                .build();
    }
}
