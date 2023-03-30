package com.example.arr_pose1.room.Contact;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Contact {
  @PrimaryKey(autoGenerate = true)
  private int id;
  private String phone;
  private String name;

  public Contact(String phone, String name) {
    this.phone = phone;
    this.name = name;
  }

  public Contact() {}

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "Contact{" +
            "id=" + id +
            ", phone=" + phone +
            ", name='" + name + '\'' +
            '}';
  }
}
