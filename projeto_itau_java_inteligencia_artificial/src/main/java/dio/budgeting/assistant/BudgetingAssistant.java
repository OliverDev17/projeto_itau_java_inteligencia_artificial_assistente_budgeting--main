package dio.budgeting.assistant;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.usecase.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class BudgetingAssistant {

    private final PersistTransactionUseCase persistUseCase;
    private final ListTransactionsByCategoryUseCase listByCategoryUseCase;
    private final ListTransactionsByEstablishmentUseCase listByEstablishmentUseCase;
    private final SummarizeMonthlyByCategoryUseCase summarizeUseCase;
    private final DeleteTransactionUseCase deleteUseCase;

    public BudgetingAssistant(
            PersistTransactionUseCase persistUseCase,
            ListTransactionsByCategoryUseCase listByCategoryUseCase,
            ListTransactionsByEstablishmentUseCase listByEstablishmentUseCase,
            SummarizeMonthlyByCategoryUseCase summarizeUseCase,
            DeleteTransactionUseCase deleteUseCase) {
        this.persistUseCase = persistUseCase;
        this.listByCategoryUseCase = listByCategoryUseCase;
        this.listByEstablishmentUseCase = listByEstablishmentUseCase;
        this.summarizeUseCase = summarizeUseCase;
        this.deleteUseCase = deleteUseCase;
    }

    @Tool(name = "registrar-transacao", description = "Registra um gasto ou transação financeira informada pelo usuário")
    public Transaction registrarTransacao(
            @ToolParam(description = "Descrição do gasto, ex: café no Starbucks") String description,
            @ToolParam(description = "Valor do gasto em reais, ex: 50.00") BigDecimal amount,
            @ToolParam(description = "Nome do estabelecimento ou local, ex: Starbucks. Pode ser nulo.") String establishment,
            @ToolParam(description = "Data e hora do gasto em ISO-8601. Se o usuário disser 'agora' ou não mencionar, deixe nulo.") Instant occurredAt,
            @ToolParam(description = "Categoria do gasto: FOOD, GROCERIES, PHARMA, AUTO, TRANSPORT, LEISURE, OTHER. Deixe nulo para inferência automática.") Category category) {
        return persistUseCase.execute(description, amount, establishment, occurredAt, category);
    }

    @Tool(name = "listar-por-categoria", description = "Lista todas as transações de uma categoria específica")
    public List<Transaction> listarPorCategoria(
            @ToolParam(description = "Categoria para filtrar: FOOD, GROCERIES, PHARMA, AUTO, TRANSPORT, LEISURE, OTHER") Category category) {
        return listByCategoryUseCase.execute(category);
    }

    @Tool(name = "listar-por-estabelecimento", description = "Lista transações por nome de estabelecimento ou local")
    public List<Transaction> listarPorEstabelecimento(
            @ToolParam(description = "Nome ou parte do nome do estabelecimento, ex: Starbucks") String name) {
        return listByEstablishmentUseCase.execute(name);
    }

    @Tool(name = "resumo-mensal-por-categoria", description = "Retorna o total gasto em uma categoria em determinado mês")
    public String resumoMensalPorCategoria(
            @ToolParam(description = "Categoria para calcular o total: FOOD, GROCERIES, PHARMA, AUTO, TRANSPORT, LEISURE, OTHER") Category category,
            @ToolParam(description = "Mês no formato YYYY-MM, ex: 2025-01") String mesAno) {
        YearMonth yearMonth = YearMonth.parse(mesAno);
        var total = summarizeUseCase.execute(category, yearMonth);
        return String.format("Total gasto em %s (%s): R$ %.2f", category, mesAno, total);
    }

    @Tool(name = "excluir-transacao", description = "Exclui uma transação pelo seu identificador único")
    public String excluirTransacao(
            @ToolParam(description = "UUID da transação a ser excluída") String id) {
        deleteUseCase.execute(UUID.fromString(id));
        return "Transação " + id + " excluída com sucesso.";
    }
}
