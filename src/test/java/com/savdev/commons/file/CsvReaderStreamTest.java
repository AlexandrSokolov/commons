package com.savdev.commons.file;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static com.savdev.commons.TestUtils.fromTemplate;
import static com.savdev.commons.file.CsvReaderHeaderCalculationTest.*;

public class CsvReaderStreamTest {
  @Test
  public void testStream(){
    final int[] processedLine = {0};
    CsvReader.builder()
      .input(IOUtils.toInputStream(
        fromTemplate(singleChLineSingleChColumn, INPUT1_TEMPLATE),
        StandardCharsets.UTF_8)).encoding(StandardCharsets.UTF_8)
      .lineSeparator(SINGLE_CHAR_LINE_SEPARATOR)
      .columnSeparator(",")
      .bufferSize(BUFFER_SIZE)
      .build()
      .csvLines()
      .forEach(csvRecordAsMap -> {
        if (processedLine[0] == 0) {
          Assert.assertTrue(csvRecordAsMap.size() == 2);
          Assert.assertEquals(
            CsvReaderHeaderCalculationTest.LINE1_VAL1,
            csvRecordAsMap.get(CsvReaderHeaderCalculationTest.COL1));
          Assert.assertEquals(
            CsvReaderHeaderCalculationTest.LINE1_VAL2,
            csvRecordAsMap.get(CsvReaderHeaderCalculationTest.COL2));
        }
        if (processedLine[0] == 1) {
          Assert.assertTrue(csvRecordAsMap.size() == 2);
          Assert.assertEquals(
            CsvReaderHeaderCalculationTest.LINE2_VAL1,
            csvRecordAsMap.get(CsvReaderHeaderCalculationTest.COL1));
          Assert.assertEquals(
            CsvReaderHeaderCalculationTest.LINE2_VAL2,
            csvRecordAsMap.get(CsvReaderHeaderCalculationTest.COL2));
        }
        processedLine[0]++;
      });
    Assert.assertEquals(2, processedLine[0]);
  }
}

