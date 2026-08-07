package xaero.common.validator;

import net.minecraft.class_342;

public class NumericFieldValidator {
   protected StringBuilder stringBuilder = new StringBuilder();

   protected boolean charIsValid(char c, int index) {
      return c >= '0' && c <= '9' || c == '-' && index == 0;
   }

   public void validate(class_342 field) {
      String text = field.method_1882();
      char[] charArray = text.toCharArray();
      this.stringBuilder.delete(0, this.stringBuilder.length());
      boolean validated = true;

      for(int i = 0; i < charArray.length; ++i) {
         if (!this.charIsValid(charArray[i], i)) {
            validated = false;
         } else {
            this.stringBuilder.append(charArray[i]);
         }
      }

      validated = this.checkNumberFormat(validated);
      if (!validated) {
         field.method_1852(this.stringBuilder.toString());
      }

   }

   protected boolean checkNumberFormat(boolean validated) {
      boolean validFormat = false;

      while(!validFormat) {
         try {
            if (this.stringBuilder.length() != 0 && (this.stringBuilder.length() != 1 || this.stringBuilder.charAt(0) != '-')) {
               Integer.parseInt(this.stringBuilder.toString());
            }

            validFormat = true;
         } catch (NumberFormatException var4) {
            this.stringBuilder.deleteCharAt(this.stringBuilder.length() - 1);
            validated = false;
         }
      }

      return validated;
   }
}
