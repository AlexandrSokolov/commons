package com.savdev.commons.file;

public class BufferInfo {

  final char[] buffer;
  int actualSize; //number of exactly read chars

  public BufferInfo(char[] buffer, int actualSize) {
    this.buffer = buffer;
    this.actualSize = actualSize;
  }

  public boolean isEmpty(){
    return this.actualSize == -1;
  }

  public void actualSize(int actualSize) {
    this.actualSize = actualSize;
  }

  public static BufferInfoBuilder builder(){
    return new BufferInfoBuilder();
  }

  public static class BufferInfoBuilder {
    private char[] buffer;
    private int actualSize;

    public BufferInfoBuilder buffer(char[] buffer) {
      this.buffer = buffer;
      return this;
    }

    public BufferInfoBuilder actualSize(int actualSize) {
      this.actualSize = actualSize;
      return this;
    }

    public BufferInfo build() {
      return new BufferInfo(buffer, actualSize);
    }
  }
}
