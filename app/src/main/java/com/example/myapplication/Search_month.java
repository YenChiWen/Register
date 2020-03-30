package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Search_month extends AppCompatActivity {
    TableLayout tableLayout;

    //region
    private void Init(){
        // spinner_month
        List<String> sSpinnerMonth = Get_month();
        // spinner_id
        List<String> sSpinnerID = Get_id();

        Spinner spinner_id = findViewById(R.id.spinner_id);
        ArrayAdapter<String> adapter_id = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sSpinnerID);
        adapter_id.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_id.setAdapter(adapter_id);
        spinner_id.setOnItemSelectedListener(onItemSelectedListener);

        Spinner spinner_month = findViewById(R.id.spinner_month);
        ArrayAdapter<String> adapter_month = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sSpinnerMonth);
        adapter_month.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_month.setAdapter(adapter_month);
        spinner_month.setOnItemSelectedListener(onItemSelectedListener);

        // table layout
        UploadTable();
    }

    private List<String> Get_id(){
        List<String> sSpinnerID = new ArrayList<String>();

        DB_open("member");
        Cursor cursor = sql.Select_member();
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                sSpinnerID.add(String.format("%04d", Integer.valueOf(cursor.getString(0))) + " " + cursor.getString(1));
            } while(cursor.moveToNext());    // 有一下筆就繼續迴圈
        }
        else{
            sSpinnerID.add("");
        }
        DB_close();

        return sSpinnerID;
    }

    private List<String> Get_month(){
        List<String> sSpinnerMonth = new ArrayList<String>();

        DB_open("register_month");
        Cursor cursor = sql.Select_Month();
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                if(cursor.getString(0) != null){
                    String sYM = cursor.getString(0).substring(0, 7);
                    if(!sSpinnerMonth.contains(sYM)){
                        sSpinnerMonth.add(sYM);
                    }
                }
            } while(cursor.moveToNext());    // 有一下筆就繼續迴圈
        }
        else{
            sSpinnerMonth.add("");
        }
        DB_close();

        return sSpinnerMonth;
    }

    private void UploadTable() {
        Spinner spinner_id = findViewById(R.id.spinner_id);
        Spinner spinner_month = findViewById(R.id.spinner_month);

        if(!spinner_id.getSelectedItem().toString().equals("") && !spinner_month.getSelectedItem().toString().equals("")){
            String sMonth="", sID="";

            sMonth = spinner_month.getSelectedItem().toString();
            sID = spinner_id.getSelectedItem().toString().substring(0,4);

            tableLayout = (TableLayout)findViewById(R.id.tablelayout);
            tableLayout.removeAllViews();
            CreateTable(sID, sMonth);
        }
    }

    private void CreateTable(String sID, String sMonth){
        if(!sID.equals("") && !sMonth.equals("")){
            ArrayList<String> sMonth_of_day = GetMonth_of_days(sMonth);
            tableLayout = (TableLayout)findViewById(R.id.tablelayout);

            DB_open("register_month");
            if(sMonth_of_day.size() > 0){
                Cursor cursor = sql.Select_register_month(sID, sMonth_of_day.get(0), sMonth_of_day.get(sMonth_of_day.size()-1));
                if(cursor !=null && cursor.getCount() > 0){
                    // header
                    String[] headerText={"Date", "Clock-in", "Clock-out", "Total"};
                    CreateTableRow(headerText, "#B50500", "#FFFFFF");

                    // content
                    int iCount = 1;
                    cursor.moveToFirst();
                    for(int i=0; i<sMonth_of_day.size() - 1; i++){
                        String sIn = "";
                        String sOut = "";

                        if(cursor.getCount() >= iCount && cursor.getString(0).substring(0,10).equals(sMonth_of_day.get(i))){
                            sIn = cursor.getString(0).substring(11);
                            if(!cursor.getString(1).equals(cursor.getString(0))){
                                sOut = cursor.getString(1).substring(11);
                            }

                            cursor.moveToNext();
                            iCount++;
                        }
                        String[] content={sMonth_of_day.get(i), sIn, sOut, dateCompute(sIn, sOut)};
                        CreateTableRow(content, "#FFFFFF", "#000000");
                    }
                }
            }
            DB_close();
        }
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

    private ArrayList<String> GetMonth_of_days(String sMonth){
        ArrayList<String> sResult = new ArrayList<String>();

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(sMonth+"-01");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int iStart = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
            int iEnd = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            while (iStart <= iEnd){
                sResult.add(sMonth + "-" + String.format("%02d", iStart));
                iStart++;
            }

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)+1);
            date = calendar.getTime();
            sResult.add(format.format(date));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return sResult;
    }

    private String dateCompute(String sStartTime, String sEndTime){
        double add = 0;
        float fDiff = 0;
        String sResult = "-";

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date1= null, date2 = null, date3 = null, data_start = null, data_end = null;
        try {
            data_start = sdf.parse(sStartTime);
            data_end = sdf.parse(sEndTime);
            date1 = sdf.parse("08:30:00");
            date2 = sdf.parse("12:15:00");
            date3 = sdf.parse("13:30:00");

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(data_start);
            if(data_end.after(date3)){
                if(data_start.before(date2)){    // start <= 1215 & end >=1330
                    add = 1.25;
                }
                else if(data_start.after(date2) && data_start.before(date3)){      // 1215 < start <= 1330 & end >=1330
                    fDiff = date3.getTime()-data_start.getTime();
                    fDiff = fDiff/(1000*60*60);
                    add = fDiff;
                }
                else if(data_start.after(date3)){     // time > 1330
                    add = 0;
                }
            }

            fDiff = data_end.getTime() - data_start.getTime();
            fDiff = fDiff/(1000*60*60) - (float) add;
            sResult = String.format("%.1f", fDiff);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sResult;
    }
    //endregion





    //region
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_month);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Init();
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

    public void onclick_refresh(View view) {
        Cursor cursor;
        // get member
        ArrayList<String> member = new ArrayList<String>();
        DB_open("member");
        cursor = sql.Select_member();
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                String sID = String.format("%04d", Integer.valueOf(cursor.getString(0)));
                member.add(sID);
            } while(cursor.moveToNext());    // 有一下筆就繼續迴圈
        }
        DB_close();

        // get register_month max date
        String sDate = "";
        DB_open("register_month");
        cursor = sql.Select_register_month_max_date();
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                sDate = cursor.getString(0);
            } while(cursor.moveToNext());    // 有一下筆就繼續迴圈
        }
        DB_close();

        // insert
        if(!member.isEmpty()) {
            DB_open("register_month");
            for (int i = 0; i < member.size(); i++) {
                sql.Insert_register_day2month_(member.get(i), sDate);
            }
            DB_close();
        }
        Init();
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
