package com.savdev.commons.config;

import com.google.common.collect.Sets;
import com.savdev.commons.TestUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

import static com.savdev.commons.config.PropertiesFileValidator.propValidator;

public class PropertiesFileValidatorTest {

  public static final String FOLDER = "config";
  public static final String SIMPLE_FILE = "simple.properties";
  public static final String SIMPLE_MAP_FILE = "simple.map.properties";
  public static final String EMPTY_PROP_FILE = "empty.value.properties";
  public static final String ALL_PROPS_FILE = "all.types.properties";
  public static final String ALL_PROPS_WITH_NOT_DEFINED_FILE
    = "all.types.with.not.defined.properties";
  public static final String MAP_PROP_KEY1 = "fst";
  public static final String MAP_PROP_KEY2 = "snd";
  public static final String NOT_DEFINED_IN_CONFIG1 =
    "notDefinedInConfig1";
  public static final String NOT_DEFINED_IN_CONFIG2 =
    "notDefinedInConfig2";

  @Test
  public void testAllProperties(){
    Properties props = propValidator().validPropsFromFile(
      AllPropertiesConfig.class,
      TestUtils.testResourceFolderFullPath(FOLDER),
      ALL_PROPS_FILE);
    validateAllPropertiesConfig(props);
  }

  /**
   *  Makes sure, that only properties, defined in AllPropertiesConfig are returned
   *  Properties defined in a prop file,
   *    but not defined in AllPropertiesConfig are filtered.
   */
  @Test
  public void testFilteringProperties(){
    Properties props = propValidator().validPropsFromFile(
      AllPropertiesConfig.class,
      TestUtils.testResourceFolderFullPath(FOLDER),
      ALL_PROPS_WITH_NOT_DEFINED_FILE);
    validateAllPropertiesConfig(props);
    Assert.assertFalse(props.containsKey(NOT_DEFINED_IN_CONFIG1));
    Assert.assertFalse(props.containsKey(NOT_DEFINED_IN_CONFIG2));
  }

  @Test
  public void simplePropTest(){
    Properties props = propValidator().validPropsFromFile(
      SimplePropConfig.class,
      TestUtils.testResourceFolderFullPath(FOLDER),
      SIMPLE_FILE);
    Assert.assertNotNull(props);
    Assert.assertEquals(1, props.size());
    Assert.assertTrue(props.containsKey(SimplePropConfig.PROP));
    Assert.assertEquals("testValue", props.getProperty(SimplePropConfig.PROP));
  }

  @Test
  public void notPropertyTest(){
    Properties props = propValidator().validPropsFromFile(
      NotPropertyConfig.class,
      TestUtils.testResourceFolderFullPath(FOLDER),
      SIMPLE_FILE);
    Assert.assertNotNull(props);
    Assert.assertEquals(1, props.size());
    Assert.assertTrue(props.containsKey(SimplePropConfig.PROP));
    Assert.assertEquals("testValue", props.getProperty(SimplePropConfig.PROP));
  }

  @Test
  public void nullablePropertyTest(){
    Properties props = propValidator().validPropsFromFile(
      NullablePropConfig.class,
      TestUtils.testResourceFolderFullPath(FOLDER),
      EMPTY_PROP_FILE);
    Assert.assertNotNull(props);
    Assert.assertEquals(1, props.size());
    Assert.assertTrue(props.containsKey(SimplePropConfig.PROP));
    Assert.assertTrue(StringUtils.isEmpty(props.getProperty(SimplePropConfig.PROP)));
  }

  @Test
  public void notExistingPropTest(){
    try {
      propValidator().validPropsFromFile(
        SimpleWithNotDefinedPropConfig.class,
        TestUtils.testResourceFolderFullPath(FOLDER),
        SIMPLE_FILE);
      Assert.fail();
    } catch (Exception e){
      Assert.assertEquals(IllegalStateException.class, e.getClass());
      Assert.assertEquals(
        String.format(
          PropertiesValuesValidator.EMPTY_PROPS_ERROR,
          String.join(", ",
            Sets.newHashSet(
              SimpleWithNotDefinedPropConfig.PROP2,
              SimpleWithNotDefinedPropConfig.PROP3))),
        e.getMessage());
    }
  }

  @Test
  public void emptyPropValueTest(){
    try {
      propValidator().validPropsFromFile(
        SimplePropConfig.class,
        TestUtils.testResourceFolderFullPath(FOLDER),
        EMPTY_PROP_FILE);
      Assert.fail();
    } catch (Exception e){
      Assert.assertEquals(IllegalStateException.class, e.getClass());
      Assert.assertEquals(
        String.format(
          PropertiesValuesValidator.EMPTY_PROPS_ERROR,
          String.join(", ",
            Sets.newHashSet(
              SimplePropConfig.PROP))),
        e.getMessage());
    }
  }

  @Test
  public void simpleMapTest(){
    SimpleMapConfig mapConfig = new SimpleMapConfig();
    Properties props = propValidator().validPropsFromFile(
      mapConfig.getClass(),
      TestUtils.testResourceFolderFullPath(FOLDER),
      SIMPLE_MAP_FILE);
    Assert.assertNotNull(props);
    Assert.assertEquals(2, props.size());

    Map<String, String> p = mapConfig.mapProps(props);
    Assert.assertEquals(2, p.size());
    Assert.assertEquals("tstVal1", p.get(MAP_PROP_KEY1));
    Assert.assertEquals("tstVal2", p.get(MAP_PROP_KEY2));
  }

  @Test
  public void simpleMapWrongFormatTest(){
    try {
      propValidator().validPropsFromFile(
        SimpleMapConfig.class,
        TestUtils.testResourceFolderFullPath(FOLDER),
        SIMPLE_FILE);
    } catch (Exception e){
      Assert.assertEquals(IllegalStateException.class, e.getClass());
      Assert.assertEquals(
        String.format(
          PropertiesValuesValidator.NOT_CORRECT_MAP_PROPS_ERROR,
          SimpleMapConfig.MAP_PROP),
        e.getMessage());
    }
  }



  public void validateAllPropertiesConfig(Properties props){
    Assert.assertNotNull(props);
    Assert.assertEquals(4, props.size());
    Assert.assertFalse(props.containsKey(AllPropertiesConfig.CONSTANT));
    Assert.assertTrue(props.containsKey(AllPropertiesConfig.EMPTY_PROP));
    Assert.assertTrue(props.containsKey(AllPropertiesConfig.SIMPLE_PROP));
    //it is only a key to map-props, but not a full property name:
    Assert.assertFalse(props.containsKey(AllPropertiesConfig.MAP_PROP_KEY));
    Assert.assertFalse(props.containsKey(
      AllPropertiesConfig.NOT_EXISTING_IN_FILE_PROP));

    AllPropertiesConfig config = new AllPropertiesConfig(props);
    Map<String, String> p = config.mapProps();
    Assert.assertNotNull(p);
    Assert.assertEquals(2, p.size());
    Assert.assertEquals("test1", p.get(MAP_PROP_KEY1));
    Assert.assertEquals("test2", p.get(MAP_PROP_KEY2));
  }
}
