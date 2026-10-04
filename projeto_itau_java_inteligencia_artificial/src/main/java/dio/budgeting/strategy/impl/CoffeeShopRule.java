package dio.budgeting.strategy.impl;

import dio.budgeting.domain.Category;
import dio.budgeting.strategy.MerchantCategoryRule;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CoffeeShopRule implements MerchantCategoryRule {

    private static final Set<String> KEYWORDS = Set.of(
            "starbucks", "café", "cafeteria", "coffee", "espresso", "latte"
    );

    @Override
    public boolean matches(String establishment) {
        return establishment != null && KEYWORDS.stream()
                .anyMatch(k -> establishment.toLowerCase().contains(k));
    }

    @Override
    public Category getCategory() {
        return Category.FOOD;
    }
}
