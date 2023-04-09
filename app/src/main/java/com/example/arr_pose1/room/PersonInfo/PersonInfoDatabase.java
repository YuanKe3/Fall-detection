package com.example.arr_pose1.room.PersonInfo;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {PersonInfo.class}, version = 1, exportSchema = false)
public abstract class PersonInfoDatabase extends RoomDatabase {
  public abstract PersonInfoDao getPersonInfoDao();
  private static PersonInfoDatabase INSTANCE;
  public static synchronized PersonInfoDatabase getInstance(Context context) {
    if (INSTANCE == null) {
      INSTANCE = Room.databaseBuilder(context.getApplicationContext(), PersonInfoDatabase.class, "PersonInfo Database")
              .allowMainThreadQueries()
              .build();

    }
    return INSTANCE;
  }
}
