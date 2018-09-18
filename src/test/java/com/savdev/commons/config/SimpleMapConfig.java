package com.savdev.commons.config;

import java.util.Map;
import java.util.Properties;

public class SimpleMapConfig {

  @MapProperty
  static final String MAP_PROP = "key.";

  public Map<String, String> mapProps(
    final Properties properties){
    return PropUtils.getPropsAsMapBySuffix(properties, MAP_PROP);
  }
}
