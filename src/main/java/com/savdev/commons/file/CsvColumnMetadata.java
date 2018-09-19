package com.savdev.commons.file;

import java.util.Objects;

public class CsvColumnMetadata {
  final int columnPosition;
  final String columnName;
  String startSeparator;
  String endSeparator;

  public CsvColumnMetadata(
    final int columnPosition,
    final String columnName,
    final String startSeparator,
    final String endSeparator) {
    if(columnPosition < 0){
      throw new IllegalArgumentException(
        String.format("Csv header position = '%d' cannot be negative",
          columnPosition));
    }
    this.columnPosition = columnPosition;
    this.columnName = columnName;
    this.startSeparator = startSeparator;
    this.endSeparator = endSeparator;
  }

  public void setStartSeparator(
    final String startSeparator) {
    this.startSeparator = startSeparator;
  }

  public void setEndSeparator(
    final String endSeparator) {
    this.endSeparator = endSeparator;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CsvColumnMetadata that = (CsvColumnMetadata) o;
    return columnPosition == that.columnPosition;
  }

  @Override
  public int hashCode() {

    return Objects.hash(columnPosition);
  }

  public static CsvColumnMetadataBuilder builder(){
    return new CsvColumnMetadataBuilder();
  }

  public static class CsvColumnMetadataBuilder {
    private int columnPosition;
    private String columnName;
    private String startSeparator;
    private String endSeparator;

    public CsvColumnMetadataBuilder position(int columnPosition) {
      this.columnPosition = columnPosition;
      return this;
    }

    public CsvColumnMetadataBuilder name(String columnName) {
      this.columnName = columnName;
      return this;
    }

    public CsvColumnMetadataBuilder startSeparator(String startSeparator) {
      this.startSeparator = startSeparator;
      return this;
    }

    public CsvColumnMetadataBuilder endSeparator(String endSeparator) {
      this.endSeparator = endSeparator;
      return this;
    }

    public CsvColumnMetadata build() {
      return new CsvColumnMetadata(
        columnPosition,
        columnName,
        startSeparator,
        endSeparator);
    }
  }
}
