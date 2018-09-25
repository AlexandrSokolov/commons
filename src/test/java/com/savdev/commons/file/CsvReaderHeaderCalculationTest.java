package com.savdev.commons.file;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.savdev.commons.TestUtils.fromTemplate;

public class CsvReaderHeaderCalculationTest {

  static final int BUFFER_SIZE = 12048;

  static final String COL1 = "col1";
  static final String COL2 = "col2";
  static final String LINE1_VAL1 = "val1";
  static final String LINE1_VAL2 = "val2";
  static final String LINE2_VAL1 = "val3";
  static final String LINE2_VAL2 = "val4";

  static final String SINGLE_CHAR_LINE_SEPARATOR = "|";
  static final String SINGLE_CHAR_COLUMN_SEPARATOR = ",";
  static final String MULTIPLE_CHARS_LINE_SEPARATOR = "|||";
  static final String INPUT1_TEMPLATE =
    "${col1}${colSep}${col2}${s}${val1}${colSep}${val2}${s}${val3}${colSep}${val4}";
  static final String INPUT_SINGLE_LINE_TEMPLATE_WITH_FINAL_SEP =
    "${col1}${colSep}${col2}${s}${val1}${colSep}${val2}${s}";
  static final String INPUT_SINGLE_LINE_TEMPLATE_WITHOUT_FINAL_SEP =
    "${col1}${colSep}${col2}${s}${val1}${colSep}${val2}";

  static final Map<String, String> singleChLineSingleChColumn = new HashMap<>();
  static {
    singleChLineSingleChColumn.put("s", SINGLE_CHAR_LINE_SEPARATOR);
    singleChLineSingleChColumn.put("colSep", SINGLE_CHAR_COLUMN_SEPARATOR);
    singleChLineSingleChColumn.put("col1", COL1);
    singleChLineSingleChColumn.put("col2", COL2);
    singleChLineSingleChColumn.put("val1", LINE1_VAL1);
    singleChLineSingleChColumn.put("val2", LINE1_VAL2);
    singleChLineSingleChColumn.put("val3", LINE2_VAL1);
    singleChLineSingleChColumn.put("val4", LINE2_VAL2);
  }

  static final Map<String, String> multiChLineSingleChColumn = new HashMap<>();
  static {
    multiChLineSingleChColumn.putAll(singleChLineSingleChColumn);
    multiChLineSingleChColumn.put("s", MULTIPLE_CHARS_LINE_SEPARATOR);
  }

  /**
   * buffer size bigger than the fst line separator
   * line separator - the single char
   */
  @Test
  public void testHeaderPositionBufferBiggerSingleCharSeparator() {
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(
        fromTemplate(singleChLineSingleChColumn, INPUT1_TEMPLATE),
        StandardCharsets.UTF_8))
      .lineSeparator(SINGLE_CHAR_LINE_SEPARATOR)
      //.encoding(StandardCharsets.UTF_8)
      //.columnSeparator(",")
      .bufferSize(BUFFER_SIZE)
      .build();
    r.calculateHeaders();
    validateHeaderMap(r);
  }

  /**
   * buffer size bigger than the fst line separator,
   * line separator - contains of several chars
   */
  @Test
  public void testHeaderPositionBufferBiggerMultipleCharsSeparator() {
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(
        fromTemplate(multiChLineSingleChColumn, INPUT1_TEMPLATE),
        StandardCharsets.UTF_8))
      .lineSeparator(MULTIPLE_CHARS_LINE_SEPARATOR)
      //.encoding(StandardCharsets.UTF_8) default
      //.columnSeparator(",") default
      .bufferSize(BUFFER_SIZE)
      .build();
    r.calculateHeaders();
    validateHeaderMap(r);
  }

  /**
   * buffer size equals to the fst line separator
   * line separator - the single char
   */
  @Test
  public void testHeaderPositionBufferEqualsSingleCharSeparator() {
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(
        fromTemplate(
          singleChLineSingleChColumn, INPUT1_TEMPLATE),
        StandardCharsets.UTF_8))
      .lineSeparator(SINGLE_CHAR_LINE_SEPARATOR)
      //.encoding(StandardCharsets.UTF_8) default
      //.columnSeparator(",") default
      .bufferSize(
        fromTemplate(singleChLineSingleChColumn, INPUT1_TEMPLATE)
          .indexOf(SINGLE_CHAR_LINE_SEPARATOR))
      .build();
    r.calculateHeaders();
    validateHeaderMap(r);
  }

  /**
   * buffer size equals to the fst line separator position,
   * line separator - contains of several chars
   */
  @Test
  public void testHeaderPositionBufferEqualsMultipleCharsSeparator() {
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(
        fromTemplate(
          multiChLineSingleChColumn, INPUT1_TEMPLATE),
        StandardCharsets.UTF_8))
      .lineSeparator(MULTIPLE_CHARS_LINE_SEPARATOR)
      //.encoding(StandardCharsets.UTF_8) default
      //.columnSeparator(",") default
      .bufferSize(
        fromTemplate(multiChLineSingleChColumn, INPUT1_TEMPLATE)
          .indexOf(MULTIPLE_CHARS_LINE_SEPARATOR))
      .build();
    r.calculateHeaders();
    validateHeaderMap(r);
  }

  /**
   * buffer size smaller than the fst line separator
   * line separator - the single char
   */
  @Test
  public void testHeaderPositionBufferSmallerSingleCharSeparator() {
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(
        fromTemplate(
          singleChLineSingleChColumn, INPUT1_TEMPLATE),
        StandardCharsets.UTF_8))
      .lineSeparator(SINGLE_CHAR_LINE_SEPARATOR)
      //.encoding(StandardCharsets.UTF_8) default
      //.columnSeparator(",") default
      .bufferSize(2)
      .build();
    r.calculateHeaders();
    validateHeaderMap(r);
  }

  /**
   * buffer size smaller than the fst line separator,
   * but bigger than a size of line separator
   * line separator - contains of several chars
   */
  @Test
  public void testHeaderPositionBufferSmallerMultipleCharsSeparator() {
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(
        fromTemplate(
          multiChLineSingleChColumn, INPUT1_TEMPLATE),
        StandardCharsets.UTF_8))
      .lineSeparator(MULTIPLE_CHARS_LINE_SEPARATOR)
      //.encoding(StandardCharsets.UTF_8) default
      //.columnSeparator(",") default
      .bufferSize(4)
      .build();
    r.calculateHeaders();
    validateHeaderMap(r);
  }

  /**
   * buffer size smaller than the fst line separator,
   * and smaller than a size of line separator
   * line separator - contains of several chars
   */
  @Test
  public void testHeaderPositionBufferSmallerMultipleCharsSeparator2() {
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(
        fromTemplate(multiChLineSingleChColumn, INPUT1_TEMPLATE),
        StandardCharsets.UTF_8))
      //.encoding(StandardCharsets.UTF_8) default
      //.columnSeparator(",") default
      .lineSeparator(MULTIPLE_CHARS_LINE_SEPARATOR)
      .bufferSize(MULTIPLE_CHARS_LINE_SEPARATOR.length() - 1)
      .build();
    r.calculateHeaders();
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
