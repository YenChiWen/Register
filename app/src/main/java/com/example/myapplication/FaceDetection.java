package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Pair;
import android.widget.Toast;
import com.viatech.idlib.DigitDetector;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class FaceDetection {
    public Context context;
    public DigitDetector mDigitDetector;

    FaceDetection(Context context, DigitDetector mDigitDetector){
        this.context = context;
        this.mDigitDetector = mDigitDetector;
    }

    public Pair<Boolean, int[]> faceDetection(Bitmap bmpSrc, File cascadeDir, String sModel){
        boolean bResult = false;

        File mCascadeFile = new File(cascadeDir, sModel);
        Bitmap bmpDest = bmpSrc.copy(bmpSrc.getConfig(), true);
        int[] faces = bmpFaceDetection(bmpSrc, bmpDest, mCascadeFile.getAbsolutePath());  //native

        if(faces.length > 0){
            bResult = true;
        }
        return new Pair<Boolean, int[]>(bResult, faces);
    }

    public float[] faceID_feature(Bitmap bmp){
        //inference by bitmap
        float[] faceFeature = mDigitDetector.extract(bmp);

        return faceFeature;
    }

    public void faceID_save(String sID, float[] faceFeature){
        // save feature
        String sDir = "/mnt/sdcard/" + context.getApplicationContext().getPackageName() + "/member";
        String sFileName = "member_feature.csv";
        CSV_rw csv_rw = new CSV_rw(sDir, sFileName);

        csv_rw.CVS_write_faceID(sID, faceFeature);
    }

    public Map<String, float[]> faceID_load(){
        // load feature
        String sDir = "/mnt/sdcard/" + context.getApplicationContext().getPackageName() + "/member";
        String sFileName = "member_feature.csv";
        CSV_rw csv_rw = new CSV_rw(sDir, sFileName);
        Map<String, float[]> mapFeature = csv_rw.CSV_read_faceID();

        return mapFeature;
    }

    public void faceID_remove(String sID){
        // save feature
        String sDir = "/mnt/sdcard/" + context.getApplicationContext().getPackageName() + "/member";
        String sFileName = "member_feature.csv";
        CSV_rw csv_rw = new CSV_rw(sDir, sFileName);

        csv_rw.CVS_delete_faceID(sID);
    }

    public String verify(float score, float[] feature){
        Map<String, float[]> pattern = faceID_load();
        String sResult = "";

        Set<Map.Entry<String, float[]>> set = pattern.entrySet();
        for(Map.Entry<String, float[]> p: set){
            float temp = compareFaceFeature(feature, p.getValue());
            if(score == 0 || score > temp){
                score = temp;
                sResult = p.getKey();
            }
        }

        return sResult;
    }

    public float compareFaceFeature(float[] a, float[] b){
        double sumA=0, sumB=0, cosin=0;
        for(int i=0;i<a.length;i++) {
            sumA += a[i]*a[i];
            sumB += b[i]*b[i];
            cosin += a[i]*b[i];
        }

        sumA=Math.sqrt(sumA);
        sumB=Math.sqrt(sumB);
        if(((sumA-0)<0.0001)||((sumB-0)<0.0001)){
            return 0;
        }

        cosin/=(sumA*sumB);
        return (float) (1-cosin);
    }

    public Bitmap DrawRect(Bitmap bmpSrc, int[] faces){
        Bitmap bmpDest = bmpSrc.copy(bmpSrc.getConfig(), true);
        Canvas canvas = new Canvas(bmpDest);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);
        canvas.drawRect(faces[0], faces[1], faces[0]+faces[2], faces[1]+faces[3], paint);

        return bmpDest;
    }

    public Bitmap CutFace(Bitmap bmpSrc, int[] faces){
        return Bitmap.createBitmap(bmpSrc, faces[0], faces[1], faces[2], faces[3]);
    }



    static {
        System.loadLibrary("native-lib");
    }
    public native int[] bmpFaceDetection(Bitmap bmpSrc, Bitmap bmpDest, String sModelPath);
//    public native boolean InitFaceDetaction();
}
