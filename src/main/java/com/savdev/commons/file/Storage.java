package com.savdev.commons.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * Is not expected to be public available. Used only by CsvReader
 */
class Storage {

  private final static int UNLIMITED = -1;

  final int bufferSize;
  final InputStream input;

  final List<BufferInfo> storage = new LinkedList<>();
  final BufferedReader reader;
  Position currentReadPosition = Position.builder().build();

  public Storage(
    final int bufferSize,
    final InputStream input,
    final Charset encoding) {
    this.bufferSize = bufferSize;
    this.input = input;
    reader = new BufferedReader(new InputStreamReader(input, encoding));
  }

  /**
   *
   * @return true if input has some data
   */
  boolean read(){
    BufferInfo buffer = getStoredBuffer();
    try {
      int c = reader.read(buffer.buffer, 0, bufferSize);
      if (c == -1){
        storage.remove(storage.size()-1);
        return false;
      }
      buffer.actualSize(c);
      return true;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private BufferInfo getStoredBuffer(){
    if (currentReadPosition.listPosition > 0
      && !storage.isEmpty()){
      return storage.get(0);
    } else {
      char[] a = new char[bufferSize];
      BufferInfo b = BufferInfo.builder()
        .buffer(a)
        .build();
      storage.add(b);
      return b;
    }
  }

  /**
   * Returns a value till the end of currently read data
   *  TODO, probably it is a better to read all data, and return all data
   * @return
   */
  String value(){
    if (storage.isEmpty()){
      read();
    }

    return value(Position.builder()
      .length(UNLIMITED) //unlimited
      .listPosition(storage.size() - 1)
      .arrayPosition(!storage.isEmpty() ?
        storage.get(storage.size()-1).actualSize : 0)
      .build());
  }

  String value(Position toPosition){
    if (storage.isEmpty()){
      read();
    }
    int totalBufferSize = bufferSize(currentReadPosition, toPosition);
    char[] totalBuffer = new char[totalBufferSize];

    int currentTargetPosition = 0;
    for (int i = 0; i <= toPosition.listPosition; i++) {
      BufferInfo bufferInfo = storage.get(i);
      if (bufferInfo.isEmpty()){
        throw new IllegalStateException("Buffer cannot be empty");
      }
      if (i == 0){ //the 1st buffer
        int length = lengthFstBuffer(toPosition);
        System.arraycopy(bufferInfo.buffer, currentReadPosition.arrayPosition,
          totalBuffer, currentTargetPosition, length);
        currentTargetPosition += length;
      } else if (i == toPosition.listPosition) { //the last buffer
        int length = lengthLstBuffer(toPosition);
        System.arraycopy(bufferInfo.buffer, 0,
          totalBuffer, currentTargetPosition, length);
        currentTargetPosition += length;
      } else {
        System.arraycopy(bufferInfo.buffer, 0,
          totalBuffer, currentTargetPosition, bufferInfo.actualSize);
        currentTargetPosition += bufferInfo.actualSize;
      }
    }
    for (int i = 0; i < toPosition.listPosition; i++){
      storage.remove(0);
    }

    if (!storage.isEmpty()
      && toPosition.arrayPosition == storage.get(0).actualSize){
      currentReadPosition = Position.builder()
        .listPosition(0)
        .arrayPosition(0)
        .build();
      storage.get(0).actualSize = 0;
    } else {
      currentReadPosition = Position.builder()
        .listPosition(0)
        .arrayPosition(toPosition.arrayPosition)
        .build();
    }
    movePositionAfter(toPosition.length, 0);
    return new String(totalBuffer);
  }

  /**
   * Tries to find a search string in input
   *  in in the current buffers there is no such information,
   *  it reads by itself more amound data if exists
   * @param search
   * @return
   */
  Position positionOf(String search){
    Position partlyFoundPosition = null;
    int foundSymbols = 0;
    for(int list = currentReadPosition.listPosition; list < storage.size(); list++){
      for(
        int searchPos = 0, el = (list == currentReadPosition.listPosition)
          ? currentReadPosition.arrayPosition : 0;
        el + searchPos < storage.get(list).actualSize; el++){
        for (;
             (el + searchPos < storage.get(list).actualSize)
               && searchPos < Math.min(search.length(), storage.get(list).actualSize);
             searchPos++){
          if (storage.get(list).buffer[el + searchPos] == search.charAt(foundSymbols)){
            if (partlyFoundPosition == null){
              partlyFoundPosition = Position.builder()
                .isFound(true)
                .length(search.length())
                .listPosition(list)
                .arrayPosition(el)
                .build();

            }
            foundSymbols++;
            if (foundSymbols == search.length()){
              return partlyFoundPosition;
            }
          } else {
            partlyFoundPosition = null;
            foundSymbols = 0;
            searchPos = 0;
            break;
          }
        }
      }
    }
    //we could not find it, try to read more data
    if (storage.isEmpty()){
      read();
      return positionOf(search);
    } else if (read()) {
      return positionOf(search);
    } else {
      return Position.builder().isFound(false).build();
    }

  }

  private int bufferSize(Position from, Position to){
    if (UNLIMITED != to.length) {
      if (to.listPosition < from.listPosition) {
        throw new IllegalStateException(
          "Cannot 'to' list position be before 'from' list position");
      }
      if (from.listPosition == to.listPosition
        && to.arrayPosition < from.arrayPosition) {
        throw new IllegalStateException(
          "Cannot 'to' array position be before 'from' array position in the same list");
      }
    }
    int length = 0;
    for (int i = 0; i <= to.listPosition; i++) {
      BufferInfo bufferInfo = storage.get(i);
      if (bufferInfo.isEmpty()){
        throw new IllegalStateException("Buffer cannot be empty");
      }
      if (i == 0){ //the 1st buffer
        length += lengthFstBuffer(to);
      } else if (i == to.listPosition) { //the last buffer
        length += lengthLstBuffer(to);
      } else {
        length += bufferInfo.actualSize;
      }
    }
    return length;
  }

  private int lengthFstBuffer(Position to){
    if (storage.get(0).actualSize < currentReadPosition.arrayPosition){
      throw new IllegalStateException(
        "Cannot actual size be less than current read position");
    }
    if (to.listPosition == 0
      && storage.get(0).actualSize < to.arrayPosition){
      throw new IllegalStateException(
        "Cannot actual size be less than 'to' position");
    }
    if (storage.get(0).actualSize > bufferSize){
      throw new IllegalStateException(
        "Cannot actual size be more than the full buffer size");
    }

    return to.listPosition == 0
      ? to.arrayPosition - currentReadPosition.arrayPosition
      : storage.get(0).actualSize - currentReadPosition.arrayPosition;
  }

  private int lengthLstBuffer(Position to){
    if (to.listPosition == 0){
      throw new IllegalStateException("Wrong method usage. " +
        "Can be used only for the last buffer in case more than one buffers participate");
    }
    if (storage.get(to.listPosition).actualSize < to.arrayPosition){
      throw new IllegalStateException(
        "Cannot actual size be less than required 'to' position");
    }
    return to.arrayPosition;
  }

  /**
   * Moves position to the next symbol after the input parameter
   *  if the current position = 1, length = 1, moves position to 3, but not to 2!
   *
   * return true if it could move current read position
   *  returns false if there is not enough data in input, to move current read position
   * @param totalLength
   * @return
   */
  private boolean movePositionAfter(int totalLength, int currentSum){
    if (UNLIMITED == totalLength){
      storage.clear();
      currentReadPosition = Position.builder().build();
      return true;
    }
    while(totalLength != currentSum || storage.size() > 1){
      for (int i = currentReadPosition.arrayPosition;
           i < storage.get(0).actualSize; i++) {
        currentSum++;
        if (currentSum == totalLength){
          currentReadPosition = Position.builder()
            .listPosition(0)
            .arrayPosition(i+1)
            .build();
          return true;
        }
      }
      //set it to the 1st position of the next line
      currentReadPosition = Position.builder()
        .listPosition(0)
        .arrayPosition(0)
        .build();
      storage.remove(0); //remove the 1st buffer
    }
    if (read()){
      return movePositionAfter(totalLength, currentSum);
    } else {
      return false;
    }
  }
}
