package com.savdev.commons.file;

import com.google.common.collect.Maps;
import com.savdev.commons.function.Executor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CsvReader {

  private static int BUFFER_SIZE = 32768;

  final Storage storage;
  final String csvLineSeparator;
  final String csvColumnSeparator;

  //calculated state:
  final Map<Integer, CsvColumnMetadata> csvHeader = Maps.newLinkedHashMap();
  boolean noMoreData = false;
  Map<String, String> csvRecord = Maps.newHashMap();

  private CsvReader(
    final InputStream input,
    final Charset encoding,
    final String csvLineSeparator,
    final String csvColumnSeparator,
    final int bufferSize
  ) {
    this.csvLineSeparator = csvLineSeparator;
    this.csvColumnSeparator = csvColumnSeparator;
    this.storage = new Storage(bufferSize, input, encoding);
  }

  public static CsvReaderBuilder builder(){
    return new CsvReaderBuilder();
  }

  public static class CsvReaderBuilder {
    private InputStream input;
    private Charset encoding = StandardCharsets.UTF_8;
    private String csvLineSeparator = System.lineSeparator();
    private String csvColumnSeparator = ",";
    private int bufferSize = BUFFER_SIZE;

    public CsvReaderBuilder input(
      @NotNull final InputStream input) {
      if (input == null){
        throw new IllegalArgumentException(
          "Cannot create reader, input cannot be null");
      }
      this.input = input;
      return this;
    }

    public CsvReaderBuilder encoding(
      @NotNull final Charset encoding) {
      if (encoding == null) {
        throw new IllegalArgumentException(
          "Cannot create reader, encoding cannot be null");
      }
      this.encoding = encoding;
      return this;
    }

    public CsvReaderBuilder lineSeparator(
      @NotNull final String csvLineSeparator) {
      if ( StringUtils.isEmpty(csvLineSeparator)){
        throw new IllegalArgumentException(
          "Cannot create reader, line separator cannot be empty");
      }
      this.csvLineSeparator = csvLineSeparator;
      return this;
    }

    public CsvReaderBuilder columnSeparator(
      @NotNull final String csvColumnSeparator) {
      if ( StringUtils.isEmpty(csvColumnSeparator)){
        throw new IllegalArgumentException(
          "Cannot create reader, column separator cannot be empty");
      }
      this.csvColumnSeparator = csvColumnSeparator;
      return this;
    }

    public CsvReaderBuilder bufferSize(
      final int bufferSize) {
      if (bufferSize == 0){
        throw new IllegalArgumentException(
          "Cannot create reader, buffer size cannot be 0");
      }
      this.bufferSize = bufferSize;
      return this;
    }

    public CsvReader build() {
      if (this.input == null){
        throw new IllegalArgumentException(
          "Cannot create reader, input is not defined");
      }
      return new CsvReader(
        input,
        encoding,
        csvLineSeparator,
        csvColumnSeparator,
        bufferSize);
    }
  }

  public Stream<Map<String, String>> csvLines(){
    Iterator var1 = new Iterator<Map<String, String>>() {

      public boolean hasNext() {
        if (csvHeader.isEmpty()){
          CsvReader.this.calculateHeaders();
        }
        if (CsvReader.this.noMoreData){
          return false;
        } else {
          CsvReader.this.handleCsvLine();
          return !CsvReader.this.csvRecord.isEmpty();
        }
      }

      public Map<String, String> next() {
        if (CsvReader.this.csvRecord.isEmpty()){
          throw new NoSuchElementException();
        } else {
          return CsvReader.this.csvRecord;
        }
      }
    };

    return StreamSupport.stream(
      Spliterators.spliteratorUnknownSize(var1, 272), false);
  }

  void calculateHeaders(){
    Position fstLineSeparatorPosition = storage.positionOf(
      this.csvLineSeparator);
    if (fstLineSeparatorPosition.isFound){
      String header = storage.value(fstLineSeparatorPosition);
      final int[] columnPosition = {0};
      Pattern.compile(
        String.format("\\%s+",csvColumnSeparator))
        .splitAsStream(header)
        .forEach(column -> {
          int currentPosition = columnPosition[0]++;
          csvHeader.put(
            currentPosition,
            CsvColumnMetadata.builder()
              .position(currentPosition)
              .name(column)
              .build());
        });
    } else {
      throw new IllegalArgumentException(
        String.format("Could not find csv line separator %s, header = 's'",
          csvLineSeparator, storage.value()));
    }
  }

  void handleCsvLine(){
    csvRecord = Maps.newHashMap();
    csvHeader.entrySet().stream()
      .filter(e -> !noMoreData)
      .forEach(entry -> {
        if (entry.getKey() == (csvHeader.size() -1)){ //the last column
          handleColumn(csvHeader.size()-1,
            csvLineSeparator, ()-> {
              //the last column in the last line:
              String value = storage.value();
              CsvColumnMetadata lastColumn = csvHeader.get(csvHeader.size() - 1);
              csvRecord.put(lastColumn.columnName,
                value.substring(lastColumn.startSeparator.length(),
                  value.length() - lastColumn.startSeparator.length()));
              noMoreData = true;
            });
        } else if (entry.getKey() == 0) {
          handleColumn(entry.getKey(),
            csvColumnSeparator, ()-> noMoreData = true);
        } else {
          handleColumn(entry.getKey(),
            csvColumnSeparator, ()-> {
              throw new IllegalStateException(
                String.format(
                  "Could not extract a value for not last column, current line = '%s'",
                  storage.value()));
            });
        }
      });
  }

  private void handleColumn(
    final int columnPosition,
    final String separator,
    final Executor notFound){
    if (StringUtils.isEmpty(csvHeader.get(columnPosition).endSeparator)){
      Position p = storage.positionOf(separator);
      if (p.isFound){
        setSeparatorAnd1stLine(columnPosition, separator, notFound);
      } else {
        notFound.execute();
      }
    } else {
      final Position p = storage.positionOf(
        csvHeader.get(columnPosition).endSeparator);
      if (p.isFound){
        csvRecord.put(
          csvHeader.get(columnPosition).columnName,
          storage.value(p)
            .substring(csvHeader.get(columnPosition).startSeparator.length()));
      } else {
        //the last column in the last line:
        notFound.execute();
      }
    }
  }

  private void setSeparatorAnd1stLine(
    final int columnPosition,
    final String separator,
    final Executor notFound){
    Position separatorPosition = storage.positionOf(separator);
    if (separatorPosition.isFound) {
      String value = storage.value(separatorPosition);
      if (value.charAt(0) == '"') {
        csvHeader.get(columnPosition).setEndSeparator(
          String.format("\"%s", separator));
        csvHeader.get(columnPosition).setStartSeparator("\"");
        //actually, if it looks as: "${csvColumnSeparator}
        if (value.charAt(value.length() - 1) == '"') {
          //skip " in the fst and last positions:
          csvRecord.put(csvHeader.get(columnPosition).columnName,
            value.substring(1, value.length() - 1));
        } else {
          Position p = storage.positionOf(
            csvHeader.get(columnPosition).endSeparator);
          if (p.isFound) {
            csvRecord.put(csvHeader.get(columnPosition).columnName,
              value.substring(1) + separator + storage.value(p));
          } else {
            notFound.execute();
          }
        }
      } else {
        csvHeader.get(columnPosition).setEndSeparator(separator);
        csvHeader.get(columnPosition).setStartSeparator(""); //empty value
        csvRecord.put(csvHeader.get(columnPosition).columnName, value);
      }
    } else {
      notFound.execute();
    }
  }
}
