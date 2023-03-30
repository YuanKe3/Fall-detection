package com.example.arr_pose1.room.Contact;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Contact.class}, version = 1, exportSchema = false)
public abstract class ContactDatabase extends RoomDatabase {
  public abstract ContactDao getContactDao();

  private static ContactDatabase INSTANCE;
  public static synchronized ContactDatabase getInstance(Context context) {
    if (INSTANCE == null) {
      INSTANCE = Room.databaseBuilder(context.getApplicationContext(), ContactDatabase.class, "Contact Database")
              .allowMainThreadQueries()
              .build();
    }
    return INSTANCE;
  }
}
