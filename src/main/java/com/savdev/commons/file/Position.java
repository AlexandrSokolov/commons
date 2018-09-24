package com.savdev.commons.file;

import java.util.Objects;

public class Position {
  final boolean isFound;
  final int listPosition;
  final int arrayPosition;
  final int length;

  private Position(
    final boolean isFound,
    final int listPosition,
    final int arrayPosition,
    final int length) {
    this.isFound = isFound;
    this.listPosition = listPosition;
    this.arrayPosition = arrayPosition;
    this.length = length;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Position position = (Position) o;
    return isFound == position.isFound &&
      listPosition == position.listPosition &&
      arrayPosition == position.arrayPosition &&
      length == position.length;
  }

  @Override
  public int hashCode() {

    return Objects.hash(isFound, listPosition, arrayPosition, length);
  }

  public static PositionBuilder builder(){
    return new PositionBuilder();
  }

  public static class PositionBuilder {
    private int listPosition;
    private int arrayPosition;
    private int length;
    private boolean isFound;

    public PositionBuilder listPosition(int listPosition) {
      this.listPosition = listPosition;
      return this;
    }

    public PositionBuilder isFound(boolean isFound) {
      this.isFound = isFound;
      return this;
    }

    public PositionBuilder arrayPosition(int arrayPosition) {
      this.arrayPosition = arrayPosition;
      return this;
    }

    public PositionBuilder length(int length) {
      this.length = length;
      return this;
    }

    public Position build() {
      return new Position(isFound, listPosition, arrayPosition, length);
    }
  }


}
