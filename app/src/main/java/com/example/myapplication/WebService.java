package com.example.myapplication;

import android.app.Activity;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

@RequiresApi(api = Build.VERSION_CODES.O)
public class WebService extends NanoHTTPD {
    private static final String TAG = "WebService";
    private Activity mActivity = null;
    private Map<String, Integer> mapId = new HashMap<>();

    public enum Status{
        NoRecord("NoRecord"),
        Working("Working"),
        OffWork("OffWork");

        Status(String s){
            status = s;
        }
        private final String status;
        public String getStatus(){
            return status;
        }
    }

    public WebService(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        // check parameter
        if(session.getMethod().equals(Method.POST)){
            try {
                Map<String, String> files = new HashMap<>();
                session.parseBody(files);
                final String id = session.getParms().get("id");
                final String record = session.getParms().get("record");

                if(id != null && !id.isEmpty()) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boolean isUpdate = Touch_Register.update(mapId.get(id));
                            if(!isUpdate){
                                DB_open("register_day");
                                sql.Insert_register_day(Integer.valueOf(id), "Touch");
                                DB_close();
                            }
                            Log.d(TAG, "run: " + id + " : " + mapId.get(id) + " : " + isUpdate);
                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else if(record != null && !record.isEmpty()){
                    String filePathName = this.mActivity.getFilesDir().toString() + File.separator + record + ".csv";
                    refreshMonthData();
                    sql2csv(record);
                    return downloadFile(new File(filePathName));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        return newFixedLengthResponse(HTML_builder());
    }

    private Response downloadFile(File file){
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while(( line = bufferedReader.readLine()) != null ) {
                sb.append( line );
                sb.append( '\n' );
            }
            Log.d(TAG, "downloadFile: " + sb.toString());

            Response fileResponse = newFixedLengthResponse(Response.Status.OK, "application/octet-stream", sb.toString());
            fileResponse.addHeader("Content-Disposition", "attachment; filename=\""+file.getName()+"\"");
            return fileResponse;
        } catch (Exception e) {
            Log.d(TAG, "downloadFile: " + e.toString());
        }

        return response404(file.getName());
    }

    private Response response404(String file){
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html>body>");
        builder.append("Sorry,Can't Found" + file + " !");
        builder.append("</body></html>\n");
        return newFixedLengthResponse(builder.toString());
    }

    private String HTML_builder(){
        // get html tag
        String html = readAssetsFileAsString("index.html");
        String[] arrayHtml = html.split("&&&");

        // get member & time from database
        Map<String, Map<String,String>> memberInfos = getMemberInfo();

        // set button
        StringBuilder sb = new StringBuilder();
        sb.append(arrayHtml[0]);
        for(String key : memberInfos.keySet()){

            // set button class by status
            String btn_class = "";
            if(memberInfos.get(key).get("STATUS").equals(Status.Working.getStatus()))
                btn_class = "btn btn-primary btn-block btn-lg";
            else if(memberInfos.get(key).get("STATUS").equals(Status.OffWork.getStatus()))
                btn_class = "btn btn-warning btn-block btn-lg";
            else
                btn_class = "btn btn-default btn-block btn-lg";

            StringBuilder value = new StringBuilder();
            value.append(memberInfos.get(key).get("NAME") + " &nbsp;&nbsp;|&nbsp;&nbsp; " + key + "<br>");
            value.append(memberInfos.get(key).get("START") + " - " + memberInfos.get(key).get("END") + " &nbsp;&nbsp;|&nbsp;&nbsp; " + memberInfos.get(key).get("DIFF") + " hr");

            sb.append("<form action=\"index.html\" method=\"post\">");
            sb.append("<input type=\"hidden\" name=\"id\" value=\"" + key + "\" >");
            sb.append("<button type=\"submit\" class=\"" + btn_class + "\">" + value + "</button>");
            sb.append("</form>");
            sb.append("<br>");
        }
        sb.append(arrayHtml[1]);
        List<String> dates = this.getMonth26To26();
        for(String date : dates){
            sb.append("<form action=\"index.html\" method=\"post\">");
            sb.append("<input type=\"hidden\" name=\"record\" value=\"" + date + "\" >");
            sb.append("<button type=\"submit\" class=\"btn btn-link btn-block\">");
            sb.append("Get [" + date + "] record");
            sb.append("</button>");
            sb.append("</form>");
        }
        sb.append(arrayHtml[2]);

        return sb.toString();
    }




    private Map<String, Map<String,String>> getMemberInfo(){
        // get member info
        int count = 0;
        List<MemberInfo> memberInfos = new ArrayList<MemberInfo>();
        DB_open("member");
        Cursor cursor = sql.Select_member();
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                String sID = String.format("%04d", Integer.valueOf(cursor.getString(0)));

                String sPath = "/mnt/sdcard/" + this.mActivity.getPackageName() + "/member";
                String sFile = sID + ".jpg";
                Bitmap bmp = BitmapFactory.decodeFile(sPath + "/" + sFile);

                memberInfos.add(new MemberInfo(cursor.getString(1), sID, bmp));
                mapId.put(sID, count++);
            } while(cursor.moveToNext());    // 有一下筆就繼續迴圈
        }
        DB_close();

        // get time
        Map<String, Map<String, String>> result = new HashMap<>();
        for(MemberInfo member : memberInfos){
            // get time
            String[] sRegistertTime = getRegisterTime(member.getID());
            Status status = Status.NoRecord;
            if(sRegistertTime[0] != null){
                if(sRegistertTime[0].equals(sRegistertTime[1])) { // only check in time
                    sRegistertTime = Register_Adaper.dateConvert(sRegistertTime);
                    status = Status.Working;
                }
                else{   // has check in & out time
                    sRegistertTime = Register_Adaper.dateCompute(sRegistertTime);
                    status = Status.OffWork;
                }
            }
            else{
                sRegistertTime[0] = "";
                sRegistertTime[1] = "";
            }

            Map<String, String> temp = new HashMap<>();
            temp.put("NAME", member.getName());
            temp.put("START", sRegistertTime[0]);
            temp.put("END", sRegistertTime[1]);
            temp.put("DIFF", sRegistertTime[2]);
            temp.put("STATUS", status.getStatus());
            result.put(member.getID(), temp);
        }

        return result;
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
        sDate = GetMonth_of_days(sFileName.substring(0, 10), sFileName.substring(11));

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
        String sDir = this.mActivity.getFilesDir().toString();
        sFileName += ".csv";
        CSV_rw csv_rw = new CSV_rw(sDir, sFileName);
        csv_rw.CSV_write_(sID, sRegisterTime);
    }

