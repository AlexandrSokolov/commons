package com.savdev.commons.config;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public final class PropUtils {

  /**
   * @param properties
   * @param keyPrefix
   * @return returns for a set of properties defined as:
   * ${propKeyPrefix}${key}=${value}
   * a mapping: ${key}=${value}
   */
  public static Map<String, String> getPropsAsMapBySuffix(
    final Properties properties,
    final String keyPrefix) {
    if (properties == null) {
      throw new IllegalStateException("Properties cannot be nullable");
    }
    if (StringUtils.isEmpty(keyPrefix)) {
      throw new IllegalStateException("A prefix for a map property cannot be empty");
    }
    return Maps.fromProperties(properties)
      .entrySet().stream()
      .filter(entry -> entry.getKey().startsWith(keyPrefix))
      .collect(Collectors.toMap(
        x -> x.getKey().substring(keyPrefix.length()),
        x -> x.getValue()));
  }

  private PropUtils() {
    throw new AssertionError(
      "Utility utils class cannot be instantiated via constructor");
  }

}
