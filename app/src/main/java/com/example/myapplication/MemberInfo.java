package com.example.myapplication;

import android.graphics.Bitmap;

import java.lang.reflect.Method;

public class MemberInfo {
    private String sName;
    private String sID;
    private Bitmap bmpHead;

    public MemberInfo(String sName, String sID, Bitmap bmpHead){
        this.sName = sName;
        this.sID = sID;
        this.bmpHead = bmpHead;
    }

    public String getName(){
        return sName;
    }

    public String getID(){
        return sID;
    }

    public Bitmap getBmpHead(){
        return bmpHead;
    }
}
