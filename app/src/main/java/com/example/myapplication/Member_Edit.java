package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.viatech.idlib.DigitDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.PendingIntent.getActivity;

public class Member_Edit extends AppCompatActivity {

    String sOld_ID, sOld_name;
    private static final int REQUEST_CAPTURE_IMAGE = 100;
    private FaceDetection FD;
    public DigitDetector mDigitDetector;
    Bitmap bmpFace = null;
    float[] face_feature;
    Thread thread_initial = null;


    //region
    public void Init(){
        // initial class
        thread_initial = new Thread(runnable_initial);
        thread_initial.start();

        // onclick listener
        Button btnCheck = findViewById(R.id.btn_check);
        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnRemove = findViewById(R.id.btn_remove);
        ImageView imageView = findViewById(R.id.imageView);
        btnCheck.setOnClickListener(onClickListener_btnCheck);
        btnCancel.setOnClickListener(onClickListener_btnCancel);
        btnRemove.setOnClickListener(onClickListener_btnRemove);
        imageView.setOnClickListener(onClickListener_imageview);

        // member info load
        if(getIntent().getExtras() != null){
            sOld_ID = getIntent().getExtras().getString("sID");

            // SQL
            DB_open("member");
            Cursor cursor = sql.Select_member(sOld_ID);
            if(cursor.getCount() > 0){
                cursor.moveToFirst();
                sOld_name = cursor.getString(1);
            }
            DB_close();

            // edit
            EditText ET_id = findViewById(R.id.editText_ID);
            EditText ET_name = findViewById(R.id.editText_Name);
            ET_id.setText(sOld_ID);
            ET_name.setText(sOld_name);

            String sPath = "/mnt/sdcard/" + getApplicationContext().getPackageName() + "/member";
            String sFile = sOld_ID + ".jpg";
            Bitmap bmp = BitmapFactory.decodeFile(sPath + "/" + sFile);
            imageView.setImageBitmap(bmp);

            // visibility
            btnRemove.setVisibility(View.VISIBLE);
            ET_id.setEnabled(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void SaveImage(Bitmap bmp, String sFileName){
        try (FileOutputStream out = new FileOutputStream(sFileName)) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable runnable_initial = new Runnable() {
        @Override
        public void run() {
            mDigitDetector = MainActivity.mDigitDetector;
            FD = new FaceDetection(Member_Edit.this, mDigitDetector);
        }
    };
    //endregion




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_edit);

        Init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
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
                    bmpFace = FD.CutFace(bmp, iFaces);
                    face_feature = FD.faceID_feature(bmpFace);

                    // image view
                    Bitmap bmpDrawFace = FD.DrawRect(bmp, iFaces);
                    ImageView imageView = new ImageView(this);
                    imageView.setImageBitmap(bmpDrawFace);

                    // alert dialog
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setMessage("Check face...")
                            .setView(imageView)
                            .setNegativeButton("No", null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ImageView imageView1 = findViewById(R.id.imageView);
                                    imageView1.setImageBitmap(bmp);
                                }
                            })
                            .create();
                    alertDialog.show();
                }
                else{
                    Toast.makeText(this, "-1", Toast.LENGTH_LONG).show();
                    onClickListener_imageview.onClick(null);
                }
            }
        }
    }



    //region listener
    private View.OnClickListener onClickListener_imageview = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(intent.resolveActivity(getPackageManager()) != null){
                startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
            }
        }
    };

    private View.OnClickListener onClickListener_btnCheck = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onClick(View view) {
            ImageView imageView = findViewById(R.id.imageView);
            EditText ET_name = findViewById(R.id.editText_Name);
            EditText ET_no = findViewById(R.id.editText_ID);
            String sName = ET_name.getText().toString();
            String sID = ET_no.getText().toString();

            if(sID.equals("") || sName.equals("")){
                Toast.makeText(Member_Edit.this, "-1", Toast.LENGTH_LONG).show();
            }
            else{
                int iID = Integer.parseInt(sID);

                // SQL
                DB_open("member");
                if(sOld_ID == null){
                    sql.Insert_member(iID, sName);
                }
                else{
                    sql.Update_member(sOld_ID, iID, sName);
                }
                DB_close();

                finish();
            }

            if(imageView.getDrawable() != null){
                String sPath = "/mnt/sdcard/" + getApplicationContext().getPackageName() + "/member";
                String sFile = String.format("%04d", Long.parseLong(sID)) + ".jpg";
                File folder = new File(sPath);
                folder.mkdirs();

                Bitmap bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                SaveImage(bmp, sPath + "/" + sFile);

                if(bmpFace != null){
                    FD.faceID_save(sID, face_feature);
                }
            }
        }
    };

    private View.OnClickListener onClickListener_btnCancel = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };

    private View.OnClickListener onClickListener_btnRemove = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DB_open("member");
            sql.Delete(sOld_ID);
            DB_close();

            String sPath = "/mnt/sdcard/" + getApplicationContext().getPackageName() + "/member";
            String sFile = sOld_ID + ".jpg";
            File file = new File(sPath + "/" + sFile);
            if(file.exists()) {
                file.delete();
                try {
                    thread_initial.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                FD.faceID_remove(sOld_ID);
            }

            finish();
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
        }
    }
    //endregion
}
