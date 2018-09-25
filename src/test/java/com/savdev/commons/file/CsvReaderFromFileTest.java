package com.savdev.commons.file;

import com.savdev.commons.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

import static com.savdev.commons.file.FileUtils.fileUtils;

public class CsvReaderFromFileTest {

  static final String FILE = "F_PRDAUFT.dsv";
  static final int EXPECTED_LINES_NUMBER = 3;

  @Test
  public void testCsvReaderFromFile(){
    InputStream i = fileUtils().validFile(
      TestUtils.testResourceFolderFullPath(FileUtilsTest.FOLDER),
      FILE);
    Assert.assertNotNull(i);
    CsvReader r = CsvReader.builder()
      .input(i)
      .columnSeparator("||")
      .lineSeparator("{EOL}")
      .build();
    final int[] total = {0};
    r.csvLines().forEach(map -> {
      total[0]++;
    });
    Assert.assertEquals(EXPECTED_LINES_NUMBER, total[0]);
  }
}
