package com.example.arr_pose1.room.PersonInfo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PersonInfo {
  @PrimaryKey(autoGenerate = true)
  private int id;
  private String name;
  private String age;
  private String disease;
  private String allergy;
  private String address;
  private String other;

  public PersonInfo(String name, String age, String disease, String allergy, String address, String other) {
    this.name = name;
    this.age = age;
    this.disease = disease;
    this.allergy = allergy;
    this.address = address;
    this.other = other;
  }

  public String getOther() {
    return other;
  }

  public void setOther(String other) {
    this.other = other;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAge() {
    return age;
  }

  public void setAge(String age) {
    this.age = age;
  }

  public String getDisease() {
    return disease;
  }

  public void setDisease(String disease) {
    this.disease = disease;
  }

  public String getAllergy() {
    return allergy;
  }

  public void setAllergy(String allergy) {
    this.allergy = allergy;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  @Override
  public String toString() {
    return "PersonInfo{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", age='" + age + '\'' +
            ", disease='" + disease + '\'' +
            ", allergy='" + allergy + '\'' +
            ", address='" + address + '\'' +
            ", other='" + other + '\'' +
            '}';
  }
}
