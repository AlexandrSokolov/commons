package com.savdev.commons.file;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

  static final String EMPTY_FOLDER_PARAMETER =
    "Required not nullable config folder input parameter";
  static final String EMPTY_FILE_PARAMETER =
    "Not nullable file name parameter is expected.";
  static final String NOT_EXISTING_FOLDER = "Directory does exist: %s";
  static final String FOLDER_EXPECTED = "Directory is expected, but not simple file: %s";
  static final String FOLDER_CANNOT_BE_READ = "Directory exists, but cannot be read: %s";

  static final String NOT_EXISTING_FILE = "File does not exist: %s";
  static final String FILE_EXPECTED = "File is expected, but not directory: %s";
  static final String FILE_CANNOT_BE_READ = "File exists, but cannot be read: %s";



  public static FileUtils fileUtils() {
    return new FileUtils();
  }

  public InputStream validFile(
    final String configFolder,
    final String fileName) {
    if (StringUtils.isEmpty(configFolder)) {
      throw new IllegalStateException(EMPTY_FOLDER_PARAMETER);
    }
    if (StringUtils.isEmpty(fileName)) {
      throw new IllegalStateException(EMPTY_FILE_PARAMETER);
    }

    File folder = new File(configFolder);
    validateDirectory(folder);

    File configFile = new File(configFolder, fileName);
    validateFile(configFile);

    try {
      return new FileInputStream(configFile.getAbsolutePath());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private void validateDirectory(File file) {
    if (!file.exists()) {
      throw new IllegalStateException(
        String.format(NOT_EXISTING_FOLDER, file.getAbsolutePath()));
    }
    if (!file.isDirectory()) {
      throw new IllegalStateException(
        String.format(FOLDER_EXPECTED, file.getAbsolutePath()));
    }
    if (!file.canRead()) {
      throw new IllegalStateException(
        String.format(FOLDER_CANNOT_BE_READ, file.getAbsolutePath()));
    }
  }

  private void validateFile(File file) {
    if (!file.exists()) {
      throw new IllegalStateException(
        String.format(NOT_EXISTING_FILE, file.getAbsolutePath()));
    }
    if (file.isDirectory()) {
      throw new IllegalStateException(
        String.format(FILE_EXPECTED, file.getAbsolutePath()));
    }
    if (!file.canRead()) {
      throw new IllegalStateException(
        String.format(FILE_CANNOT_BE_READ, file.getAbsolutePath()));
    }
  }
}
