package com.example.arr_pose1.room.Record;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RecordDao {
  @Insert
  void insertWrongWarningItem(Record record);

  @Insert
  void insertWarningItem(Record record);

  @Update
  void updateRecord(Record record);

  @Query("SELECT * FROM Record")
  List<Record> getRecords();
}
