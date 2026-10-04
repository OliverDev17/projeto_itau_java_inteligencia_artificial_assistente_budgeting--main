package dio.budgeting.strategy;

import dio.budgeting.domain.Category;
import dio.budgeting.strategy.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MerchantCategoryClassifierTest {

    private MerchantCategoryClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new MerchantCategoryClassifier(List.of(
                new CoffeeShopRule(),
                new GroceryRule(),
                new PharmaRule(),
                new AutoRule(),
                new TransportRule()
        ));
    }

    @Test
    void deveClassificarStarbucksComoFood() {
        assertEquals(Category.FOOD, classifier.classify("Starbucks"));
    }

    @Test
    void deveClassificarMercadaoComoGroceries() {
        assertEquals(Category.GROCERIES, classifier.classify("Supermercado Extra"));
    }

    @Test
    void deveClassificarDrogariaComoPharma() {
        assertEquals(Category.PHARMA, classifier.classify("Drogaria São Paulo"));
    }

    @Test
    void deveClassificarPostoComoAuto() {
        assertEquals(Category.AUTO, classifier.classify("Posto Shell"));
    }

    @Test
    void deveClassificarUberComoTransport() {
        assertEquals(Category.TRANSPORT, classifier.classify("Uber"));
    }

    @Test
    void deveRetornarOtherQuandoNenhumaRegraCorresponde() {
        assertEquals(Category.OTHER, classifier.classify("Restaurante Desconhecido XYZ"));
    }

    @Test
    void deveRetornarOtherParaEstabelecimentoNulo() {
        assertEquals(Category.OTHER, classifier.classify(null));
    }

    @Test
    void deveRetornarOtherParaEstabelecimentoVazio() {
        assertEquals(Category.OTHER, classifier.classify(""));
    }
}
