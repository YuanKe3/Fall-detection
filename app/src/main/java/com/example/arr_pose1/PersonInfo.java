package com.example.arr_pose1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.arr_pose1.room.PersonInfo.PersonInfoDatabase;

public class PersonInfo extends AppCompatActivity {
  private EditText nameEditText;
  private EditText ageEditText;
  private EditText diseaseEditText;
  private EditText allergyEditText;
  private EditText addressEditText;
  private EditText otherEditText;
  private Button saveInfoBtn;
  private Button resetInfoBtn;
  private PersonInfoDatabase personInfoDatabase;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    overridePendingTransition(R.anim.bottom_in, R.anim.bottom_silent);
    setContentView(R.layout.activity_person_info);

    personInfoDatabase = PersonInfoDatabase.getInstance(this);

    nameEditText = findViewById(R.id.nameEditText);
    ageEditText = findViewById(R.id.ageEditText);
    diseaseEditText = findViewById(R.id.diseaseEditText);
    allergyEditText = findViewById(R.id.allergyEditText);
    addressEditText = findViewById(R.id.addressEditText);
    otherEditText = findViewById(R.id.otherEditText);
    saveInfoBtn = findViewById(R.id.saveInfoBtn);
    resetInfoBtn = findViewById(R.id.resetInfoBtn);

    if (personInfoDatabase.getPersonInfoDao().getAllPersonInfo().size() != 0) {
      com.example.arr_pose1.room.PersonInfo.PersonInfo personInfo = personInfoDatabase.getPersonInfoDao().getAllPersonInfo().get(0);
      // 设置信息
      String name = personInfo.getName();
      String age = personInfo.getAge();
      String disease = personInfo.getDisease();
      String allergy = personInfo.getAllergy();
      String address = personInfo.getAddress();
      String other = personInfo.getOther();
      nameEditText.setText(name);
      ageEditText.setText(age);
      diseaseEditText.setText(disease);
      allergyEditText.setText(allergy);
      addressEditText.setText(address);
      otherEditText.setText(other);
    }


    saveInfoBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String name = nameEditText.getText().toString();
        String age = ageEditText.getText().toString();
        String disease = diseaseEditText.getText().toString();
        String allergy = allergyEditText.getText().toString();
        String address = addressEditText.getText().toString();
        String other = otherEditText.getText().toString();
        if (name.isEmpty() || age.isEmpty() || disease.isEmpty() || allergy.isEmpty() || address.isEmpty()) {
          Toast.makeText(PersonInfo.this, "表单项不能为空", Toast.LENGTH_SHORT).show();
        } else {
          if (personInfoDatabase.getPersonInfoDao().getAllPersonInfo().size() == 0) {
            personInfoDatabase.getPersonInfoDao().insertPersonInfo(new com.example.arr_pose1.room.PersonInfo.PersonInfo(
                    name, age, disease, allergy, address, other
            ));
          } else {
            com.example.arr_pose1.room.PersonInfo.PersonInfo personInfo = personInfoDatabase.getPersonInfoDao().getAllPersonInfo().get(0);
            personInfo.setName(name);
            personInfo.setAge(age);
            personInfo.setDisease(disease);
            personInfo.setAllergy(allergy);
            personInfo.setAddress(address);
            personInfo.setOther(other);
            personInfoDatabase.getPersonInfoDao().updatePersonInfo(personInfo);
          }
          AlertDialog.Builder builder = new AlertDialog.Builder(PersonInfo.this);
          builder
                  .setTitle("保存成功！")
                  .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                  })
                  .create()
                  .show();
        }
      }
    });

    resetInfoBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (personInfoDatabase.getPersonInfoDao().getAllPersonInfo().size() != 0) {
          com.example.arr_pose1.room.PersonInfo.PersonInfo personInfo = personInfoDatabase.getPersonInfoDao().getAllPersonInfo().get(0);
          personInfoDatabase.getPersonInfoDao().deletePersonInfo(personInfo);
        }
        nameEditText.setText("");
        ageEditText.setText("");
        diseaseEditText.setText("");
        allergyEditText.setText("");
        addressEditText.setText("");
        otherEditText.setText("");
      }
    });
  }

  @Override
  public void finish() {
    super.finish();
    overridePendingTransition(R.anim.bottom_silent, R.anim.bottom_out);
  }
}