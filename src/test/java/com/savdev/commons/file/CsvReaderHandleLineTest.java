package com.savdev.commons.file;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static com.savdev.commons.TestUtils.fromTemplate;
import static com.savdev.commons.file.CsvReaderHeaderCalculationTest.*;

public class CsvReaderHandleLineTest {

  @Test
  public void test(){
    CsvReader r = new CsvReader(
      IOUtils.toInputStream(fromTemplate(m, INPUT1_TEMPLATE), StandardCharsets.UTF_8),
      StandardCharsets.UTF_8,
      SINGLE_CHAR_LINE_SEPARATOR,
      ",",
      CsvReader.BUFFER_SIZE);
    r.headerPosition();
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