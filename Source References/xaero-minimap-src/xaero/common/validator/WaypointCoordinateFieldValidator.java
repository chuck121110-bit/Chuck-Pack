package xaero.common.validator;

public class WaypointCoordinateFieldValidator extends NumericFieldValidator {
   protected boolean charIsValid(char c, int index) {
      return c == '~' && index == 0 || !this.stringBuilder.toString().equals("~") && super.charIsValid(c, index);
   }

   protected boolean checkNumberFormat(boolean validated) {
      return this.stringBuilder.toString().equals("~") ? validated : super.checkNumberFormat(validated);
   }
}
