package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendMail {

    //region initial
    Context context;
    public SendMail(Context context){
        this.context = context;
    }
    //endregion



    //region method
    public boolean RegMail(String sEmail){
        Boolean bResult = false;

        Pattern pattern = Pattern.compile("^[_a-z0-9-]+([._a-z0-9-]+)*@[a-z0-9-]+([.a-z0-9-]+)*$");
        Matcher matcher = pattern.matcher(sEmail);
        if(matcher.find())
            bResult = true;

        return bResult;
    }

    public boolean RegDate(String sDate){
        Boolean bResult = false;

        if(!sDate.equals("") && sDate.length() >= 21){
            bResult = true;
        }

        return bResult;
    }

    public Intent Send(Context context, String[] sSendTo, String sSubject, String sMessage, String sFileName){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/csv");

        emailIntent.putExtra(Intent.EXTRA_EMAIL, sSendTo);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, sSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, sMessage);

        File file = new File(sFileName);
        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);

        return emailIntent;
    }

    private void sql2csv(String sFileName){
        // get member id
        ArrayList<String> sMember_ID = new ArrayList<String>();
        ArrayList<String> sMember_name = new ArrayList<String>();

        DB_open("member");
        Cursor cursor = sql.Select_member();
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                sMember_ID.add(String.format("%04d", Integer.valueOf(cursor.getString(0))));
                sMember_name.add(cursor.getString(1));
            } while(cursor.moveToNext());    // 有一下筆就繼續迴圈
        }
        DB_close();

        // get date
        ArrayList<String> sDate = new ArrayList<String>();
        sDate = GetMonth_of_days(sFileName.substring(0, 11), sFileName.substring(11));

        // get register time
        ArrayList<String> sID = new ArrayList<String>();
        ArrayList<String> sRegisterTime = new ArrayList<String>();
        DB_open("register_month");
        for(String id:sMember_ID){
            cursor = sql.Select_register_month(id, sDate.get(0), sDate.get(sDate.size()-1));
            cursor.moveToFirst();
            if(cursor !=null &&cursor.getCount() > 0){
                do{
                    // get start time
                    String sTemp = cursor.getString(0)
                            .replace("-", "")
                            .replace(":", "")
                            .replace(" ", "");
                    sID.add(id);
                    sRegisterTime.add(sTemp);

                    // get end time
                    if(!cursor.getString(1).equals(cursor.getString(0))){
                        String sTemp2 = cursor.getString(1)
                                .replace("-", "")
                                .replace(":", "")
                                .replace(" ", "");
                        sID.add(id);
                        sRegisterTime.add(sTemp2);
                    }
                } while(cursor.moveToNext());    // 有一下筆就繼續迴圈
            }
        }
        DB_close();

        // CSV
        String sDir = context.getFilesDir().toString();
        sFileName += ".csv";
        CSV_rw csv_rw = new CSV_rw(sDir, sFileName);
        csv_rw.CSV_write_(sID, sRegisterTime);
    }

    private ArrayList<String> GetMonth_of_days(String sStartDate, String sEndDate){
        ArrayList<String> sResult = new ArrayList<String>();

        try {
            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date dateStart = format.parse(sStartDate);
            Date dateEnd = format.parse(sEndDate);

            while(dateStart.before(dateEnd) || dateStart.equals(dateEnd)){
                sResult.add(format.format(dateStart));

                // add one day
                calendar.setTime(dateStart);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                dateStart = calendar.getTime();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return sResult;
    }
    //endregion






    //region UI
    public void SendDialog(){
        // Button -> get date
        final Button button_Date = new Button(context);
        button_Date.setText("");
        button_Date.setHint("Select Date ...");
        calendarListener_start calendarListener_Start = new calendarListener_start(context, button_Date);
        button_Date.setOnClickListener(calendarListener_Start);

        //  edit text -> mail
        final EditText ET = new EditText(context);
        ET.setHint("Email");
        ET.setText("");
        ET.requestFocus();

        // layout
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(button_Date);
        layout.addView(ET);

        DB_open("mail_setting");
        Cursor cursor = sql.Selete_mail();
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            ET.setText(cursor.getString(0));
        }
        DB_close();

        // create AlertDialog
        final AlertDialog ad_Send = new AlertDialog.Builder(context)
                .setTitle("Send monthly report")
                .setView(layout)
                .setCancelable(true)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", null)
                .create();

        ad_Send.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ad_Send.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean bRegMail = RegMail(ET.getText().toString());
                        boolean bRegDate = RegDate(button_Date.getText().toString());
                        if(bRegMail && bRegDate){
                            DB_open("mail_setting");
                            sql.Update_mail(ET.getText().toString());
                            DB_close();

                            // mail to
                            String[] mailto = {ET.getText().toString()};
                            // create csv file
                            String sFileName = button_Date.getText().toString();
                            File path = context.getFilesDir();
                            String baseDir = path.toString();
                            String filePath = baseDir + File.separator + sFileName + ".csv";
                            sql2csv(sFileName);

                            Intent Intent_mail = Send(context, mailto, "Punch record (" + sFileName + ")", "", filePath);
                            context.startActivity(Intent_mail);
                            Toast.makeText(context, "Send to " + ET.getText().toString(), Toast.LENGTH_SHORT).show();
                            ad_Send.cancel();
                        }
                        else{
                            Toast.makeText(context, "-1", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        ad_Send.show();
    }
    //endregion





    //region listener

    public class calendarListener_start implements View.OnClickListener{
        Button button;
        Context context;
        DatePickerDialog datePickerDialog;
        Date dateStart;
        Date dateEnd;

        public calendarListener_start(Context context, Button button){
            this.button = button;
            this.context = context;
        }

        @Override
        public void onClick(View view) {
            // initial
            dateStart = null;
            dateEnd = null;

            // Get current year, month and day.
            Calendar now = Calendar.getInstance();
            int y = now.get(Calendar.YEAR);
            int m = now.get(Calendar.MONTH);
            int d = now.get(Calendar.DAY_OF_MONTH);

            datePickerDialog = new DatePickerDialog(context, onDateSetListener, y, m, d);
            datePickerDialog.setTitle("Please select start date ...");
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        }

        public void UpdateDate(String sDate){

            if(!sDate.equals("")){
                if(dateStart == null){
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        dateStart = dateFormat.parse(sDate);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(dateStart);

                        int y = calendar.get(Calendar.YEAR);
                        int m = calendar.get(Calendar.MONTH);
                        int d = calendar.get(Calendar.DAY_OF_MONTH);
                        long l =calendar.getTimeInMillis();

                        datePickerDialog = new DatePickerDialog(context, onDateSetListener, y, m, d);
                        datePickerDialog.setTitle("Please select end date ...");
                        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                        datePickerDialog.getDatePicker().setMinDate(l);
                        datePickerDialog.show();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else if(dateStart != null && dateEnd == null){
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    button.setText(dateFormat.format(dateStart) + "_" + sDate);
                }
            }
        }

        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                StringBuffer strBuf = new StringBuffer();
                strBuf.append(y);
                strBuf.append("-");
                strBuf.append(String.format("%02d", m+1));
                strBuf.append("-");
                strBuf.append(String.format("%02d", d));

                UpdateDate(strBuf.toString());
            }
        };
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
        db = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(db_name).getPath(), null);
        sql = new SQL(db_name, tb_name, db);

        if(!sql.TableIsExist(db_name)){
            sql.TableCreate();

            if(tb_name.equals("mail_setting")) {
                sql.Insert_mail();
            }
        }
    }
    //endregion
}