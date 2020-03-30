package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Touch_Register extends AppCompatActivity {

    static ListView listView;
    static List<MemberInfo> memberInfos;
    private static Register_Adaper register_adaper;


    public static boolean update(int id) {
        if(listView != null){
            return listView.getAdapter().getView(id, listView.getChildAt(id), listView).callOnClick();
        }
        return false;
    }

    private void Init(){
        // member view-list
        memberInfos = new ArrayList<MemberInfo>();
        listView = this.findViewById(R.id.memberList);

        DB_open("member");
        Cursor cursor = sql.Select_member();
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                String sID = String.format("%04d", Integer.valueOf(cursor.getString(0)));

                String sPath = "/mnt/sdcard/" + getApplicationContext().getPackageName() + "/member";
                String sFile = sID + ".jpg";
                Bitmap bmp = BitmapFactory.decodeFile(sPath + "/" + sFile);

                memberInfos.add(new MemberInfo(cursor.getString(1), sID, bmp));
            } while(cursor.moveToNext());    // 有一下筆就繼續迴圈
        }
        DB_close();

        register_adaper = new Register_Adaper(this, memberInfos);
        listView.setAdapter(register_adaper);
    }

    // ---------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_register);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Init();
    }

    // ------------------- SQL ----------------------------

    final static String db_name = "Register.db";
    static SQLiteDatabase db;
    SQL sql;

    public void DB_close(){
        db.close();
    }

    public void DB_open(String tb_name){
        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);
        sql = new SQL(db_name, tb_name, db);

        if(!sql.TableIsExist(db_name)){
            sql.TableCreate();
        }
    }
}
