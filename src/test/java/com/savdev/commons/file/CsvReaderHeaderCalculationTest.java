package com.savdev.commons.file;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.savdev.commons.TestUtils.fromTemplate;

public class CsvReaderHeaderCalculationTest {

  static final String COL1 = "col1";
  static final String COL2 = "col2";
  static final String LINE1_VAL1 = "val1";
  static final String LINE1_VAL2 = "val2";
  static final String LINE2_VAL1 = "val3";
  static final String LINE2_VAL2 = "val4";

  static final String SINGLE_CHAR_LINE_SEPARATOR = "|";
  static final Map<String, String> m = new HashMap<>();
  static {
    m.put("s", SINGLE_CHAR_LINE_SEPARATOR);
    m.put("col1", COL1);
    m.put("col2", COL2);
    m.put("val1", LINE1_VAL1);
    m.put("val2", LINE1_VAL2);
    m.put("val3", LINE2_VAL1);
    m.put("val4", LINE2_VAL2);
  }
  static final String INPUT1_TEMPLATE =
    "${col1},${col2}${s}${val1},val2${s}${val3},${val4}";


  static final String MULTIPLE_CHARS_LINE_SEPARATOR = "|||";
  static final String INPUT2 = new StringSubstitutor(
    ImmutableMap.of(
      "s", MULTIPLE_CHARS_LINE_SEPARATOR,
      "col1", COL1,
      "col2", COL2)).replace(
    "${col1},${col2}${s}val1,val2${s}val3,val4");



  /**
   * buffer size bigger than the fst line separator
   * line separator - the single char
   */
  @Test
  public void testHeaderPositionBufferBiggerSingleCharSeparator() {
    CsvReader r = new CsvReader(
      IOUtils.toInputStream(fromTemplate(m, INPUT1_TEMPLATE), StandardCharsets.UTF_8),
      StandardCharsets.UTF_8,
      SINGLE_CHAR_LINE_SEPARATOR,
      ",",
      CsvReader.BUFFER_SIZE);
    Assert.assertEquals(
      fromTemplate(m, INPUT1_TEMPLATE)
          .indexOf(SINGLE_CHAR_LINE_SEPARATOR)
        + SINGLE_CHAR_LINE_SEPARATOR.length(), r.headerPosition());
    validateHeaderMap(r);
  }

  /**
   * buffer size bigger than the fst line separator,
   * line separator - contains of several chars
   */
  @Test
  public void testHeaderPositionBufferBiggerMultipleCharsSeparator() {
    CsvReader r = new CsvReader(
      IOUtils.toInputStream(INPUT2, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8,
      MULTIPLE_CHARS_LINE_SEPARATOR,
      ",",
      CsvReader.BUFFER_SIZE);
    Assert.assertEquals(
      INPUT2.indexOf(MULTIPLE_CHARS_LINE_SEPARATOR) +
        MULTIPLE_CHARS_LINE_SEPARATOR.length(),
      r.headerPosition());
    validateHeaderMap(r);
  }

  /**
   * buffer size equals to the fst line separator
   * line separator - the single char
   */
  @Test
  public void testHeaderPositionBufferEqualsSingleCharSeparator() {
    CsvReader r = new CsvReader(
      IOUtils.toInputStream(fromTemplate(m, INPUT1_TEMPLATE), StandardCharsets.UTF_8),
      StandardCharsets.UTF_8,
      SINGLE_CHAR_LINE_SEPARATOR,
      ",",
      fromTemplate(m, INPUT1_TEMPLATE).indexOf(SINGLE_CHAR_LINE_SEPARATOR));
    Assert.assertEquals(
      fromTemplate(m, INPUT1_TEMPLATE).indexOf(SINGLE_CHAR_LINE_SEPARATOR) +
        SINGLE_CHAR_LINE_SEPARATOR.length(),
      r.headerPosition());
    validateHeaderMap(r);
  }

  /**
   * buffer size equals to the fst line separator position,
   * line separator - contains of several chars
   */
  @Test
  public void testHeaderPositionBufferEqualsMultipleCharsSeparator() {
    CsvReader r = new CsvReader(
      IOUtils.toInputStream(INPUT2, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8,
      MULTIPLE_CHARS_LINE_SEPARATOR,
      ",",
      INPUT2.indexOf(MULTIPLE_CHARS_LINE_SEPARATOR));
    Assert.assertEquals(
      INPUT2.indexOf(MULTIPLE_CHARS_LINE_SEPARATOR) +
        MULTIPLE_CHARS_LINE_SEPARATOR.length(),
      r.headerPosition());
    validateHeaderMap(r);
  }

  /**
   * buffer size smaller than the fst line separator
   * line separator - the single char
   */
  @Test
  public void testHeaderPositionBufferSmallerSingleCharSeparator() {
    CsvReader r = new CsvReader(
      IOUtils.toInputStream(fromTemplate(m, INPUT1_TEMPLATE), StandardCharsets.UTF_8),
      StandardCharsets.UTF_8,
      SINGLE_CHAR_LINE_SEPARATOR,
      ",",
      2);
    Assert.assertEquals(
      fromTemplate(m, INPUT1_TEMPLATE).indexOf(SINGLE_CHAR_LINE_SEPARATOR) +
        SINGLE_CHAR_LINE_SEPARATOR.length(),
      r.headerPosition());
    validateHeaderMap(r);
  }

  /**
   * buffer size smaller than the fst line separator,
   * but bigger than a size of line separator
   * line separator - contains of several chars
   */
  @Test
  public void testHeaderPositionBufferSmallerMultipleCharsSeparator() {
    CsvReader r = new CsvReader(
      IOUtils.toInputStream(INPUT2, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8,
      MULTIPLE_CHARS_LINE_SEPARATOR,
      ",",
      4);
    Assert.assertEquals(
      INPUT2.indexOf(MULTIPLE_CHARS_LINE_SEPARATOR) +
        MULTIPLE_CHARS_LINE_SEPARATOR.length(),
      r.headerPosition());
    validateHeaderMap(r);
  }

  /**
   * buffer size smaller than the fst line separator,
   * and smaller than a size of line separator
   * line separator - contains of several chars
   */
  @Test
  public void testHeaderPositionBufferSmallerMultipleCharsSeparator2() {
    CsvReader r = new CsvReader(
      IOUtils.toInputStream(INPUT2, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8,
      MULTIPLE_CHARS_LINE_SEPARATOR,
      ",",
      MULTIPLE_CHARS_LINE_SEPARATOR.length() - 1);
    Assert.assertEquals(
      INPUT2.indexOf(MULTIPLE_CHARS_LINE_SEPARATOR) +
        MULTIPLE_CHARS_LINE_SEPARATOR.length(),
      r.headerPosition());
    validateHeaderMap(r);
  }

  private void validateHeaderMap(CsvReader r) {
    Assert.assertEquals(2, r.csvHeader.size());
    Assert.assertTrue(r.csvHeader.containsKey(0));
    Assert.assertEquals(COL1, r.csvHeader.get(0).columnName);
    Assert.assertTrue(r.csvHeader.containsKey(1));
    Assert.assertEquals(COL2, r.csvHeader.get(1).columnName);
  }
}
