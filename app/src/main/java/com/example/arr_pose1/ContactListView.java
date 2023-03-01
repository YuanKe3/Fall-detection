package com.example.arr_pose1;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.arr_pose1.model.Contact;
import com.example.arr_pose1.room.ContactDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ContactListView extends AppCompatActivity {
  private static final int REQUEST_READ_CONTACTS = 1;
  private static final int REQUEST_WRITE_CONTACTS = 2;
  private List<Contact> contactList = new ArrayList<>();
  private ListView contactListView;
  private FloatingActionButton fabAdd;
  private ArrayAdapter<Contact> adapter;
  private int mSelectedPosition = 0;
  private ContactDatabase mDatabase;
  private TextView contactTip;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    overridePendingTransition(R.anim.bottom_in, R.anim.bottom_silent);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contact_list_view);

    mDatabase = ContactDatabase.getInstance(this);
//    mDatabase.getContactDao().getAllContact().get(0).getName()

    contactListView = findViewById(R.id.contact_list_view);
    fabAdd = findViewById(R.id.fab_add);
    contactTip = findViewById(R.id.contact_tip);

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
    } else {
      readContacts();
    }
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, REQUEST_WRITE_CONTACTS);
    }

    // contactList is available
    if (contactList.size() != 0 && mDatabase.getContactDao().getAllContact().size() != 0) {
      for (int index = 0; index < contactList.size(); index += 1) {
        if (contactList.get(index).getPhone().equals(mDatabase.getContactDao().getAllContact().get(0).getPhone())) {
          mSelectedPosition = index;
          contactTip.setText("紧急联系人 - " + contactList.get(index).getName());
        }
      }
    }

    fabAdd.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        int permissionCheck = ContextCompat.checkSelfPermission(ContactListView.this, Manifest.permission.WRITE_CONTACTS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
          addContact();
        } else {
          ActivityCompat.requestPermissions(ContactListView.this, new String[]{Manifest.permission.WRITE_CONTACTS}, REQUEST_WRITE_CONTACTS);
        }
      }
    });

    contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ContactListView.this);
        builder
                .setTitle("设立紧急联系人")
                .setMessage("是否设置 「" + contactList.get(position).getName() + "」 为你的紧急联系人？")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {
                    mSelectedPosition = position;
                    adapter.notifyDataSetChanged();

                    com.example.arr_pose1.room.Contact contact = new com.example.arr_pose1.room.Contact();
                    mDatabase.getContactDao().deleteAllContact();
                    contact.setName(contactList.get(position).getName());
                    contact.setPhone(contactList.get(position).getPhone());
                    mDatabase.getContactDao().insertContact(contact);

                    contactTip.setText("紧急联系人 - " + contact.getName());
                  }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {

                  }
                })
                .create()
                .show();
      }
    });
  }

  @Override
  public void finish() {
    super.finish();
    overridePendingTransition(R.anim.bottom_silent, R.anim.bottom_out);
  }

  @Override
  protected void onResume() {
    super.onResume();
    readContacts();
  }

  private void addContact() {
    Intent intent = new Intent(Intent.ACTION_INSERT);
    intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
    startActivity(intent);
  }

  private void readContacts() {
    // 读取其他程序的数据
    ContentResolver contentResolver = getContentResolver();
    Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null, null);
    if (cursor != null) {
      while (cursor.moveToNext()) {
        String displayName = cursor.getString(Math.abs(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
        String contactId = cursor.getString(Math.abs(cursor.getColumnIndex(ContactsContract.Contacts._ID)));
        Cursor phoneCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
        );
        if (phoneCursor != null) {
          while (phoneCursor.moveToNext()) {
            String phoneNumber = phoneCursor.getString(Math.abs(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            contactList.add(new Contact(displayName, phoneNumber));
          }
          phoneCursor.close();
        }
      }
      cursor.close();
    }
    adapter = new ListItemAdapter(this, R.layout.simple_list_item_1, contactList);
    contactListView.setAdapter(adapter);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_READ_CONTACTS) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        readContacts();
      }
    }
//    if (requestCode == REQUEST_WRITE_CONTACTS) {
//      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//        addContact();
//      }
//    }
  }

  class ListItemAdapter extends ArrayAdapter<Contact> {
    private int mResourceId;

    public ListItemAdapter(@NonNull Context context, int resource, @NonNull List<Contact> objects) {
      super(context, resource, objects);
      mResourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      View view;
      ViewHolder viewHolder;
      if (convertView == null) {
        view = LayoutInflater.from(getContext()).inflate(mResourceId, parent, false);
        viewHolder = new ViewHolder();
        viewHolder.numberTextView = view.findViewById(R.id.contact_number);
        viewHolder.nameTextView = view.findViewById(R.id.contact_name);
        view.setTag(viewHolder);
      } else {
        view = convertView;
        viewHolder = (ViewHolder) view.getTag();
      }
      Contact contact = getItem(position);
      viewHolder.nameTextView.setText(contact.getName());
      viewHolder.numberTextView.setText(contact.getPhone());
      if (position == mSelectedPosition) {
        view.setBackgroundColor(Color.parseColor("#e6ecff"));
      } else {
        view.setBackgroundColor(Color.TRANSPARENT);
      }
      return view;
    }

    private class ViewHolder {
      TextView nameTextView;
      TextView numberTextView;
    }
  }
}



