package com.savdev.commons.function;

import java.util.function.Predicate;

public class FunctionUtils {
  public static <T> Predicate<T> not(Predicate<T> t) {
    return t.negate();
  }
}
