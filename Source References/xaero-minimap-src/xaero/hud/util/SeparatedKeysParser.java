package xaero.hud.util;

import java.text.ParseException;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.function.Predicate;

public class SeparatedKeysParser {
   private Predicate<Character> isSeparator;

   public SeparatedKeysParser(Predicate<Character> isSeparator) {
      this.isSeparator = isSeparator;
   }

   public String[] parseKeys(String keysString) throws ParseException {
      StringCharacterIterator sci = new StringCharacterIterator(keysString);
      StringBuilder keyBuilder = new StringBuilder(64);
      ArrayList<String> keysBuilder = new ArrayList();
      Predicate<Character> isSeparator = this.isSeparator;

      char c;
      for(boolean findSeparator = false; (c = sci.current()) != '\uffff'; sci.next()) {
         if (keyBuilder.length() != 0 || c != ' ') {
            if (isSeparator.test(c)) {
               if (!findSeparator) {
                  keysBuilder.add(keyBuilder.toString());
                  keyBuilder.setLength(0);
               }

               findSeparator = false;
            } else {
               if (findSeparator) {
                  this.throwError(c, sci.getIndex(), keysString);
               }

               if (c == '\'') {
                  if (keyBuilder.length() != 0) {
                     this.throwError('\'', sci.getIndex(), keysString);
                  }

                  sci.next();
                  keysBuilder.add(this.parseKeyUntilChar(keyBuilder, sci, (t) -> t == '\'', keysString));
                  keyBuilder.setLength(0);
                  findSeparator = true;
               } else {
                  keyBuilder.append(c);
               }
            }
         }
      }

      if (keyBuilder.length() > 0) {
         keysBuilder.add(keyBuilder.toString());
      }

      return (String[])keysBuilder.toArray(new String[0]);
   }

   private String parseKeyUntilChar(StringBuilder keyBuilder, StringCharacterIterator sci, Predicate<Character> isEnd, String keysString) throws ParseException {
      keyBuilder.setLength(0);

      char c;
      for(; (c = sci.current()) != '\uffff'; sci.next()) {
         if (c == '\\') {
            keyBuilder.append(sci.next());
         } else {
            if (isEnd.test(c)) {
               break;
            }

            keyBuilder.append(c);
         }
      }

      if (!isEnd.test(c)) {
         this.throwError(c, sci.getIndex(), keysString);
      }

      return keyBuilder.toString();
   }

   private void throwError(char unexpected, int position, String keysString) throws ParseException {
      throw new ParseException(String.format("Unexpected \"%s\" at position %d in \"%s\"!", unexpected, position, keysString), position);
   }
}
