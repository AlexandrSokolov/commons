package com.savdev.commons.config;

import java.util.Map;
import java.util.Properties;

public class AllPropertiesConfig {

  @NotProperty
  public static final String CONSTANT = "someConstantValue";

  static final String SIMPLE_PROP = "simpleProp1";

  @NullableProperty
  static final String NOT_EXISTING_IN_FILE_PROP = "notExistingInFile";

  @NullableProperty
  static final String EMPTY_PROP = "emptyProp2";

  @MapProperty
  static final String MAP_PROP_KEY = "propKey.";

  final Properties props;

  public AllPropertiesConfig(Properties props) {
    this.props = props;
  }

  final Map<String, String> mapProps() {
    return PropUtils.getPropsAsMapBySuffix(this.props, MAP_PROP_KEY);
  }
}
