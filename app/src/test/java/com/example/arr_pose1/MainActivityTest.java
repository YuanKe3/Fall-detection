package com.example.arr_pose1;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

//import androidx.test.core.app.ApplicationProvider;

import com.example.arr_pose1.room.Contact.ContactDatabase;
import com.example.arr_pose1.room.Graph.GraphDatabase;
import com.example.arr_pose1.room.Record.RecordDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MainActivityTest {


  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void warningIfNoContent() {

  }

  @Test
  public void getAngle() {
    assertThat(MainActivity.getAngle(
            new PoseLandMark(100F, 100F, (float) 0.9),
            new PoseLandMark(100F, 100F, (float) 0.9),
            new PoseLandMark(100F, 100F, (float) 0.9)
    ), is(0.0));
  }

}