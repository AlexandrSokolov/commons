package com.savdev.commons.file;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.savdev.commons.TestUtils.fromTemplate;
import static com.savdev.commons.file.CsvReaderHeaderCalculationTest.*;

public class CsvReaderHandleLineTest {

  static final String QUOTED1_1 = "value 1_1 in Quotes";
  static final String QUOTED1_2 = "value 1_2 in Quotes";
  static final String QUOTED2_1 = "value 2_1 in Quotes";
  static final String QUOTED2_2 = "value 2_2 in Quotes";


  static final String QUOTED1_1_WITH_SEP = "\"value 1_1 ${s} in Quotes\"";
  static final String QUOTED1_2_WITH_SEP = "\"value 1_2 ${s} in Quotes\"";
  static final String QUOTED2_1_WITH_SEP = "\"value 2_1 ${s} in Quotes\"";
  static final String QUOTED2_2_WITH_SEP = "\"value 2_2 ${s} in Quotes\"";

  static final String QUOTED1_1_WITH_2SEP = "\"value ${l} 1_1 ${s} in Quotes\"";
  static final String QUOTED1_2_WITH_2SEP = "\"value ${l} 1_2 ${s} in Quotes\"";
  static final String QUOTED2_1_WITH_2SEP = "\"value ${l} 2_1 ${s} in Quotes\"";
  static final String QUOTED2_2_WITH_2SEP = "\"value ${l} 2_2 ${s} in Quotes\"";

