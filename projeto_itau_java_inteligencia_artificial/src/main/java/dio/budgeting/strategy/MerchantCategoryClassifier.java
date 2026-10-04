package dio.budgeting.strategy;

import dio.budgeting.domain.Category;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MerchantCategoryClassifier {

    private final List<MerchantCategoryRule> rules;

    public MerchantCategoryClassifier(List<MerchantCategoryRule> rules) {
        this.rules = rules;
    }

    public Category classify(String establishment) {
        if (establishment == null || establishment.isBlank()) {
            return Category.OTHER;
        }
        return rules.stream()
                .filter(rule -> rule.matches(establishment))
                .map(MerchantCategoryRule::getCategory)
                .findFirst()
                .orElse(Category.OTHER);
    }
}
