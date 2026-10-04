package dio.budgeting.strategy;

import dio.budgeting.domain.Category;

public interface MerchantCategoryRule {
    boolean matches(String establishment);
    Category getCategory();
}
