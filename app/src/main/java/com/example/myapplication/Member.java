package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Member extends AppCompatActivity {

    ListView listView;
    List<MemberInfo> memberInfos;
    private MemberInfo_Adaper memberInfo_adaper;

    private void Init(){
        // FloatingActionButton
        FloatingActionButton FAB = findViewById(R.id.FAB_add);
        FAB.setOnClickListener(onClickListener_AddMember);

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

        memberInfo_adaper = new MemberInfo_Adaper(this, memberInfos);
        listView.setAdapter(memberInfo_adaper);
    }

    // -------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Init();
    }

    public View.OnClickListener onClickListener_AddMember = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(Member.this, Member_Edit.class);
            startActivity(intent);
        }
    };

    // ------------------- SQL ----------------------------

    final static String db_name = "Register.db";
    SQLiteDatabase db;
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
