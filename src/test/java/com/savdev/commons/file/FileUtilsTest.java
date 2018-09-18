package com.savdev.commons.file;

import com.savdev.commons.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;

import static com.savdev.commons.file.FileUtils.fileUtils;

public class FileUtilsTest {

  static final String FOLDER = "file";
  static final String FOLDER_NOT_EXISTING = "file_not_existing";
  static final String FILE = "test.empty.txt";
  static final String FILE_NOT_EXISTING = "not.existing.txt";

  @Test
  public void testSuccessful(){
    InputStream i = fileUtils().validFile(
      TestUtils.testResourceFolderFullPath(FOLDER), FILE);
    Assert.assertNotNull(i);
  }

  @Test
  public void testEmptyFolder(){
    try {
      fileUtils().validFile("", FILE);
      Assert.fail("Must not allow empty folder as a parameter");
    } catch (Exception e){
      Assert.assertEquals(IllegalStateException.class, e.getClass());
      Assert.assertEquals(fileUtils().EMPTY_FOLDER_PARAMETER, e.getMessage());
    }
  }

  @Test
  public void testNullableFileParameter(){
    try {
      fileUtils().validFile(FOLDER, null);
      Assert.fail("Must not allow null as a parameter for file");
    } catch (Exception e){
      Assert.assertEquals(IllegalStateException.class, e.getClass());
      Assert.assertEquals(fileUtils().EMPTY_FILE_PARAMETER, e.getMessage());
    }
  }

  @Test
  public void testNotExistingFolder(){
    String folder = TestUtils.testResourceNotExistingFolderFullPath(
      FOLDER_NOT_EXISTING);
    try {
      fileUtils().validFile(folder, FILE);
      Assert.fail("Must not allow not existing folder as a parameter");
    } catch (Exception e){
      Assert.assertEquals(IllegalStateException.class, e.getClass());
      Assert.assertEquals(
        String.format(fileUtils().NOT_EXISTING_FOLDER, folder),
        e.getMessage());
    }
  }

  @Test
  public void testNotExistingFile(){
    String folder = TestUtils.testResourceFolderFullPath(FOLDER);
    try {
      fileUtils().validFile(folder, FILE_NOT_EXISTING);
      Assert.fail("Must not allow not existing files as a parameter");
    } catch (Exception e){
      Assert.assertEquals(IllegalStateException.class, e.getClass());
      Assert.assertEquals(
        String.format(
          fileUtils().NOT_EXISTING_FILE,
          folder + File.separator + FILE_NOT_EXISTING),
        e.getMessage());
    }
  }

}
