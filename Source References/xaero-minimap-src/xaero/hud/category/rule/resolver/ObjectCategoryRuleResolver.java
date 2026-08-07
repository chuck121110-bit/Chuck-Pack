package xaero.hud.category.rule.resolver;

import java.util.Iterator;
import java.util.List;
import xaero.hud.category.FilterObjectCategory;
import xaero.hud.category.rule.ExcludeListMode;
import xaero.hud.category.rule.ObjectCategoryExcludeList;
import xaero.hud.category.rule.ObjectCategoryIncludeList;

public final class ObjectCategoryRuleResolver {
   private ObjectCategoryRuleResolver() {
   }

   public <E, P, C extends FilterObjectCategory<E, P, ?, C>> C resolve(C category, E element, P context) {
      if (!this.followsRules(category, element, context)) {
         return null;
      } else {
         Iterator<C> subCategoryIterator = category.getDirectSubCategoryIterator();

         while(subCategoryIterator.hasNext()) {
            C subCategory = (C)(subCategoryIterator.next());
            C subResolve = this.resolve(subCategory, element, context);
            if (subResolve != null) {
               return subResolve;
            }
         }

         return category;
      }
   }

   private <E, P, C extends FilterObjectCategory<E, P, ?, C>> boolean followsRules(C category, E element, P context) {
      boolean result = ((FilterObjectCategory)category).getBaseRule().isFollowedBy(element, context);
      if (!result) {
         for(ObjectCategoryIncludeList<E, P, ?> includeList : ((FilterObjectCategory)category).getIncludeLists()) {
            if (includeList.isFollowedBy(element, context)) {
               result = true;
               break;
            }
         }
      }

      if (result) {
         List<ObjectCategoryExcludeList<E, P, ?>> excludeLists = ((FilterObjectCategory)category).getExcludeLists();
         if (category.getExcludeMode() == ExcludeListMode.ALL_BUT) {
            result = false;
         }

         for(ObjectCategoryExcludeList<E, P, ?> excludeList : excludeLists) {
            if (result != excludeList.isFollowedBy(element, context)) {
               result = !result;
               break;
            }
         }
      }

      return result;
   }

   public static final class Builder {
      private Builder() {
      }

      public Builder setDefault() {
         return this;
      }

      public ObjectCategoryRuleResolver build() {
         return new ObjectCategoryRuleResolver();
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
