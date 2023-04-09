package com.example.arr_pose1.room.Graph;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Graph {
  @PrimaryKey(autoGenerate = true)
  private int id;
  private double latitude;
  private double longitude;
  private long fallTime;
  private String fallLocation;

  public Graph(double latitude, double longitude, long fallTime, String fallLocation) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.fallTime = fallTime;
    this.fallLocation = fallLocation;
  }

  public Graph() {}

  public String getFallLocation() {
    return fallLocation;
  }

  public void setFallLocation(String fallLocation) {
    this.fallLocation = fallLocation;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public long getFallTime() {
    return fallTime;
  }

  public void setFallTime(long fallTime) {
    this.fallTime = fallTime;
  }

  @Override
  public String toString() {
    return "Graph{" +
            "id=" + id +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", fallTime=" + fallTime +
            ", fallLocation='" + fallLocation + '\'' +
            '}';
  }
}
