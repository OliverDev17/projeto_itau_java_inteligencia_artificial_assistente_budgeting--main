package dio.budgeting.usecase;

import dio.budgeting.domain.Transaction;
import dio.budgeting.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListAllTransactionsUseCase {

    private final TransactionRepository repository;

    public ListAllTransactionsUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> execute() {
        return repository.findAll();
    }
}
