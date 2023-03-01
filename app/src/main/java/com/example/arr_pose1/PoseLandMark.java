package com.example.arr_pose1;

public class PoseLandMark {
  float x, y, visible;

  PoseLandMark(float x, float y, float visible) {
    this.x = x;
    this.y = y;
    this.visible = visible;
  }

  public void setX(float x) {
    this.x = x;
  }

  public void setY(float y) {
    this.y = y;
  }

  public void setVisible(float visible) {
    this.visible = visible;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public float getVisible() {
    return visible;
  }
}
