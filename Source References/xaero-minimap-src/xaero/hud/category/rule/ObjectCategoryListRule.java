package xaero.hud.category.rule;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import xaero.common.misc.ListFactory;
import xaero.hud.category.FilterObjectCategory;

public abstract class ObjectCategoryListRule<E, P, S> extends ObjectCategoryRule<E, P> implements Iterable<String> {
   private final List<String> stringList;
   private final Set<S> set;
   private final ObjectCategoryListRuleType<E, P, S> type;

   ObjectCategoryListRule(@Nonnull ObjectCategoryListRuleType<E, P, S> type, @Nonnull String name, @Nonnull List<String> stringList, @Nonnull Set<S> set) {
      super(name);
      this.type = type;
      this.stringList = stringList;
      this.set = set;
   }

   public boolean inList(E object, P context) {
      if (this.set.isEmpty()) {
         return false;
      } else {
         S s = (S)this.type.getGetter().apply(object, context);
         return s != null && this.set.contains(s);
      }
   }

   public Iterator<String> iterator() {
      return this.stringList.iterator();
   }

   public Predicate<String> getStringValidator() {
      return this.type.getStringValidator();
   }

   public ObjectCategoryListRuleType<E, P, S> getType() {
      return this.type;
   }

   public abstract static class Builder<E, P, S, B extends Builder<E, P, S, B>> {
      protected final B self = (B)this;
      protected final List<String> stringList;
      protected final ObjectCategoryListRuleType<E, P, S> type;

      protected Builder(ListFactory listFactory, ObjectCategoryListRuleType<E, P, S> type) {
         this.stringList = listFactory.<String>get();
         this.type = type;
      }

      public B setDefault() {
         this.stringList.clear();
         return this.self;
      }

      public B addListElement(String element) {
         this.stringList.add(element);
         return this.self;
      }

      public ObjectCategoryListRuleType<E, P, S> getType() {
         return this.type;
      }

      protected <C extends FilterObjectCategory<E, P, ?, C>> ObjectCategoryListRule<E, P, S> build(List<C> subCategories, Function<C, ObjectCategoryListRule<E, P, S>> subListGetter, Function<C, ObjectCategoryListRule<E, P, S>> subListExceptionsGetter) {
         if (this.stringList == null) {
            throw new IllegalStateException("required fields not set!");
         } else {
            Set<S> effectiveSet = new HashSet();

            for(String stringElement : this.stringList) {
               String validatedString = (String)this.type.getStringFixer().apply(stringElement);
               List<S> resolvedElement = (List)this.type.getElementResolver().apply(validatedString);
               if (resolvedElement != null && !resolvedElement.isEmpty()) {
                  effectiveSet.addAll(resolvedElement);
               }
            }

            if (subListGetter == null) {
               return this.buildInternally(effectiveSet);
            } else {
               for(C subCategory : subCategories) {
                  if (subCategory.getIncludeInSuperCategory()) {
                     ObjectCategoryListRule<E, P, S> subList = (ObjectCategoryListRule)subListGetter.apply(subCategory);
                     ObjectCategoryListRule<E, P, S> subListExceptions = subListExceptionsGetter == null ? null : (ObjectCategoryListRule)subListExceptionsGetter.apply(subCategory);
                     Stream var10000 = subList.set.stream().filter((s) -> subListExceptions == null || !subListExceptions.set.contains(s));
                     Objects.requireNonNull(effectiveSet);
                     var10000.forEach(effectiveSet::add);
                  }
               }

               return this.buildInternally(effectiveSet);
            }
         }
      }

      protected abstract ObjectCategoryListRule<E, P, S> buildInternally(Set<S> var1);
   }
}
