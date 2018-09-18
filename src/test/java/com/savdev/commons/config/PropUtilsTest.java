package com.savdev.commons.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

public class PropUtilsTest {

  static final String PROP_KEY = "key.";
  static final String KEY1 = "fst";
  static final String KEY2 = "snd";
  static final String VALUE1 = "value1";
  static final String VALUE2 = "value2";

  @Test
  public void testGetPropsAsMapBySuffix(){
    Properties prop = new Properties();
    prop.setProperty(PROP_KEY + KEY1, VALUE1);
    prop.setProperty(PROP_KEY + KEY2, VALUE2);
    Map<String, String> map = PropUtils.getPropsAsMapBySuffix(
      prop, PROP_KEY);
    Assert.assertNotNull(map);
    Assert.assertEquals(2, map.size());
    Assert.assertEquals(VALUE1, map.get(KEY1));
    Assert.assertEquals(VALUE2, map.get(KEY2));
  }
}
