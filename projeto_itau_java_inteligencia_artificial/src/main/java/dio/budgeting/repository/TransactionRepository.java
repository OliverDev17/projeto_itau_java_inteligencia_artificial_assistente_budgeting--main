package dio.budgeting.repository;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByCategory(Category category);

    List<Transaction> findByEstablishmentContainingIgnoreCase(String name);

    List<Transaction> findByCategoryAndOccurredAtBetween(Category category, Instant from, Instant to);
}
