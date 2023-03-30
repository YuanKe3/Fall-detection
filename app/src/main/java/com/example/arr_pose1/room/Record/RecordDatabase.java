package com.example.arr_pose1.room.Record;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Record.class}, version = 1, exportSchema = false)
public abstract class RecordDatabase extends RoomDatabase {
  public abstract RecordDao getRecordDao();

  private static RecordDatabase INSTANCE;
  public static synchronized RecordDatabase getInstance(Context context) {
    if (INSTANCE == null) {
      INSTANCE = Room.databaseBuilder(context.getApplicationContext(), RecordDatabase.class, "Record Database")
              .allowMainThreadQueries()
              .build();
    }
    return INSTANCE;
  }
}
