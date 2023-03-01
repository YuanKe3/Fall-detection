package com.example.arr_pose1.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Graph.class}, version = 1, exportSchema = false)
public abstract class GraphDatabase extends RoomDatabase {
  public abstract GraphDao getGraphDao();

  private static GraphDatabase INSTANCE;
  public static synchronized GraphDatabase getInstance(Context context) {
    if (INSTANCE == null) {
      INSTANCE = Room.databaseBuilder(context.getApplicationContext(), GraphDatabase.class, "Graph Database")
              .allowMainThreadQueries()
              .build();
    }
    return INSTANCE;
  }
}
