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
  private int kneeSettingAlgorithm = 0;
  private int mainAlgorithm = 0;
  private int lieDownAlgorithm = 0;
  private int wrongMainAlgorithm = 0;
  private int wrongKneeSettingAlgorithm = 0;
  private int wrongLieDownAlgorithm = 0;

  public Record(int wrongWarningTimes, int warningTimes, int kneeSettingAlgorithm, int mainAlgorithm, int lieDownAlgorithm, int wrongMainAlgorithm, int wrongKneeSettingAlgorithm, int wrongLieDownAlgorithm) {
    this.wrongWarningTimes = wrongWarningTimes;
    this.warningTimes = warningTimes;
    this.kneeSettingAlgorithm = kneeSettingAlgorithm;
    this.mainAlgorithm = mainAlgorithm;
    this.lieDownAlgorithm = lieDownAlgorithm;
    this.wrongMainAlgorithm = wrongMainAlgorithm;
    this.wrongKneeSettingAlgorithm = wrongKneeSettingAlgorithm;
    this.wrongLieDownAlgorithm = wrongLieDownAlgorithm;
  }

  public int getWrongMainAlgorithm() {
    return wrongMainAlgorithm;
  }

  public void setWrongMainAlgorithm(int wrongMainAlgorithm) {
    this.wrongMainAlgorithm = wrongMainAlgorithm;
  }

  public int getWrongKneeSettingAlgorithm() {
    return wrongKneeSettingAlgorithm;
  }

  public void setWrongKneeSettingAlgorithm(int wrongKneeSettingAlgorithm) {
    this.wrongKneeSettingAlgorithm = wrongKneeSettingAlgorithm;
  }

  public int getWrongLieDownAlgorithm() {
    return wrongLieDownAlgorithm;
  }

  public void setWrongLieDownAlgorithm(int wrongLieDownAlgorithm) {
    this.wrongLieDownAlgorithm = wrongLieDownAlgorithm;
  }

  public int getKneeSettingAlgorithm() {
    return kneeSettingAlgorithm;
  }

  public void setKneeSettingAlgorithm(int kneeSettingAlgorithm) {
    this.kneeSettingAlgorithm = kneeSettingAlgorithm;
  }

  public int getMainAlgorithm() {
    return mainAlgorithm;
  }

  public void setMainAlgorithm(int mainAlgorithm) {
    this.mainAlgorithm = mainAlgorithm;
  }

  public int getLieDownAlgorithm() {
    return lieDownAlgorithm;
  }

  public void setLieDownAlgorithm(int lieDownAlgorithm) {
    this.lieDownAlgorithm = lieDownAlgorithm;
  }

  @Override
  public String toString() {
    return "Record{" +
            "id=" + id +
            ", wrongWarningTimes=" + wrongWarningTimes +
            ", warningTimes=" + warningTimes +
            ", kneeSettingAlgorithm=" + kneeSettingAlgorithm +
            ", mainAlgorithm=" + mainAlgorithm +
            ", lieDownAlgorithm=" + lieDownAlgorithm +
            ", wrongMainAlgorithm=" + wrongMainAlgorithm +
            ", wrongKneeSettingAlgorithm=" + wrongKneeSettingAlgorithm +
            ", wrongLieDownAlgorithm=" + wrongLieDownAlgorithm +
            '}';
  }
//  public Record(int warningTimes, int wrongWarningTimes) {
//    this.wrongWarningTimes = wrongWarningTimes;
//    this.warningTimes = warningTimes;
//  }

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
}
