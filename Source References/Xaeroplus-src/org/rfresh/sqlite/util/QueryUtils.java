package org.rfresh.sqlite.util;

import java.util.List;
import java.util.stream.Collectors;

public class QueryUtils {
   public static String valuesQuery(List<String> columns, List<List<Object>> valuesList) {
      valuesList.forEach((list) -> {
         if (list.size() != columns.size()) {
            throw new IllegalArgumentException("values and columns must have the same size");
         }
      });
      return "with cte(" + String.join(",", columns) + ") as (values " + (String)valuesList.stream().map((values) -> "(" + (String)values.stream().map((o) -> {
            if (o instanceof String) {
               return "'" + o + "'";
            } else {
               return o == null ? "null" : o.toString();
            }
         }).collect(Collectors.joining(",")) + ")").collect(Collectors.joining(",")) + ") select * from cte";
   }
}
