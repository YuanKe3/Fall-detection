package com.example.arr_pose1;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.arr_pose1.room.Contact.ContactDatabase;
import com.example.arr_pose1.room.Graph.GraphDatabase;
import com.example.arr_pose1.room.Record.RecordDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private ContactDatabase contactDatabase;
    private RecordDatabase recordDatabase;
    private GraphDatabase graphDatabase;

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.arr_pose1", appContext.getPackageName());

        contactDatabase = ContactDatabase.getInstance(appContext);
        assertEquals(contactDatabase.getContactDao().getAllContact().size(), 1);
        recordDatabase = RecordDatabase.getInstance(appContext);
        assertEquals(recordDatabase.getRecordDao().getRecords().size(), 1);
        graphDatabase = GraphDatabase.getInstance(appContext);
        assertEquals(graphDatabase.getGraphDao().getAllGraph().size(), 4);
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