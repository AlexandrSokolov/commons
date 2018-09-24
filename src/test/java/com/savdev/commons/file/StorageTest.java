package com.savdev.commons.file;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class StorageTest {
  public static final String SHORT_INPUT = "12345";
  public static final String LONG_INPUT = "1234567890123456789012";

  @Test
  public void testReadAndGetBufferBigger(){
    Storage s = new Storage(10,
      IOUtils.toInputStream(SHORT_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    //read all data with one step, no more data in input stream:
    //not necessary to read it first:
    //getting a value, first reads it
    Assert.assertEquals(SHORT_INPUT, s.value());
  }

  @Test
  public void testReadEmptyAndGetBufferBigger(){
    Storage s = new Storage(10,
      IOUtils.toInputStream("", StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    Assert.assertTrue(StringUtils.isEmpty(s.value()));
  }

  @Test
  public void testReadAndGetBufferEquals2Input(){
    Storage s = new Storage(SHORT_INPUT.length(),
      IOUtils.toInputStream(SHORT_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    //read all data with one step,
    //number of read items equals to buffer size
    //storage consider it as still has not read all data
    Assert.assertTrue(s.read());
    Assert.assertEquals(SHORT_INPUT, s.value());
  }

  @Test
  public void testPositionOfSingleBufferSingleElementSearch1(){
    Storage s = new Storage(LONG_INPUT.length(),
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    s.read();
    Position p = s.positionOf("1");
    Assert.assertTrue(p.isFound);
    Assert.assertEquals(0, p.listPosition);
    Assert.assertEquals(0, p.arrayPosition);
  }

  @Test
  public void testPositionOfSingleBufferSingleElementSearch5(){
    Storage s = new Storage(LONG_INPUT.length(),
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    s.read();
    Position p = s.positionOf("5");
    Assert.assertTrue(p.isFound);
    Assert.assertEquals(0, p.listPosition);
    Assert.assertEquals(4, p.arrayPosition);
  }

  @Test
  public void testPositionOfSingleBufferSingleElementSearch5MultipleSearchs(){
    Storage s = new Storage(LONG_INPUT.length(),
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    s.read();
    //1st search
    Position p1 = s.positionOf("5");
    Assert.assertTrue(p1.isFound);
    Assert.assertEquals(0, p1.listPosition);
    Assert.assertEquals(4, p1.arrayPosition);
    String val1 = s.value(p1);
    Assert.assertEquals("1234", val1);

    //2nd search
    Position p2 = s.positionOf("5");
    Assert.assertTrue(p2.isFound);
    Assert.assertEquals(0, p2.listPosition);
    Assert.assertEquals(14, p2.arrayPosition);
    String val2 = s.value(p2);
    Assert.assertEquals("678901234", val2);
  }

  @Test
  public void testPositionOfSingleBufferMultipleElementSearch(){
    Storage s = new Storage(LONG_INPUT.length(),
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    s.read();
    Position p = s.positionOf("567");
    Assert.assertTrue(p.isFound);
    Assert.assertEquals(0, p.listPosition);
    Assert.assertEquals(4, p.arrayPosition);
  }

  @Test
  public void testPositionOfSingleBufferMultipleElementSearchMultipleSearchs(){
    Storage s = new Storage(LONG_INPUT.length(),
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    //1st search
    Position p1 = s.positionOf("567");
    Assert.assertTrue(p1.isFound);
    Assert.assertEquals(0, p1.listPosition);
    Assert.assertEquals(4, p1.arrayPosition);
    //2nd search with the same value, should return the same position
    Position p2 = s.positionOf("567");
    Assert.assertTrue(p1.equals(p2));
    Assert.assertEquals(0, s.currentReadPosition.arrayPosition);
    //extreact value, move to p2 position
    Assert.assertEquals("1234", s.value(p1));
    Assert.assertEquals(0, s.currentReadPosition.listPosition);
    Assert.assertEquals(7, s.currentReadPosition.arrayPosition);

    //3rd search with the same value, should return next found position
    Position p3 = s.positionOf("567");
    Assert.assertTrue(p3.isFound);
    Assert.assertEquals(0, p3.listPosition);
    Assert.assertEquals(14, p3.arrayPosition);
    Assert.assertEquals("8901234", s.value(p3));
    Assert.assertEquals(p3.arrayPosition + "567".length(),
      s.currentReadPosition.arrayPosition);
  }


  @Test
  public void testReadAndGetBufferSmaller2Input(){
    int buffSize = 4;
    Storage s = new Storage(buffSize,
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    //read all data with one step,
    //number of read items equals to buffer size
    //storage consider it as still has not read all data
    Assert.assertTrue(s.read());
    Assert.assertEquals(LONG_INPUT.substring(0, buffSize), s.value());
  }

  @Test
  public void testReadAllInputAndGetBufferSmaller2Input(){
    int buffSize = 4;
    Storage s = new Storage(buffSize,
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    while (s.read()) {}
    Assert.assertEquals(LONG_INPUT, s.value());
  }

  @Test
  public void testReadAllInputAndCheckExistingBuffersBufferSmaller2Input(){
    int buffSize = 4;
    Storage s = new Storage(buffSize,
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    Assert.assertTrue(s.storage.isEmpty());
    Assert.assertTrue(s.read());
    Assert.assertEquals(1, s.storage.size());
    Assert.assertTrue(s.read());
    Assert.assertEquals(2, s.storage.size());
    Assert.assertTrue(s.read());
    Assert.assertEquals(3, s.storage.size());
    Assert.assertTrue(s.read());
    Assert.assertEquals(4, s.storage.size());
    Assert.assertTrue(s.read());
    Assert.assertEquals(5, s.storage.size());
    Assert.assertTrue(s.read()); //reads last 2 symbols, no more data
    Assert.assertEquals(6, s.storage.size());
    Assert.assertFalse(s.read()); //no more data
    Assert.assertFalse(s.read()); //no more data
    Assert.assertEquals(6, s.storage.size());

    Assert.assertEquals(LONG_INPUT, s.value());

    Assert.assertTrue(s.storage.isEmpty());
    Assert.assertEquals(0, s.currentReadPosition.listPosition);
    Assert.assertEquals(0, s.currentReadPosition.arrayPosition);
    Assert.assertFalse(s.currentReadPosition.isFound);
  }

  @Test
  public void testPositionOfBufferSmaller2InputWithoutReading(){
    int buffSize = 3;
    Storage s = new Storage(buffSize,
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    //when we try to find a positon, input will be read automatically till the end
    Position p1 = s.positionOf("5");
    Assert.assertTrue(p1.isFound);
  }

  @Test
  public void testPositionOfBufferSmaller2Input(){
    int buffSize = 3;
    Storage s = new Storage(buffSize,
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    Position p2 = s.positionOf("5");
    Assert.assertTrue(p2.isFound);
  }


  @Test
  public void testPositionOfBufferSmaller2InputAndBigger2Search(){
    int buffSize = 3;
    Storage s = new Storage(buffSize,
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    Position p = s.positionOf("56");
    Assert.assertTrue(p.isFound);
    Assert.assertEquals(1, p.listPosition);
    Assert.assertEquals(1, p.arrayPosition);
  }

  @Test
  public void testPositionOfBufferSmaller2InputAndEquals2Search1() {
    int buffSize = 3;
    Storage s = new Storage(buffSize,
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    Position p1 = s.positionOf("567");
    Assert.assertTrue(p1.isFound);
    Assert.assertEquals(1, p1.listPosition);
    Assert.assertEquals(1, p1.arrayPosition);
  }

  @Test
  public void testPositionOfBufferSmaller2InputAndEquals2Search2(){
    int buffSize = 3;
    Storage s = new Storage(buffSize,
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    Position p1 = s.positionOf("901");
    Assert.assertTrue(p1.isFound);
    Assert.assertEquals(2, p1.listPosition);
    Assert.assertEquals(2, p1.arrayPosition);
  }

  @Test
  public void testPositionOfBufferSmaller2InputAndSearch(){
    int buffSize = 3;
    Storage s = new Storage(buffSize,
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    Position p1 = s.positionOf("9012");
    Assert.assertTrue(p1.isFound);
    Assert.assertEquals(2, p1.listPosition);
    Assert.assertEquals(2, p1.arrayPosition);
  }

  /**
   *
   */
  @Test
  public void testPositionOfMultipleBufferMultipleElementSearchMultipleSearchs(){
    int bufferSize = 3;
    Storage s = new Storage(bufferSize,
      IOUtils.toInputStream(LONG_INPUT, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8);
    //1st search
    Position p1 = s.positionOf("90");
    Assert.assertTrue(p1.isFound);
    Assert.assertEquals(2, p1.listPosition);
    Assert.assertEquals(2, p1.arrayPosition);
    Assert.assertEquals(0, s.currentReadPosition.listPosition);
    Assert.assertEquals(0, s.currentReadPosition.arrayPosition);
    Assert.assertEquals(4, s.storage.size());

    Assert.assertEquals("12345678", s.value(p1));

    Assert.assertEquals(1, s.storage.size());
    Assert.assertEquals(0, s.currentReadPosition.listPosition);
    Assert.assertEquals(1, s.currentReadPosition.arrayPosition);

    Position p2 = s.positionOf("67");
    Assert.assertEquals(2, p2.listPosition);
    Assert.assertEquals(0, p2.arrayPosition);
  }



}
