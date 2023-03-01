package com.example.arr_pose1.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContactDao {
  @Insert
  void insertContact(Contact contact);

  @Update
  void updateContact(Contact contact);

  @Delete
  void deleteContact(Contact contact);

  @Query("DELETE FROM Contact")
  void deleteAllContact();

  @Query("SELECT * FROM Contact")
  List<Contact> getAllContact();
}