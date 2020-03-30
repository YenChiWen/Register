/*
   Copyright 2016 Narrative Nights Inc. All Rights Reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.viatech.idlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


/**
 * Handwritten digit detector.
 * <p/>
 * Created by miyoshi on 16/01/17.
 */
public class DigitDetector {

    private static final String TAG = "ViaDetector";

    public boolean create(Context context) {
//        this.loadLibrary();

        String file_path = context.getExternalFilesDir("").getPath()+"/";
        Log.i (TAG, "external path: " + file_path);

        AssetCopyer.copyAllAssets(context.getApplicationContext(), file_path);
        Log.i(TAG, "assets copyed.");
        return NCNNInit(file_path);
    }


    public DigitDetector() {

    }

    public void destroy()
    {
        NCNNExit();
    }


    private native boolean NCNNInit(String file_path);
    private native void    NCNNExit();

    public float[] extract(Bitmap faceCrop) {
        Mat face = new Mat();
        Utils.bitmapToMat(faceCrop, face);
        float[] feature_vector = GetFaceID(face.getNativeObjAddr());
        face.release();
        face = null;

        return feature_vector;
    }


    static {
        System.loadLibrary("squeezencnn_id");
    }
    private native float[] GetFaceID(long matAddr);
}
