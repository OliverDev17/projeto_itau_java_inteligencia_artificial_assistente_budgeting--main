package dio.budgeting.usecase;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.repository.TransactionRepository;
import dio.budgeting.strategy.MerchantCategoryClassifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersistTransactionUseCaseTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private MerchantCategoryClassifier classifier;

    @InjectMocks
    private PersistTransactionUseCase useCase;

    @Test
    void deveUsarInstantNowQuandoOccurredAtForNulo() {
        when(classifier.classify(any())).thenReturn(Category.FOOD);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Instant before = Instant.now();
        Transaction result = useCase.execute("Café", BigDecimal.TEN, "Starbucks", null, null);
        Instant after = Instant.now();

        assertNotNull(result.getOccurredAt());
        assertTrue(!result.getOccurredAt().isBefore(before));
        assertTrue(!result.getOccurredAt().isAfter(after));
    }

    @Test
    void deveUsarClassifierQuandoCategoryForNula() {
        when(classifier.classify("Starbucks")).thenReturn(Category.FOOD);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = useCase.execute("Café", BigDecimal.TEN, "Starbucks", Instant.now(), null);

        assertEquals(Category.FOOD, result.getCategory());
        verify(classifier).classify("Starbucks");
    }

    @Test
    void deveUsarCategoryInformadaQuandoNaoForNula() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = useCase.execute("Café", BigDecimal.TEN, "Starbucks", Instant.now(), Category.LEISURE);

        assertEquals(Category.LEISURE, result.getCategory());
        verify(classifier, never()).classify(any());
    }
}
