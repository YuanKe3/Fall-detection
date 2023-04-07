package com.example.arr_pose1.model;

import com.google.gson.annotations.SerializedName;

public class SendMailData {
  @SerializedName("total")
  private int total;
  @SerializedName("wrongTimes")
  private int wrongTimes;
//  @SerializedName("locationList")
//  private String[] locationList;

  public SendMailData(int total, int wrongTimes, String[] locationList) {
    super();
    this.total = total;
    this.wrongTimes = wrongTimes;
//    this.locationList = locationList;
  }
}

