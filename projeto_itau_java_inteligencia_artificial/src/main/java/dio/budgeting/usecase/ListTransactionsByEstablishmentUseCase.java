package dio.budgeting.usecase;

import dio.budgeting.domain.Transaction;
import dio.budgeting.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListTransactionsByEstablishmentUseCase {

    private final TransactionRepository repository;

    public ListTransactionsByEstablishmentUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> execute(String name) {
        return repository.findByEstablishmentContainingIgnoreCase(name);
    }
}
