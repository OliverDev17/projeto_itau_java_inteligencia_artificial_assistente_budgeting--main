package dio.budgeting.usecase;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class SummarizeMonthlyByCategoryUseCase {

    private final TransactionRepository repository;

    public SummarizeMonthlyByCategoryUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public BigDecimal execute(Category category, YearMonth yearMonth) {
        Instant from = yearMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = yearMonth.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        List<Transaction> transactions = repository.findByCategoryAndOccurredAtBetween(category, from, to);
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
