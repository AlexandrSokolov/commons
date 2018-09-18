package com.savdev.commons;

import com.google.common.io.Files;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class TestUtils {

  private TestUtils() {
    throw new AssertionError("Utility class cannot be instantiated");
  }

  public static String testResourceFolderFullPath(final String folderName) {
    URL url = TestUtils.class.getClassLoader().getResource(folderName);
    try {
      return new File(url.toURI()).getAbsolutePath();
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

  public static String testResourceNotExistingFolderFullPath(final String folderName) {
    String path = testResourceFolderFullPath() +
      File.separator + folderName;
    if (new File(path).exists()){
      throw new IllegalArgumentException(
        String.format("Not expected existing folder %s, " +
          "use testResourceFolderFullPath method instead", path));
    }
    return path;
  }

  public static String testResourceFolderFullPath() {
    URL url = TestUtils.class.getClassLoader().getResource(".");
    try {
      return new File(url.toURI()).getAbsolutePath();
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

}