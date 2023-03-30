package com.example.arr_pose1.room.Record;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Record {
  @PrimaryKey(autoGenerate = true)
  private int id;
  private int wrongWarningTimes = 0;
  private int warningTimes = 0;

  public Record(int warningTimes, int wrongWarningTimes) {
    this.wrongWarningTimes = wrongWarningTimes;
    this.warningTimes = warningTimes;
  }

  public Record() {}

  public int getWarningTimes() {
    return warningTimes;
  }

  public void setWarningTimes(int warningTimes) {
    this.warningTimes = warningTimes;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getWrongWarningTimes() {
    return wrongWarningTimes;
  }

  public void setWrongWarningTimes(int wrongWarningTimes) {
    this.wrongWarningTimes = wrongWarningTimes;
  }

  @Override
  public String toString() {
    return "Record{" +
            "id=" + id +
            ", wrongWarningTimes=" + wrongWarningTimes +
            ", warningTimes=" + warningTimes +
            '}';
  }
}
