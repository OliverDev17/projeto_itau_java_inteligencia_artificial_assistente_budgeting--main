package dio.budgeting.usecase;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.repository.TransactionRepository;
import dio.budgeting.strategy.MerchantCategoryClassifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class PersistTransactionUseCase {

    private final TransactionRepository repository;
    private final MerchantCategoryClassifier merchantCategoryClassifier;

    public PersistTransactionUseCase(TransactionRepository repository,
                                     MerchantCategoryClassifier merchantCategoryClassifier) {
        this.repository = repository;
        this.merchantCategoryClassifier = merchantCategoryClassifier;
    }

    public Transaction execute(String description, BigDecimal amount, String establishment,
                               Instant occurredAt, Category category) {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
        if (category == null) {
            category = merchantCategoryClassifier.classify(establishment);
        }
        Transaction transaction = Transaction.builder()
                .description(description)
                .amount(amount)
                .establishment(establishment)
                .occurredAt(occurredAt)
                .category(category)
                .build();
        return repository.save(transaction);
    }
}
