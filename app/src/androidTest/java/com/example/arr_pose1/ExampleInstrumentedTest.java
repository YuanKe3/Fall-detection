package com.example.arr_pose1;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.arr_pose1.HealthNews.HealthNewsRepository;
import com.example.arr_pose1.HealthNews.bean.HealthNews;
import com.example.arr_pose1.room.Contact.Contact;
import com.example.arr_pose1.room.Contact.ContactDatabase;
import com.example.arr_pose1.room.Graph.GraphDatabase;
import com.example.arr_pose1.room.Record.RecordDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

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
    public void dataBaseTest() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.arr_pose1", appContext.getPackageName());

        contactDatabase = ContactDatabase.getInstance(appContext);
        assertEquals(contactDatabase.getContactDao().getAllContact().size(), 0);
        recordDatabase = RecordDatabase.getInstance(appContext);
        assertEquals(recordDatabase.getRecordDao().getRecords().size(), 0);
        graphDatabase = GraphDatabase.getInstance(appContext);
        assertEquals(graphDatabase.getGraphDao().getAllGraph().size(), 0);
    }

    @Test
    public void getAngleTest() {
        assertThat(MainActivity.getAngle(
                new PoseLandMark(100F, 100F, (float) 0.9),
                new PoseLandMark(100F, 100F, (float) 0.9),
                new PoseLandMark(100F, 100F, (float) 0.9)
        ), is(0.0));
    }

    @Test
    public void callForHelpTest() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        contactDatabase = ContactDatabase.getInstance(appContext);

        // 查
        assertEquals(contactDatabase.getContactDao().getAllContact().size(), 0);

        // 增
        contactDatabase.getContactDao().insertContact(new Contact("110", "changing"));
        Contact newMan = contactDatabase.getContactDao().getAllContact().get(0);
        assertEquals(newMan.getName(), "changing");

        // 改
        newMan.setName("Mike");
        assertEquals(newMan.getName(), "Mike");

        // 删
        contactDatabase.getContactDao().deleteContact(newMan);
        assertEquals(contactDatabase.getContactDao().getAllContact().size(), 0);
    }

    @Test
    public void addContactTest() {
        assertEquals(ContactsContract.Contacts.CONTENT_TYPE, "vnd.android.cursor.dir/contact");
    }

    @Test
    public void loadPageTest() throws Exception {
        HealthNewsRepository healthNewsRepository = HealthNewsRepository.getInstance();
        List<HealthNews> healthNewsList = healthNewsRepository.getHealthNews("7489a44a34cf0054b704ba1829cda829", 10, 1);
        assertTrue(healthNewsList.get(0).getTitle().length() > 0);
        List<HealthNews> healthNewsList2 = healthNewsRepository.getHealthNews("7489a44a34cf0054b704ba1829cda829", 10, 1);
        assertTrue(healthNewsList2.get(0).getTitle().length() > 0);
    }
}
