package com.example.arr_pose1.room.PersonInfo;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PersonInfoDao {
  @Insert
  void insertPersonInfo(PersonInfo personInfo);

  @Update
  void updatePersonInfo(PersonInfo personInfo);

  @Delete
  void deletePersonInfo(PersonInfo personInfo);

  @Query("SELECT * FROM PersonInfo")
  List<PersonInfo> getAllPersonInfo();
}
