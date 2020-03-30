package com.example.myapplication;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class SQL{
    public String db_name;
    public String tb_name;
    public SQLiteDatabase db;

    SQL(String DB_name, String TB_name, SQLiteDatabase DB){
        db_name = DB_name;
        tb_name = TB_name;
        db = DB;
    }

    public void TableCreate(){
        String sCreateTabel = "";

        switch (tb_name){
            case "member":
                sCreateTabel = "CREATE TABLE IF NOT EXISTS " +
                        tb_name +
                        "(_id INTEGER PRIMARY KEY, " +
                        "_name VARCHAR(32))";
                break;
            case "register_month":
                sCreateTabel = "CREATE TABLE IF NOT EXISTS " +
                        tb_name +
                        "(_id INTEGER, " +
                        "_RegisterTime DATETIME, " +
                        "_method char(10))";
                break;
            case "register_day":
                sCreateTabel = "CREATE TABLE IF NOT EXISTS " +
                        tb_name +
                        "(_id INTEGER, " +
                        "_RegisterTime TIMESTAMP DEFAULT (datetime('now','localtime')), " +
                        "_method char(10))";
                break;
            case "mail_setting":
                sCreateTabel = "CREATE TABLE IF NOT EXISTS " +
                        tb_name +
                        "(_mail VARCHAR(32))";
                break;
            case "face_id":

        }

        db.execSQL(sCreateTabel);
    }

    public boolean TableIsExist(String tb_name){
        boolean result = false;

        if(tb_name == null){
            return false;
        }

        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from Sqlite_master where type ='table' and name ='"+tb_name.trim()+"' ";
            cursor = db.rawQuery(sql, null);
            if(cursor.getInt(0) > 0){
                result = true;
            }
        } catch (Exception e) {

        }
        return result;
    }

    public void Delete(String sID){
        db.delete(tb_name, "_id=" + sID, null);
    }




    // region mail
    public void Insert_mail(){
        ContentValues contentValues = new ContentValues(1);
        contentValues.put("_mail", "");

        db.insert(tb_name, null, contentValues);
    }

    public void Update_mail(String sMail){
        ContentValues contentValues = new ContentValues(1);
        contentValues.put("_mail", sMail);

        db.update(tb_name, contentValues, null, null);
    }

    public Cursor Selete_mail(){
        Cursor cursor =db.rawQuery("SELECT * FROM " + tb_name, null);
        return cursor;
    }
    // endregion





    //region Member
    public void Insert_member(int iID, String sName){
        ContentValues contentValues = new ContentValues(2);
        contentValues.put("_id", iID);
        contentValues.put("_name", sName);

        db.insert(tb_name, null, contentValues);
    }

    public void Update_member(String sOld_ID, int iID, String sName){
        ContentValues contentValues = new ContentValues(2);
        contentValues.put("_id", iID);
        contentValues.put("_name", sName);

        db.update(tb_name, contentValues, "_id=" + sOld_ID, null);
    }

    public Cursor Select_member(String sID){
        Cursor cursor =db.rawQuery("SELECT * FROM " + tb_name + " WHERE _id=" + sID , null);
        return cursor;
    }

    public Cursor Select_member(){
        Cursor cursor =db.rawQuery("SELECT * FROM " + tb_name, null);
        return cursor;
    }
    //endregion





    //region Register day
    public void Delete(String sID, String sDate){
        String sQuery = "delete from " + tb_name + " where _id=" + sID + " and date(_RegisterTime)=date('"+ sDate + "')";
        db.execSQL(sQuery);
    }

    public void Delete_3month(){
        String sQuery = "delete from " + tb_name + " where _RegisterTime < datetime('now', '-3 month')";
        db.execSQL(sQuery);
    }

    public void Insert_register_day(int iID, String sMethod){
        ContentValues contentValues = new ContentValues(2);
        contentValues.put("_id", iID);
        contentValues.put("_method", sMethod);
        db.insert(tb_name, null, contentValues);
    }

    public Cursor Select_RegisterToday(String sID){
        Cursor cursor =db.rawQuery("SELECT * FROM " + tb_name + " WHERE strftime(\"%Y-%m-%d\", _RegisterTime)=strftime(\"%Y-%m-%d\", date('now')) AND _id=" + sID + " ORDER BY _RegisterTime ASC", null);
        return cursor;
    }

    public Cursor Select_RegisterDay(String sID, String sDate){

        Cursor cursor = null;
        if(!TableIsExist("member") && !TableIsExist("register_day")){
            String sQuery = "SELECT member._id, member._name, register_day._RegisterTime, register_day._method " +
                    "from member inner join register_day " +
                    "on member._id = register_day._id ";

            if(!sDate.equals("")){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

                Date date1 = null, date2=null;
                String sDate1=null, sDate2=null;
                try {
                    date1 = sdf.parse(sDate);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date1);
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                    date2 = calendar.getTime();

                    sDate1 = sdf.format(date1);
                    sDate2 = sdf.format(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(!sID.equals("")){
                    sQuery = sQuery + "where member._id=" + sID + " and register_day._RegisterTime between date('" + sDate1 + "') and date('" + sDate2 + "')";
                }
                else{
                    sQuery = sQuery + "where register_day._RegisterTime between date('" + sDate1 + "') and date('" + sDate2 + "')";
                }
            }
            else{
                if(!sID.equals("")){
                    sQuery = sQuery + "where member._id=" + sID;
                }
            }
            cursor =db.rawQuery(sQuery, null);
        }
        return cursor;
    }
    //endregion



    //region register_month
    public Cursor Select_Month(){
        String sQuery = "select date(_RegisterTime) from " + tb_name + " group by date(_RegisterTime) order by date(_RegisterTime) desc";
        Cursor cursor =db.rawQuery(sQuery, null);
        return  cursor;
    }

    public Cursor Select_register_month(String sID, String sStartDate, String sEndDate){
        String sQuery = "select min(_RegisterTime), max(_RegisterTime) " +
                "from " + tb_name +
                " where _id=" + sID + " and date(_RegisterTime) between date('"+ sStartDate +"') and date('"+ sEndDate +"') " +
                "group by date(_RegisterTime)";
        Cursor cursor =db.rawQuery(sQuery, null);
        return  cursor;
    }

    public void Insert_register_day2month(String date1, String date2){

        if(!TableIsExist("register_month") && !TableIsExist("register_day")){
            String sQuery = "insert into register_month (_id, _RegisterTime, _method) " +
                    "select _id, min(_RegisterTime), _method from register_day " +
                    "where register_day._RegisterTime between date('"+ date1 +"') and date('"+ date2 +"') " +
                    "group by register_day._id";
            db.execSQL(sQuery);
            sQuery =    "insert into register_month (_id, _RegisterTime, _method) " +
                    "select _id, max(_RegisterTime), _method from register_day " +
                    "where register_day._RegisterTime between date('"+ date1 +"') and date('"+ date2 +"') " +
                    "and register_day._RegisterTime != (Select _RegisterTime from register_month where _id=register_day._id)" +
                    "group by register_day._id";
            db.execSQL(sQuery);
        }
    }

    public void Insert_register_day2month_(String sID, String date){
        if(!TableIsExist("register_month") && !TableIsExist("register_day")){
            if(date == null){
                String sQuery = "insert into register_month (_id, _RegisterTime, _method)" +
                        " select _id, min(_RegisterTime), _method from register_day" +
                        " where register_day._id=" + sID +
                        " group by strftime(\"%Y-%m-%d\", register_day._RegisterTime);";
                db.execSQL(sQuery);
                sQuery =    "insert into register_month (_id, _RegisterTime, _method)" +
                        " select _id, max(_RegisterTime), _method from register_day " +
                        " where register_day._id = " + sID +
                        " and register_day._RegisterTime != (Select _RegisterTime from register_month where _id=" + sID + ")" +
                        " group by strftime(\"%Y-%m-%d\", register_day._RegisterTime);";
                db.execSQL(sQuery);
            }
            else{
                String sQuery = "insert into register_month (_id, _RegisterTime, _method)" +
                        " select _id, min(_RegisterTime), _method from register_day" +
                        " where register_day._id=" + sID +
                        " and register_day._RegisterTime >= date('" + date +"')" +
                        " and register_day._RegisterTime != (Select _RegisterTime from register_month where _id=" + sID + ")" +
                        " group by strftime(\"%Y-%m-%d\", register_day._RegisterTime);";
                db.execSQL(sQuery);
                sQuery =    "insert into register_month (_id, _RegisterTime, _method)" +
                        " select _id, max(_RegisterTime), _method from register_day " +
                        " where register_day._id = " + sID +
                        " and register_day._RegisterTime >= date('" + date +"')" +
                        " and register_day._RegisterTime != (Select _RegisterTime from register_month where _id=" + sID + ")" +
                        " group by strftime(\"%Y-%m-%d\", register_day._RegisterTime);";
                db.execSQL(sQuery);
            }
        }
    }

    public Cursor Select_register_month_max_date(){
        String sQuery = "select max(_RegisterTime) from " + tb_name;
        Cursor cursor =db.rawQuery(sQuery, null);
        return  cursor;
    }
    //endregion
}