  /**
   * Buffer bigger than input
   * Single char column separator
   * Single char line separator
   * The last line DOES contain final column separator
   */
  @Test
  public void testBufferBiggerWithoutQuatesSingleLine() {
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(
        fromTemplate(singleChLineSingleChColumn,
          INPUT_SINGLE_LINE_TEMPLATE_WITH_FINAL_SEP),
        StandardCharsets.UTF_8))
      .encoding(StandardCharsets.UTF_8)
      .lineSeparator(SINGLE_CHAR_LINE_SEPARATOR)
      .columnSeparator(SINGLE_CHAR_COLUMN_SEPARATOR)
      .bufferSize(BUFFER_SIZE)
      .build();
    r.calculateHeaders();
    runAndValidate1NotEmptyLines(r);
  }

  /**
   * Buffer smaller than input
   * Single char column separator
   * Single char line separator
   * The last line DOES contain final column separator
   */
  @Test
  public void testBufferSmallerWithoutQuatesSingleLine() {
    String input = fromTemplate(
      singleChLineSingleChColumn,
      INPUT_SINGLE_LINE_TEMPLATE_WITH_FINAL_SEP);
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(input, StandardCharsets.UTF_8))
      .encoding(StandardCharsets.UTF_8)
      .lineSeparator(SINGLE_CHAR_LINE_SEPARATOR)
      .columnSeparator(SINGLE_CHAR_COLUMN_SEPARATOR)
      .bufferSize(input.length() - 5)
      .build();
    r.calculateHeaders();
    runAndValidate1NotEmptyLines(r);
  }

  /**
   * Buffer BIGGER than input
   * Single char column separator
   * Single char line separator
   * The last line DOES NOT contain final column separator
   */
  @Test
  public void testBufferBiggerWithoutQuatesSingleLine2() {
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(
        fromTemplate(singleChLineSingleChColumn,
          INPUT_SINGLE_LINE_TEMPLATE_WITHOUT_FINAL_SEP),
        StandardCharsets.UTF_8))
      .encoding(StandardCharsets.UTF_8)
      .lineSeparator(SINGLE_CHAR_LINE_SEPARATOR)
      .columnSeparator(SINGLE_CHAR_COLUMN_SEPARATOR)
      .bufferSize(BUFFER_SIZE)
      .build();
    r.calculateHeaders();
    runAndValidate1NotEmptyLines(r);
  }

  /**
   * Buffer SMALLER than input
   * Single char column separator
   * Single char line separator
   * The last line DOES NOT contain final column separator
   */
  @Test
  public void testBufferSmallerWithoutQuatesSingleLine2() {
    String input = fromTemplate(singleChLineSingleChColumn,
      INPUT_SINGLE_LINE_TEMPLATE_WITHOUT_FINAL_SEP);
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(input, StandardCharsets.UTF_8))
      .encoding(StandardCharsets.UTF_8)
      .lineSeparator(SINGLE_CHAR_LINE_SEPARATOR)
      .columnSeparator(SINGLE_CHAR_COLUMN_SEPARATOR)
      .bufferSize(input.length() - 5)
      .build();
    r.calculateHeaders();
    runAndValidate1NotEmptyLines(r);
  }

  /**
   * Buffer bigger than input
   * Single char column separator
   * Single char line separator
   * 2 lines INPUT
   */
  @Test
  public void testBufferBiggerSingColSepWithoutQuotes() {
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(
        fromTemplate(singleChLineSingleChColumn, INPUT1_TEMPLATE),
        StandardCharsets.UTF_8))
      .encoding(StandardCharsets.UTF_8)
      .lineSeparator(SINGLE_CHAR_LINE_SEPARATOR)
      .columnSeparator(SINGLE_CHAR_COLUMN_SEPARATOR)
      .bufferSize(BUFFER_SIZE)
      .build();
    r.calculateHeaders();
    runAndValidate2NotEmptyLines(r);
  }

  /**
   * Buffer SMALLER than input
   * Single char column separator
   * Single char line separator
   * 2 lines INPUT
   */
  @Test
  public void testBufferSmallerSingColSepWithoutQuotes() {
    String input = fromTemplate(singleChLineSingleChColumn, INPUT1_TEMPLATE);
    CsvReader r = CsvReader.builder().input(
      IOUtils.toInputStream(input, StandardCharsets.UTF_8))
      .encoding(StandardCharsets.UTF_8)
      .lineSeparator(SINGLE_CHAR_LINE_SEPARATOR)
      .columnSeparator(SINGLE_CHAR_COLUMN_SEPARATOR)
      .bufferSize(input.length() - 5)
      .build();
    r.calculateHeaders();
    runAndValidate2NotEmptyLines(r);
  }

  /**
   * Buffer SMALLER than input
   * Single char column separator
   * Mutiple char line separator
   * 2 lines INPUT
   */
  @Test
  public void testBufferSmallerMultColSepWithoutQuotes() {
    Map<String, String> map = Maps.newHashMap(singleChLineSingleChColumn);
    map.put("s", MULTIPLE_CHARS_LINE_SEPARATOR); //override it
    String input = fromTemplate(map, INPUT1_TEMPLATE);
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(input, StandardCharsets.UTF_8))
      .encoding(StandardCharsets.UTF_8)
      .lineSeparator(MULTIPLE_CHARS_LINE_SEPARATOR)
      .columnSeparator(SINGLE_CHAR_COLUMN_SEPARATOR)
      .bufferSize(input.length() - 5)
      .build();
    r.calculateHeaders();
    runAndValidate2NotEmptyLines(r);
  }

  /**
   * Buffer BIGGER than input
   * Single char column separator
   * Mutiple char line separator
   * 2 lines INPUT
   */
  @Test
  public void testBufferBiggerMultColSepWithoutQuotes() {
    Map<String, String> map = Maps.newHashMap(singleChLineSingleChColumn);
    map.put("s", MULTIPLE_CHARS_LINE_SEPARATOR); //override it
    String input = fromTemplate(map, INPUT1_TEMPLATE);
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(input, StandardCharsets.UTF_8))
      .encoding(StandardCharsets.UTF_8)
      .lineSeparator(MULTIPLE_CHARS_LINE_SEPARATOR)
      .columnSeparator(SINGLE_CHAR_COLUMN_SEPARATOR)
      .bufferSize(BUFFER_SIZE)
      .build();
    r.calculateHeaders();
    runAndValidate2NotEmptyLines(r);
  }


  /**
   * Buffer BIGGER than input
   * Single char column separator
   * Mutiple char line separator
   * 2 lines INPUT WITH Quates
   */
  @Test
  public void testBufferBiggerMultColSepWithQuotes() {
    Map<String, String> map = Maps.newHashMap(singleChLineSingleChColumn);
    map.put("s", MULTIPLE_CHARS_LINE_SEPARATOR); //override it
    map.put(LINE1_VAL1, QUOTED1_1);
    map.put(LINE2_VAL1, QUOTED2_1);
    map.put(LINE1_VAL2, QUOTED1_2);
    map.put(LINE2_VAL2, QUOTED2_2);
    String input = fromTemplate(map, INPUT1_TEMPLATE);
    CsvReader r = CsvReader.builder()
      .input(
        IOUtils.toInputStream(input, StandardCharsets.UTF_8))
      .encoding(StandardCharsets.UTF_8)
      .lineSeparator(MULTIPLE_CHARS_LINE_SEPARATOR)
      .columnSeparator(SINGLE_CHAR_COLUMN_SEPARATOR)
      .bufferSize(BUFFER_SIZE)
      .build();
    r.calculateHeaders();
    Assert.assertTrue(r.csvRecord.isEmpty());
    r.handleCsvLine();
    Assert.assertFalse(r.csvRecord.isEmpty());
    Assert.assertEquals(QUOTED1_1, r.csvRecord.get(COL1));
    Assert.assertEquals(QUOTED1_2, r.csvRecord.get(COL2));

    r.handleCsvLine();
    Assert.assertFalse(r.csvRecord.isEmpty());
    Assert.assertEquals(QUOTED2_1, r.csvRecord.get(COL1));
    Assert.assertEquals(QUOTED2_2, r.csvRecord.get(COL2));
  }

  /**
   * Buffer BIGGER than input
   * Single char column separator
   * Mutiple char line separator
   * 2 lines INPUT WITH Quates AND line separator inside
   */
  @Test
  public void testBufferBiggerMultColSepWithQuotesAndSep() {
    Map<String, String> map = Maps.newHashMap(singleChLineSingleChColumn);
    map.put("s", MULTIPLE_CHARS_LINE_SEPARATOR); //override it
    String v1 = fromTemplate(
      ImmutableMap.of("s", MULTIPLE_CHARS_LINE_SEPARATOR),
      QUOTED1_1_WITH_SEP);
    map.put(LINE1_VAL1, v1);
    String v2 = fromTemplate(
      ImmutableMap.of("s", MULTIPLE_CHARS_LINE_SEPARATOR),
      QUOTED2_1_WITH_SEP);
    map.put(LINE2_VAL1, v2);

    String v3 = fromTemplate(
      ImmutableMap.of("s", MULTIPLE_CHARS_LINE_SEPARATOR),
      QUOTED1_2_WITH_SEP);
    map.put(LINE1_VAL2, v3);

    String v4 = fromTemplate(
      ImmutableMap.of("s", MULTIPLE_CHARS_LINE_SEPARATOR),
      QUOTED2_2_WITH_SEP);
    map.put(LINE2_VAL2, v4);
    String input = fromTemplate(map, INPUT1_TEMPLATE);
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(input, StandardCharsets.UTF_8))
      .encoding(StandardCharsets.UTF_8)
      .lineSeparator(MULTIPLE_CHARS_LINE_SEPARATOR)
      .columnSeparator(SINGLE_CHAR_COLUMN_SEPARATOR)
      .bufferSize(BUFFER_SIZE)
      .build();
    r.calculateHeaders();
    Assert.assertTrue(r.csvRecord.isEmpty());
    r.handleCsvLine();
    Assert.assertFalse(r.csvRecord.isEmpty());
    Assert.assertEquals(v1.substring(1, v1.length() - 1), r.csvRecord.get(COL1));
    Assert.assertEquals(v3.substring(1, v1.length() - 1), r.csvRecord.get(COL2));

    r.handleCsvLine();
    Assert.assertFalse(r.csvRecord.isEmpty());
    Assert.assertEquals(v2.substring(1, v1.length() - 1), r.csvRecord.get(COL1));
    Assert.assertEquals(v4.substring(1, v1.length() - 1), r.csvRecord.get(COL2));
  }

  /**
   * Buffer BIGGER than input
   * Single char column separator
   * Mutiple char line separator
   * 2 lines INPUT WITH Quates AND both line and column separator inside quotes
   */
  @Test
  public void testBufferBiggerMultColSepWithQuotesAndBothSep() {
    Map<String, String> map = Maps.newHashMap(singleChLineSingleChColumn);
    map.put("s", MULTIPLE_CHARS_LINE_SEPARATOR); //override it

    String v1 = fromTemplate(
      ImmutableMap.of("s", MULTIPLE_CHARS_LINE_SEPARATOR,
        "l", SINGLE_CHAR_COLUMN_SEPARATOR),
      QUOTED1_1_WITH_2SEP);
    map.put(LINE1_VAL1, v1);
    String v2 = fromTemplate(
      ImmutableMap.of("s", MULTIPLE_CHARS_LINE_SEPARATOR,
        "l", SINGLE_CHAR_COLUMN_SEPARATOR),
      QUOTED2_1_WITH_2SEP);
    map.put(LINE2_VAL1, v2);

    String v3 = fromTemplate(
      ImmutableMap.of("s", MULTIPLE_CHARS_LINE_SEPARATOR,
        "l", SINGLE_CHAR_COLUMN_SEPARATOR),
      QUOTED1_2_WITH_2SEP);
    map.put(LINE1_VAL2, v3);

    String v4 = fromTemplate(
      ImmutableMap.of("s", MULTIPLE_CHARS_LINE_SEPARATOR,
        "l", SINGLE_CHAR_COLUMN_SEPARATOR),
      QUOTED2_2_WITH_2SEP);
    map.put(LINE2_VAL2, v4);
    String input = fromTemplate(map, INPUT1_TEMPLATE);
    CsvReader r = CsvReader.builder()
      .input(IOUtils.toInputStream(input, StandardCharsets.UTF_8))
      .encoding(StandardCharsets.UTF_8)
      .lineSeparator(MULTIPLE_CHARS_LINE_SEPARATOR)
      .columnSeparator(SINGLE_CHAR_COLUMN_SEPARATOR)
      .bufferSize(BUFFER_SIZE).build();
    r.calculateHeaders();
    Assert.assertTrue(r.csvRecord.isEmpty());
    r.handleCsvLine();
    Assert.assertFalse(r.csvRecord.isEmpty());
    Assert.assertEquals(v1.substring(1, v1.length() - 1), r.csvRecord.get(COL1));
    Assert.assertEquals(v3.substring(1, v1.length() - 1), r.csvRecord.get(COL2));

    r.handleCsvLine();
    Assert.assertFalse(r.csvRecord.isEmpty());
    Assert.assertEquals(v2.substring(1, v1.length() - 1), r.csvRecord.get(COL1));
    Assert.assertEquals(v4.substring(1, v1.length() - 1), r.csvRecord.get(COL2));
  }

  private void runAndValidate1NotEmptyLines(final CsvReader r) {
    Assert.assertTrue(r.csvRecord.isEmpty());
    r.handleCsvLine(); //1st line
    Assert.assertFalse(r.csvRecord.isEmpty());
    Assert.assertEquals(LINE1_VAL1, r.csvRecord.get(COL1));
    Assert.assertEquals(LINE1_VAL2, r.csvRecord.get(COL2));
    r.handleCsvLine(); //2nd line
    Assert.assertTrue(r.csvRecord.isEmpty());
  }

  private void runAndValidate2NotEmptyLines(final CsvReader r) {
    Assert.assertTrue(r.csvRecord.isEmpty());
    r.handleCsvLine(); //1st line
    Assert.assertFalse(r.csvRecord.isEmpty());
    Assert.assertEquals(LINE1_VAL1, r.csvRecord.get(COL1));
    Assert.assertEquals(LINE1_VAL2, r.csvRecord.get(COL2));
    r.handleCsvLine(); //2nd line
    Assert.assertFalse(r.csvRecord.isEmpty());
    Assert.assertEquals(LINE2_VAL1, r.csvRecord.get(COL1));
    Assert.assertEquals(LINE2_VAL2, r.csvRecord.get(COL2));
  }

}