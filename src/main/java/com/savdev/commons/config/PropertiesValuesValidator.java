package com.savdev.commons.config;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class is not expected to be used by clients
 * It is used only by PropertiesFileValidator
 */
class PropertiesValuesValidator {

  static final String EMPTY_PROPS_ERROR =
    "The following props MUST be set to not empty values: %s\n";
  static final String NOT_CORRECT_MAP_PROPS_ERROR =
    "The following map-props MUST be set correctly: " +
      "${propKeyPrefix} = '%s';\n " +
    "Format: ${propKeyPrefix}${key}=${value} \n";

  static PropertiesValuesValidator instance(){
    return new PropertiesValuesValidator();
  }

  /**
   * Returns only properties, that are mentioned in the clazz
   * @param allProperties
   * @param clazz
   * @param <T>
   * @return
   */
  static <T> Properties propsFromConfigOnly(
    final Properties allProperties,
    final Class<T> clazz){
    Set<String> singleDefinedProps = propsDefined(clazz);
    Properties filteredProps = new Properties();
    Maps.fromProperties(allProperties)
      .entrySet().stream()
      .filter(e -> singleDefinedProps.contains(e.getKey()))
      .forEach(e -> filteredProps.put(e.getKey(), e.getValue()));
    Set<String> mapPropsDefined = mapsPropsDefined(clazz);
    Maps.fromProperties(allProperties)
      .entrySet().stream()
      .filter(e -> mapPropsDefined.stream()
          .anyMatch(mapProp -> e.getKey().startsWith(mapProp)))
      .forEach(e -> filteredProps.put(e.getKey(), e.getValue()));
    return filteredProps;
  }

  /**
   * Validates properties for exact configuration class.
   * <p>
   * Makes sure that per each "static final String" constants of this class,
   * there are nullable/empty properties
   *
   * @param clazz
   * @param properties
   * @param <T>
   */
  <T> Properties validateAndFilterProperties(
    final Class<T> clazz,
    final Properties properties) {
    Set<String> errors = Sets.newHashSet();
    errors.addAll(summariseErrors(
      notExpectedEmptyProperties(clazz, properties),
      EMPTY_PROPS_ERROR));
    errors.addAll(summariseErrors(notExpectedEmptyMapProperteis(clazz, properties),
      NOT_CORRECT_MAP_PROPS_ERROR));
    if (!errors.isEmpty()) {
      throw new IllegalStateException(String.join("\n", errors));
    }
    return propsFromConfigOnly(properties, clazz);
  }

  private Set<String> summariseErrors(
    final List<String> wrongProps,
    final String errorDescription) {
    if (!wrongProps.isEmpty()) {
      return Collections.singleton(
        String.format(errorDescription, String.join(", ", wrongProps)));
    } else {
      return Collections.emptySet();
    }
  }

  private <T> List<String> notExpectedEmptyMapProperteis(
    final Class<T> clazz,
    final Properties properties) {
    return Arrays.stream(clazz.getDeclaredFields())
      .filter(f -> f.getAnnotation(NotProperty.class) == null)
      .filter(f -> f.getAnnotation(MapProperty.class) != null)
      .filter(f -> (Modifier.isFinal(f.getModifiers())
        && Modifier.isStatic(f.getModifiers())
        && String.class.equals(f.getType())))
      .filter(f -> f.getAnnotation(NullableProperty.class) == null)
      .map(f -> {
        try {
          return (String) FieldUtils.readDeclaredStaticField(
            clazz, f.getName(), true);
        } catch (IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      })
      .filter(propName -> {
        Map<String, String> map = PropUtils.getPropsAsMapBySuffix(properties, propName);
        return map == null || map.isEmpty();
      })
      .collect(Collectors.toList());
  }

  private static <T> Set<String> mapsPropsDefined(
    final Class<T> clazz) {
    return Arrays.stream(clazz.getDeclaredFields())
      .filter(f -> f.getAnnotation(NotProperty.class) == null)
      .filter(f -> f.getAnnotation(MapProperty.class) != null)
      .filter(f -> (Modifier.isFinal(f.getModifiers())
        && Modifier.isStatic(f.getModifiers())
        && String.class.equals(f.getType())))
      .map(f -> {
        try {
          return (String) FieldUtils.readDeclaredStaticField(
            clazz, f.getName(), true);
        } catch (IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      })
      .collect(Collectors.toSet());
  }

  private static <T> Set<String> propsDefined(
    final Class<T> clazz) {
    return Arrays.stream(clazz.getDeclaredFields())
      .filter(f -> f.getAnnotation(NotProperty.class) == null)
      .filter(f -> (Modifier.isFinal(f.getModifiers())
        && Modifier.isStatic(f.getModifiers())
        && String.class.equals(f.getType())))
      .map(f -> {
        try {
          return (String) FieldUtils.readDeclaredStaticField(
            clazz, f.getName(), true);
        } catch (IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      })
      .collect(Collectors.toSet());
  }

  private <T> List<String> notExpectedEmptyProperties(
    final Class<T> clazz,
    final Properties properties) {
    return Arrays.stream(clazz.getDeclaredFields())
      .filter(f -> f.getAnnotation(NotProperty.class) == null)
      .filter(f -> (Modifier.isFinal(f.getModifiers())
        && Modifier.isStatic(f.getModifiers())
        && String.class.equals(f.getType())))
      .filter(f -> f.getAnnotation(NullableProperty.class) == null)
      .filter(f -> f.getAnnotation(MapProperty.class) == null)
      .map(f -> {
        try {
          return (String) FieldUtils.readDeclaredStaticField(
            clazz, f.getName(), true);
        } catch (IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      })
      .filter(propName -> StringUtils.isEmpty(properties.getProperty(propName)))
      .collect(Collectors.toList());
  }
}
