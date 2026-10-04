package dio.budgeting.controller;

import dio.budgeting.assistant.BudgetingAssistant;
import dio.budgeting.assistant.OpenAiAudioClient;
import dio.budgeting.config.BudgetingAssistantConfig;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.usecase.*;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
class BudgetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatClient chatClient;
    @MockBean
    private OpenAiAudioClient audioClient;
    @MockBean
    private PersistTransactionUseCase persistUseCase;
    @MockBean
    private ListTransactionsByCategoryUseCase listByCategoryUseCase;
    @MockBean
    private ListTransactionsByEstablishmentUseCase listByEstablishmentUseCase;
    @MockBean
    private ListAllTransactionsUseCase listAllUseCase;
    @MockBean
    private DeleteTransactionUseCase deleteUseCase;
    @MockBean
    private BudgetingAssistant budgetingAssistant;
    @MockBean
    private SummarizeMonthlyByCategoryUseCase summarizeUseCase;

    @Test
    void deveRetornarListaDeTransacoes() throws Exception {
        Transaction t = Transaction.builder()
                .id(UUID.randomUUID())
                .description("Café")
                .amount(BigDecimal.TEN)
                .establishment("Starbucks")
                .occurredAt(Instant.now())
                .category(Category.FOOD)
                .build();
        when(listAllUseCase.execute()).thenReturn(List.of(t));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Café"));
    }

    @Test
    void deveDeletarTransacaoERetornarNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/transactions/" + id))
                .andExpect(status().isNoContent());
    }
}
