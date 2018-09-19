package com.savdev.commons.file;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CsvReader {

  static final int BUFFER_SIZE = 12048;

  final char[] charArray; //= new char[BUFFER_SIZE];
  final BufferedReader buffer;
  final String csvLineSeparator;
  final String csvColumnSeparator;

  //to be able to override it in tests
  final int sizeBuffer;

  //calculated state:
  Map<Integer, CsvColumnMetadata> csvHeader = Maps.newLinkedHashMap();
  Map<String, String> csvRecord = Maps.newHashMap();
  int currentCharArrayPosition = -1;

  public CsvReader(
    final InputStream input,
    final Charset encoding,
    final String csvLineSeparator,
    final String csvColumnSeparator,
    final int bufferSize
  ) {
    this.csvLineSeparator = csvLineSeparator;
    this.csvColumnSeparator = csvColumnSeparator;
    this.sizeBuffer = bufferSize;
    charArray = new char[bufferSize];
    this.buffer = new BufferedReader(new InputStreamReader(input, encoding));
  }

  int headerPosition(){
    return headerPosition(new StringBuilder(0));
  }

  int headerPosition(StringBuilder header){
    try {
      int c = buffer.read(charArray, 0, sizeBuffer);
      String fullHeader = header.append(new String(charArray)).toString();
      if (c > 0){
        if (fullHeader.contains(csvLineSeparator)){
          int position = fullHeader.indexOf(csvLineSeparator);
          String csvHeaderAsString = fullHeader.substring(0, position);
          final int[] columnPosition = {0};
          Pattern.compile(
            String.format("\\%s+",csvColumnSeparator))
            .splitAsStream(csvHeaderAsString)
            .forEach(column -> {
              int currentPosition = columnPosition[0]++;
              csvHeader.put(
                currentPosition,
                CsvColumnMetadata.builder()
                  .position(currentPosition)
                  .name(column)
                  .build());
            });
          currentCharArrayPosition = position + csvLineSeparator.length();
          return currentCharArrayPosition;
        } else {
          if (c == sizeBuffer) {
            return headerPosition(new StringBuilder(fullHeader));
          } else {
            throw new IllegalArgumentException(
              String.format("Could not find csv line separator %s", csvLineSeparator));
          }
        }
      } else {
        throw new IllegalArgumentException(
          String.format("Could not find csv line separator %s", csvLineSeparator));
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  void handleCsvLine(){
    csvRecord = Maps.newHashMap();
    csvHeader.entrySet().stream()
      .forEach(entry -> {
        if (currentCharArrayPosition != -1){
          int firstNullableElement = nullablePosition(charArray);
          int length = charArray.length - currentCharArrayPosition;
          if(firstNullableElement != -1
            && firstNullableElement < length) {
            length = firstNullableElement;
          }

          StringBuilder partLine = new StringBuilder(
            java.nio.CharBuffer.wrap(
              charArray,
              currentCharArrayPosition,
              length));

          StringBuilder fullLine = (entry.getKey() == (csvHeader.size() -1)) ?
            lastColumn(partLine, entry.getKey()) :
            notLastColumn(partLine, entry.getKey());

          int foundPos;
          if(entry.getKey() != (csvHeader.size() -1)) {
            foundPos = handleNotLastColumn(fullLine, entry.getKey());
          } else {
            foundPos = handleLastColumn(fullLine, entry.getKey());
          }

          updateCharArrayPosition(foundPos, fullLine);
        }
      });
  }

  private int nullablePosition(StringBuilder line){
    for(int i = 0; i < line.length(); i++) {
      if(line.charAt(i) == 0)
        return i;
    }
    return -1;
  }

  private int nullablePosition(char[] chars){
    for(int i = 0; i < chars.length; i++) {
      if(chars[i] == 0)
        return i;
    }
    return -1;
  }

  private int handleLastColumn(
    final StringBuilder fullLine,
    final int columnPosition){
    if (columnPosition != (csvHeader.size() -1)){
      throw new IllegalArgumentException(
        "Expected to handle the last column only");
    }
    if (StringUtils.isEmpty(csvHeader.get(columnPosition).endSeparator)){
      updateColumnSeparatorMeta(
        csvLineSeparator,
        columnPosition,
        fullLine);
    }
    String s = csvHeader.get(columnPosition).endSeparator;
    int pos = fullLine.indexOf(s);
    if (pos != -1){
      //not the last csv line
      csvRecord.put(
        csvHeader.get(columnPosition).columnName,
        fullLine.substring(0, pos));
    } else {
      int firstNullableElement = nullablePosition(fullLine);
      csvRecord.put(
        csvHeader.get(columnPosition).columnName,
        firstNullableElement == -1 ?
          fullLine.toString() :
          fullLine.substring(0, firstNullableElement));
      return -1; //indicator, processing is over
    }
    return pos + s.length();
  }

  private int handleNotLastColumn(
    final StringBuilder fullLine,
    final int columnPosition){
    if (columnPosition == (csvHeader.size() -1)){
      throw new IllegalArgumentException(
        "Not expected to handle the last column");
    }
    if (StringUtils.isEmpty(csvHeader.get(columnPosition).endSeparator)){
      updateColumnSeparatorMeta(
        csvColumnSeparator,
        columnPosition,
        fullLine);
    }
    //it must be found for not last column
    String s = csvHeader.get(columnPosition).endSeparator;
    int pos = fullLine.indexOf(s);
    String value = fullLine.substring(0, pos);
    csvRecord.put(
      csvHeader.get(columnPosition).columnName,
      value);
    return pos + s.length();
  }

  private void updateColumnSeparatorMeta(
    String separator,
    int columnPosition,
    StringBuilder currentFullLine
  ){
    String colSeparatorWithDblQuate =
      String.format("\"%s", separator);
    int quotePos = currentFullLine.indexOf(colSeparatorWithDblQuate);
    int simplePos = currentFullLine.indexOf(separator);
    if (simplePos == quotePos){
      csvHeader.get(columnPosition).setStartSeparator("\"");
      csvHeader.get(columnPosition).setEndSeparator(colSeparatorWithDblQuate);
    } else {
      csvHeader.get(columnPosition).setStartSeparator("");
      csvHeader.get(columnPosition).setEndSeparator(separator);
    }
  }

  private void updateCharArrayPosition(
    final int foundSeparatorPosition,
    final StringBuilder fullLine){
    if (foundSeparatorPosition == -1) {
      currentCharArrayPosition = foundSeparatorPosition;
    } else if (fullLine.length() > charArray.length){
      currentCharArrayPosition = foundSeparatorPosition -
        (fullLine.length() % charArray.length)
        * charArray.length;
    } else {
      currentCharArrayPosition += foundSeparatorPosition;
    }
  }

  private StringBuilder notLastColumn(
    final StringBuilder currentLine,
    final int columnPosition){
    if (columnPosition == (csvHeader.size() -1)){
      throw new IllegalArgumentException(
        "Not expected to handle the last column");
    }
    int simplePos = currentLine.indexOf(csvColumnSeparator);
    if (simplePos == -1){
      try {
        int c = buffer.read(charArray, 0, sizeBuffer);
        if (c == -1){
          throw new IllegalStateException(
            "Could not find csv column separator");
        }
        return notLastColumn(currentLine.append(charArray), columnPosition);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    } else {
      return currentLine;
    }
  }

  private StringBuilder lastColumn(
    final StringBuilder currentLine,
    final int columnPosition){
    if (columnPosition != (csvHeader.size() -1)){
      throw new IllegalArgumentException(
        "Expected to handle the last column");
    }
    int simplePos = currentLine.indexOf(csvLineSeparator);
    if (simplePos == -1){
      try {
        int c = buffer.read(charArray, 0, sizeBuffer);
        if (c == -1){
          //the last csv record does not contain csvLineSeparator
          return currentLine;
        }
        return lastColumn(currentLine.append(charArray), columnPosition);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    } else {
      return currentLine;
    }
  }

  Stream<Map<String, String>> csvLines(){
    Iterator var1 = new Iterator<Map<String, String>>() {

      public boolean hasNext() {
        if (csvHeader.isEmpty()){
          CsvReader.this.headerPosition();
        }
        CsvReader.this.handleCsvLine();
        return !CsvReader.this.csvRecord.isEmpty();
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
}
