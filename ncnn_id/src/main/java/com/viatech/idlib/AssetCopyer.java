package com.viatech.idlib;

/**
 * copy the files and folders of assets to sdCard to ensure that we can read files in JNI part
 * @author Qinghao Hu
 * @date   2015/9/22
 * @version 1.0
 * @email qinghao.hu@nlpr.ia.ac.cn
 */


import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AssetCopyer {

    private static String TAG="AssetCopyer";

    /**
     * copy all the files and folders to the destination
     * @param context  application context
     * @param destination the destination path
     */
    public  static void copyAllAssets(Context context, String destination)
    {
        copyAssetsToDst(context,"",destination);
    }
    /**
     *
     * @param context :application context
     * @param srcPath :the path of source file
     * @param dstPath :the path of destination
     */
    private  static void copyAssetsToDst(Context context, String srcPath, String dstPath) {
        int file_flag=-1;

        try {
            String fileNames[] =context.getAssets().list(srcPath);
            if (fileNames.length > 0)
            {
                File file = new File(dstPath);
                if(!file.exists())
                {
                    file.mkdirs();
                }

                String file_lists[] = file.list();
                for (String fileName : fileNames)
                {
                    for (String file_list : file_lists)
                    {
                        if(fileName.equals(file_list)){
                            file_flag=1; //match file file_flag to set 1
                            break;
                        }
                    }
                    if(file_flag!=1) //No match file. To do copy
                    {
                        Log.i(TAG, "No match file ,so should do copy.");
                        if(srcPath!="")
                        {
                            copyAssetsToDst(context,srcPath + "/" + fileName,dstPath+"/"+fileName);
                        }else{
                            copyAssetsToDst(context, fileName,dstPath+"/"+fileName);
                        }
                    }
                    file_flag=-1;

                }
            }else
            {
                InputStream is = context.getAssets().open(srcPath);
                FileOutputStream fos = new FileOutputStream(new File(dstPath));
                byte[] buffer = new byte[1024];
                int byteCount=0;
                while((byteCount=is.read(buffer))!=-1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();//
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}