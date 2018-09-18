package com.savdev.commons.config;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import static com.savdev.commons.file.FileUtils.fileUtils;

public class PropertiesFileValidator {

  public static PropertiesFileValidator propValidator(){
    return new PropertiesFileValidator();
  }

  /**
   * Returns from a file only properties, specified in the "clazz" config
   *
   * @param configFolder property folder location
   * @param fileName     property file name
   * @return validated input stream of a property file
   */
  public <T> Properties validPropsFromFile(
    final Class<T> clazz,
    final String configFolder,
    final String fileName) {
    try {
      InputStream inputStream = fileUtils().validFile(configFolder, fileName);
      Properties properties = new Properties();
      properties.load(inputStream);

      return PropertiesValuesValidator.instance()
        .validateAndFilterProperties(clazz, properties);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Extracts value from a system property, make sure it is not null
   * @param systemVariableName
   * @return
   */
  public String notNullableSysPropValue(String systemVariableName) {
    //wrong system property setting
    Optional<String> maybeValue = sysPropValue(systemVariableName);
    if (!maybeValue.isPresent() || StringUtils.isEmpty(maybeValue.get())) {
      throw new IllegalStateException(
        String.format("System property = '%s' is not defined",
          systemVariableName));
    }
    return maybeValue.get();
  }

  /**
   * Extracts value from a system property
   *
   * @param systemVariableName
   * @return
   */
  public Optional<String> sysPropValue(String systemVariableName) {
    //wrong API usage
    if (StringUtils.isEmpty(systemVariableName)) {
      throw new IllegalStateException(
        "System property name in the method is empty");
    }
    //wrong system property setting
    String value = System.getProperty(systemVariableName);
    return StringUtils.isEmpty(value) ?
      Optional.empty() : Optional.of(value);
  }
}
