package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Search_day extends AppCompatActivity {
    TableLayout tableLayout;

    //region
    private void Init(){
        // button_date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Button btn_date = findViewById(R.id.btn_date);
        btn_date.setText(sdf.format(date));
        btn_date.setOnClickListener(onClickListener_date);

        // spinner_id
        List<String> sSpinnerID = new ArrayList<String>();
        sSpinnerID.add("");

        DB_open("member");
        Cursor cursor = sql.Select_member();
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                sSpinnerID.add(String.format("%04d", Integer.valueOf(cursor.getString(0))) + " " + cursor.getString(1));
            } while(cursor.moveToNext());    // 有一下筆就繼續迴圈
        }
        DB_close();

        Spinner spinner_id = findViewById(R.id.spinner_id);
        ArrayAdapter<String> adapter_id = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sSpinnerID);
        adapter_id.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_id.setAdapter(adapter_id);
        spinner_id.setOnItemSelectedListener(onItemSelectedListener);

        // table layout
        UploadTable();
    }

    private void UploadTable() {
        String sDate="", sID="";

        Button button = findViewById(R.id.btn_date);
        Spinner spinner = findViewById(R.id.spinner_id);
        sDate = button.getText().toString();
        if(spinner.getSelectedItem().toString().equals("")){
            sID = "";
        }
        else{
            sID = spinner.getSelectedItem().toString().substring(0,4);
        }

        tableLayout = (TableLayout)findViewById(R.id.tablelayout);
        tableLayout.removeAllViews();
        CreateTable(sID, sDate);
    }

    private void CreateTable(String sID, String sDate){
        tableLayout = (TableLayout)findViewById(R.id.tablelayout);

        DB_open("register_day");
        Cursor cursor = sql.Select_RegisterDay(sID, sDate);
        if(cursor !=null && cursor.getCount() > 0){
            // header
            String[] headerText={"ID", "Name", "Time", "Method"};
            CreateTableRow(headerText, "#B50500", "#FFFFFF");

            // content
            cursor.moveToFirst();
            do{
                sID = String.format("%04d", Integer.valueOf(cursor.getString(0)));
                String sStartTime = cursor.getString(2).substring(11);

                String[] content={sID, cursor.getString(1), sStartTime, cursor.getString(3)};
                CreateTableRow(content, "#FFFFFF", "#000000");
            } while(cursor.moveToNext());
        }
        DB_close();
    }

    private void CreateTableRow(String[] sTableRow, String BackgroundColor, String textColor){
        TableRow rowHeader = new TableRow(this);
        rowHeader.setBackgroundColor(Color.parseColor(BackgroundColor));
        rowHeader.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        for(String c:sTableRow) {
            TextView tv = new TextView(this);
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(18);
            tv.setTextColor(Color.parseColor(textColor));
            tv.setPadding(5, 5, 5, 5);
            tv.setText(c);
            rowHeader.addView(tv);
        }
        tableLayout.addView(rowHeader);
    }
    //endregion





    //region
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_day);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private Spinner.OnItemSelectedListener onItemSelectedListener = new Spinner.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            UploadTable();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private View.OnClickListener onClickListener_date = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                    StringBuffer strBuf = new StringBuffer();
                    strBuf.append(y);
                    strBuf.append("-");
                    strBuf.append(m+1);
                    strBuf.append("-");
                    strBuf.append(d);

                    Toast.makeText(Search_day.this, strBuf.toString(), Toast.LENGTH_LONG).show();
                    Button button = findViewById(R.id.btn_date);
                    button.setText(strBuf.toString());
                    UploadTable();
                }
            };

            // Get current year, month and day.
            Calendar now = Calendar.getInstance();
            int y = now.get(Calendar.YEAR);
            int m = now.get(Calendar.MONTH);
            int d = now.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(Search_day.this, onDateSetListener, y, m, d);
            datePickerDialog.setTitle("Please select date.");
            datePickerDialog.show();
        }
    };

    public void onclick_delete(View view) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Remove")
                .setMessage("Are you sure to remove the data of three months ago?")
                .setCancelable(true)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DB_open("register_day");
                        sql.Delete_3month();
                        DB_close();
                    }
                })
                .create();
        alertDialog.show();
    }
    //endregion





    //region SQL
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
    //endregion
}
