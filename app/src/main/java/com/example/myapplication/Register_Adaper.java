package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class Register_Adaper extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private List<MemberInfo> memberInfoList;
    public static Context context;
    private ViewHolder viewHolder;

    Register_Adaper(Context context, List<MemberInfo> memberInfos){
        this.context = context;
        this.memberInfoList = memberInfos;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return memberInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        return memberInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return memberInfoList.indexOf(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        viewHolder = null;
        if(view==null){
            view = layoutInflater.inflate(R.layout.member_register, null);
            viewHolder = new ViewHolder(
                    (TextView) view.findViewById(R.id.TV_name),
                    (TextView) view.findViewById(R.id.TV_id),
                    (ImageView) view.findViewById(R.id.IV),
                    (ConstraintLayout) view.findViewById(R.id.linearLayout),
                    (TextView) view.findViewById(R.id.TV_Start),
                    (TextView) view.findViewById(R.id.TV_End),
                    (TextView) view.findViewById(R.id.TV_Total)
            );
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        final MemberInfo memberInfo = (MemberInfo) getItem(i);
        viewHolder.txtName.setText(memberInfo.getName());
        viewHolder.txtID.setText(memberInfo.getID());
        viewHolder.imageView.setImageBitmap(memberInfo.getBmpHead());
        viewHolder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DB_open("register_day");
                sql.Insert_register_day(Integer.valueOf(memberInfo.getID()), "Touch");
                DB_close();
                Toast.makeText(context, "Register time: " + getNowTime(), Toast.LENGTH_LONG).show();
                refresh(memberInfoList);
            }
        });
        viewHolder.constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle("Remove")
                        .setMessage("Remove today's sign in record ...")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // today yyyy-MM-dd
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                Date date = new Date();

                                DB_open("register_day");
                                sql.Delete(memberInfo.getID(), dateFormat.format(date));
                                DB_close();
                                Toast.makeText(context, "Remove " + memberInfo.getID() + " today's record.",Toast.LENGTH_LONG).show();
                                refresh(memberInfoList);
                            }
                        });
                builder.show();
                return false;
            }
        });

        String[] sRegistertTime = getRegisterTime(memberInfo.getID());

        if(sRegistertTime[0] != null){
            if(sRegistertTime[0].equals(sRegistertTime[1])) {
                sRegistertTime = dateConvert(sRegistertTime);
                viewHolder.txtEndTime.setTextColor(context.getResources().getColor(R.color.color_F36363));
                viewHolder.constraintLayout.setBackgroundColor(context.getResources().getColor(R.color.color_053857));
            }
            else{
                sRegistertTime = dateCompute(sRegistertTime);
                viewHolder.txtEndTime.setTextColor(context.getResources().getColor(R.color.color_E2D828));
                viewHolder.constraintLayout.setBackgroundColor(context.getResources().getColor(R.color.color_272727));
            }
        }
        else{
            viewHolder.txtEndTime.setTextColor(context.getResources().getColor(R.color.color_E2D828));
            viewHolder.constraintLayout.setBackgroundColor(context.getResources().getColor(R.color.color_9d9d9d));
        }

        viewHolder.txtStartTime.setText(sRegistertTime[0]);
        viewHolder.txtEndTime.setText(sRegistertTime[1]);
        viewHolder.txtTotal.setText(sRegistertTime[2] + " hr");
        return view;
    }

    private class ViewHolder {
        TextView txtID;
        TextView txtName;
        ImageView imageView;
        ConstraintLayout constraintLayout;
        TextView txtStartTime;
        TextView txtEndTime;
        TextView txtTotal;

        public ViewHolder(TextView txtName, TextView txtID, ImageView imageView, ConstraintLayout constraintLayout, TextView txtStartTime, TextView txtEndTime, TextView txtTotal){
            this.txtName = txtName;
            this.txtID = txtID;
            this.imageView = imageView;
            this.constraintLayout = constraintLayout;
            this.txtStartTime = txtStartTime;
            this.txtEndTime = txtEndTime;
            this.txtTotal = txtTotal;
        }
    }





    //region define-self
    public static String getNowTime(){
        String sResult;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        sResult = dateFormat.format(date);

        return sResult;
    }

    public static String[] dateCompute(String[] s){
        String[] sResult = s;
        double add = 0;
        float fDiff = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date1= null, date2 = null, date3 = null, data_start = null, data_end = null;
        try {
            data_start = sdf.parse(s[0]);
            data_end = sdf.parse(s[1]);
            date1 = sdf.parse("08:30:00");
            date2 = sdf.parse("12:15:00");
            date3 = sdf.parse("13:30:00");

        } catch (ParseException e) {
            e.printStackTrace();
        }

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
        sResult[2] = String.format("%.1f", fDiff);

        return sResult;
    }

    public static String[] dateConvert(String[] s){
        String[] sResult = s;

        // string convert date
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = null, date1= null, date2 = null, date3 = null;
        try {
            date = sdf.parse(s[1]);
            date1 = sdf.parse("08:30:00");
            date2 = sdf.parse("12:15:00");
            date3 = sdf.parse("13:30:00");

        } catch (ParseException e) {
            e.printStackTrace();
        }

        // set hour
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int iHour = calendar.get(Calendar.HOUR_OF_DAY);
        int iMinute = calendar.get(Calendar.MINUTE);
        if(iHour < 16){
            if(date.before(date1) | date.equals(date1)){    // time <= 0830
                calendar.set(Calendar.HOUR_OF_DAY, 17);
                calendar.set(Calendar.MINUTE, 45);
                calendar.set(Calendar.SECOND, 0);
            }
            else if(date.after(date1) && date.before(date2) | date.equals(date2)){     // 0830 < time <= 1215
                calendar.set(Calendar.HOUR_OF_DAY, iHour + 9);
                calendar.set(Calendar.MINUTE, iMinute + 15);
            }
            else if(date.after(date2) && date.before(date3) | date.equals(date3)){      // 1215 < time <= 1330
                calendar.set(Calendar.HOUR_OF_DAY, 21);
                calendar.set(Calendar.MINUTE, 30);
                calendar.set(Calendar.SECOND, 0);
            }
            else if(date.after(date3)){     // time > 1330
                calendar.set(Calendar.HOUR_OF_DAY, iHour + 8);
            }
        }
        else{
            sResult[1] = "";
            return sResult;
        }
        date = calendar.getTime();

        // date convert string
        sResult[1] = sdf.format(date);

        return sResult;
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

    public void refresh(List<MemberInfo> list){
        memberInfoList = list;
        notifyDataSetChanged();
    }
    //endregion





    //region SQL
    final static String db_name = "Register.db";
    SQLiteDatabase db;
    SQL sql;

    void DB_close(){
        db.close();
    }

    void DB_open(String tb_name){
        db = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(db_name).getPath(), null);
        sql = new SQL(db_name, tb_name, db);

        if(!sql.TableIsExist(db_name)){
            sql.TableCreate();
        }
    }
    //endregion
}