    private List<String> getMonth26To26(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar now = Calendar.getInstance();
        List<Calendar> calendarList = new ArrayList<>();

        // get this, last, two month before date
        Calendar temp1 = Calendar.getInstance();
        temp1.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH)-2, 26);
        calendarList.add(temp1);

        Calendar temp2 = Calendar.getInstance();
        temp2.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH)-1, 26);
        calendarList.add(temp2);

        Calendar temp3 = Calendar.getInstance();
        temp3.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 26);
        calendarList.add(temp3);

        // format calendar

        List<String> result = new ArrayList<>();
        result.add(sdf.format(calendarList.get(0).getTime()) + "_" + sdf.format(calendarList.get(1).getTime()));
        result.add(sdf.format(calendarList.get(1).getTime()) + "_" + sdf.format(calendarList.get(2).getTime()));
        return result;
    }

    private String[] getRegisterTime(String sID){
        String[] sResult = new String[3];
        sResult[2] = "0";

        // sql read
        DB_open("register_day");
        Cursor cursor;
        cursor = sql.Select_RegisterToday(sID);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            sResult[0] = cursor.getString(1).substring(11);
            cursor.moveToLast();
            sResult[1] = cursor.getString(1).substring(11);
        }
        DB_close();

        return sResult;
    }

    public void refreshMonthData() {
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
    }

    public void setContext(Activity c){
        this.mActivity = c;
    }

    private String readAssetsFileAsString(String FileName){
        String result = "";
        try {
            AssetManager assetManager = this.mActivity.getAssets();
            InputStream is = assetManager.open(FileName);
            int len = is.available();
            byte[] data = new byte[len];
            is.read(data);
            result = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "readAssetsFileAsString: " + e.toString());
        }
        return result;
    }




    //region SQL
    final static String db_name = "Register.db";
    SQLiteDatabase db;
    SQL sql;

    void DB_close(){
        db.close();
    }

    void DB_open(String tb_name){
        db = SQLiteDatabase.openOrCreateDatabase(this.mActivity.getDatabasePath(db_name).getPath(), null);
        sql = new SQL(db_name, tb_name, db);

        if(!sql.TableIsExist(db_name)){
            sql.TableCreate();
        }
    }
    //endregion
}
