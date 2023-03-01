package com.example.arr_pose1.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GraphDao {
  @Insert
  void insertOneGraph(Graph graph);

  @Query("DELETE FROM Graph WHERE fallTime <= :oneWeekAgo")
  void deleteOneWeekAgoGraph(long oneWeekAgo);

  @Query("SELECT * FROM Graph")
  List<Graph> getAllGraph();
}