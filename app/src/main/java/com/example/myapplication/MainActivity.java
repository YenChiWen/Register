package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.viatech.idlib.DigitDetector;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;


public class MainActivity extends AppCompatActivity{

    private  static final int REQUEST_CAPTURE_IMAGE = 100;
    private FaceDetection FD;
    public static DigitDetector mDigitDetector;
    Thread thread_initial = null;
    private WebService webService = null;
    public static boolean mFlag_FaceDetectEnable = false;

    //region
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void Init(){
        // start web service
        try {
            webService = new WebService(8080);
            webService.setContext(MainActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // navView
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // listener
        FloatingActionButton fab = findViewById(R.id.fab);
        Button btnTouch = findViewById(R.id.btn_Register_Touch);
        Button btnFace = findViewById(R.id.btn_Register_Face);
        FloatingActionButton fabBackup = findViewById(R.id.FAB_backup);
        fab.setOnClickListener(onClickListener_Send);
        btnTouch.setOnClickListener(onClickListener_Touch);
        btnFace.setOnClickListener(onClickListener_Face);
        btnFace.setEnabled(MainActivity.mFlag_FaceDetectEnable);
        fabBackup.setOnClickListener(onClickListener_Backup);

        // face ID initial
        if(mFlag_FaceDetectEnable){
            thread_initial = new Thread(runnable_initial);
            thread_initial.start();
            init_model_file();
        }
    }

    private void init_model_file(){
        try {
            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int byteRead;
            while ((byteRead = is.read(buffer)) != -1){
                os.write(buffer, 0, byteRead);
            }
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable runnable_initial = new Runnable() {
        @Override
        public void run() {
            mDigitDetector = new DigitDetector();
            mDigitDetector.create(MainActivity.this);
            FD = new FaceDetection(MainActivity.this, mDigitDetector);
        }
    };
    //endrgion



    //region
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mFlag_FaceDetectEnable && requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                final Bitmap bmp = (Bitmap) data.getExtras().get("data");

                File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                String sModel = "haarcascade_frontalface_alt2.xml";

                try {
                    thread_initial.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Pair<Boolean, int[]> pair_bool_ints = FD.faceDetection(bmp, cascadeDir, sModel);

                boolean bResult = pair_bool_ints.first;
                int[] iFaces = pair_bool_ints.second;

                if(bResult){
                    final Bitmap bmpFace = FD.CutFace(bmp, iFaces);
                    float[] face_feature = FD.faceID_feature(bmpFace);
                    final String sID = FD.verify((float)0.5, face_feature);

                    if(!sID.equals("")){
                        String sName = "";
                        // get name
                        DB_open("member");
                        Cursor cursor = sql.Select_member(sID);
                        if(cursor.getCount() > 0){
                            cursor.moveToFirst();
                            sName = cursor.getString(1);
                        }
                        DB_close();

                        // image view
                        Bitmap bmpDrawFace = FD.DrawRect(bmp, iFaces);
                        ImageView imageView = new ImageView(this);
                        imageView.setImageBitmap(bmpDrawFace);

                        // alert dialog
                        AlertDialog alertDialog = new AlertDialog.Builder(this)
                                .setTitle("Check face ID")
                                .setMessage("ID: " + sID + "  " + "Name: " + sName)
                                .setView(imageView)
                                .setNegativeButton("No", null)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        Date date = new Date();
                                        String sTime = dateFormat.format(date);

                                        DB_open("register_day");
                                        sql.Insert_register_day(Integer.valueOf(sID), "Touch");
                                        DB_close();
                                        Toast.makeText(MainActivity.this, "Register time:" + sTime, Toast.LENGTH_LONG).show();
                                    }
                                })
                                .create();
                        alertDialog.show();
                    }
                    else{
                        Toast.makeText(this, "-1", Toast.LENGTH_LONG).show();
                        onClickListener_Face.onClick(null);
                    }
                }
                else{
                    Toast.makeText(this, "-1", Toast.LENGTH_LONG).show();
                    onClickListener_Face.onClick(null);
                }
            }
        }
    }
    //endregion



    //region listener
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                default:
                    return false;
                case R.id.navigation_member:
                    onMemberClick();
                    break;
                case R.id.navigation_search:
                    onMonthClick();
                    break;
                case R.id.navigation_send:
                    onDayClick();
                    break;
            }
            return true;
        }

        private void onMemberClick(){
            Intent intent = new Intent(MainActivity.this, Member.class);
            startActivity(intent);
        }

        private void onMonthClick(){
            Intent intent = new Intent(MainActivity.this, Search_month.class);
            startActivity(intent);
        }

        private void onDayClick(){
            Intent intent = new Intent(MainActivity.this, Search_day.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener onClickListener_Touch = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, Touch_Register.class);
            startActivity(intent);
        }
    };


    private View.OnClickListener onClickListener_Face = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(MainActivity.mFlag_FaceDetectEnable && intent.resolveActivity(getPackageManager()) != null){
                startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
            }
        }
    };

    private View.OnClickListener onClickListener_Send = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            SendMail sendMail = new SendMail(MainActivity.this);
            sendMail.SendDialog();
        }
    };

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    View.OnClickListener onClickListener_Backup = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                // 檢查是否有權限寫入
                int permission = ActivityCompat.checkSelfPermission(MainActivity.this,"android.permission.WRITE_EXTERNAL_STORAGE");
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // 没有写的权限，去申请写的权限，会弹出对话框
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String sDB_Name = "Register.db";
            String sPackageName = getApplicationContext().getPackageName();
            String sBackupPath = "/mnt/sdcard/" + sPackageName;
            String sBackupFile = "Register.db";
            File f = new File("/data/data/" + sPackageName + "/databases/" + sDB_Name);
            FileInputStream file_in = null;
            FileOutputStream file_out = null;

            // mkdir directory
            File folder = new File(sBackupPath);
            folder.mkdirs();

            if(folder.exists()){
                // backup db
                try {
                    file_in = new FileInputStream(f);
                    file_out = new FileOutputStream(sBackupPath + "/" + sBackupFile);
                    while (true){
                        int i = file_in.read();
                        if(i != -1)
                            file_out.write(i);
                        else
                            break;
                    }
                    file_out.flush();
                    Toast.makeText(MainActivity.this, "DB dump to " + sBackupPath + " OK.", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "DB dump ERROR.", Toast.LENGTH_LONG).show();
                }
                finally {
                    try {
                        file_in.close();
                        file_out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "-1", Toast.LENGTH_LONG).show();
                    }
                }
            }
            else{
                Toast.makeText(MainActivity.this, sBackupPath + " not exist.", Toast.LENGTH_LONG).show();
            }
        }
    };
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

            if(tb_name.equals("mail_setting")) {
                sql.Insert_mail();
            }
        }
    }
    //endregion
}
