package com.example.myapplication;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CSV_rw {
    String sDir = "";
    String sFileName = "";

    public CSV_rw(String sDir, String sFileName){
        this.sDir = sDir;
        this.sFileName = sFileName;
    }

    public void CSV_write(ArrayList<String> sMonth_of_day, ArrayList<String> sMember_name,
                           ArrayList<String> sMemberID, List<List<String>> sSignIn, List<List<String>> sSigeOut,
                           List<List<String>> sTotal){

        String filePath = sDir + File.separator + sFileName;
        CSVWriter writer;

        // File exist
        try {
            writer = new CSVWriter(new FileWriter(filePath));

            ArrayList<String> sTemp = new ArrayList<String>();
            // title -> name
            sTemp.add(""); //date
            for(int i=0; i<sMemberID.size(); i++){
                sTemp.add(sMember_name.get(i));
                sTemp.add(sMemberID.get(i));
                sTemp.add("");
            }
            writer.writeNext(sTemp.toArray(new String[0]));
            sTemp.clear();

            //
            sTemp.add("");
            for(int i=0; i<sMemberID.size(); i++){
                sTemp.add("Clock-in");
                sTemp.add("Clock-out");
                sTemp.add("Total");
            }
            writer.writeNext(sTemp.toArray(new String[0]));
            sTemp.clear();

            // content
            for (int i=0; i<sMonth_of_day.size()-1; i++){
                sTemp.add(sMonth_of_day.get(i));
                for(int j=0; j<sMemberID.size(); j++){
                    sTemp.add(sSignIn.get(j).get(i));
                    sTemp.add(sSigeOut.get(j).get(i));
                    sTemp.add(sTotal.get(j).get(i));
                }
                writer.writeNext(sTemp.toArray(new String[0]));
                sTemp.clear();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CSV_write_(ArrayList<String> sMemberID, ArrayList<String> sRegisterTime){

        String filePath = sDir + File.separator + sFileName;
        CSVWriter writer;

        // File exist
        try {
            writer = new CSVWriter(new FileWriter(filePath));

            ArrayList<String> sWrite_Temp = new ArrayList<String>();
            sWrite_Temp.add("ID");
            sWrite_Temp.add("Time");
            writer.writeNext(sWrite_Temp.toArray(new String[0]));
            sWrite_Temp.clear();

            for(int i=0; i<sRegisterTime.size(); i++){
                sWrite_Temp.add(sMemberID.get(i));
                sWrite_Temp.add(sRegisterTime.get(i));

                writer.writeNext(sWrite_Temp.toArray(new String[0]));
                sWrite_Temp.clear();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, float[]> CSV_read_faceID(){
        String filePath = sDir + File.separator + sFileName;
        File f = new File(filePath);
        CSVReader csvReader;
        Map<String, float[]> mapResult = new HashMap<>();

        try{
            if(f.exists()&&!f.isDirectory())
            {
                Reader reader = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    reader = Files.newBufferedReader(Paths.get(filePath));
                }
                csvReader = new CSVReader(reader);

                String[] s;
                while ((s = csvReader.readNext()) != null){
                    float[] temp = new float[s.length-1];
                    for (int i=1; i < s.length; i++){
                        temp[i-1] = Float.valueOf(s[i]);
                    }

                    mapResult.put(s[0], temp);
                }
                csvReader.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        return mapResult;
    }

    public void CVS_write_faceID(String sID, float[] face){
        Map<String, float[]> pattern = CSV_read_faceID();

        String filePath = sDir + File.separator + sFileName;
        CSVWriter writer;

        // File exist
        try {
            writer = new CSVWriter(new FileWriter(filePath));
            ArrayList<String> sWrite_Temp = new ArrayList<String>();

            // rewrite old member
            Set<Map.Entry<String, float[]>> set = pattern.entrySet();
            for(Map.Entry<String, float[]> p: set){
                if(sID.equals(String.format("%04d", Long.parseLong(p.getKey()))))
                    continue;

                sWrite_Temp.add(p.getKey());
                for(int i=0; i<p.getValue().length; i++){
                    sWrite_Temp.add(String.valueOf(p.getValue()[i]));
                }
                writer.writeNext(sWrite_Temp.toArray(new String[0]));
                sWrite_Temp.clear();
            }

            // add new member
            sWrite_Temp.add(sID);
            for(int i=0; i<face.length; i++){
                sWrite_Temp.add(String.valueOf(face[i]));
            }
            writer.writeNext(sWrite_Temp.toArray(new String[0]));
            sWrite_Temp.clear();

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CVS_delete_faceID(String sID){
        Map<String, float[]> pattern = CSV_read_faceID();

        String filePath = sDir + File.separator + sFileName;
        CSVWriter writer;

        // File exist
        try {
            writer = new CSVWriter(new FileWriter(filePath));
            ArrayList<String> sWrite_Temp = new ArrayList<String>();

            // rewrite old member
            Set<Map.Entry<String, float[]>> set = pattern.entrySet();
            for(Map.Entry<String, float[]> p: set){
                if(sID.equals(String.format("%04d", Long.parseLong(p.getKey()))))
                    continue;

                sWrite_Temp.add(p.getKey());
                for(int i=0; i<p.getValue().length; i++){
                    sWrite_Temp.add(String.valueOf(p.getValue()[i]));
                }
                writer.writeNext(sWrite_Temp.toArray(new String[0]));
                sWrite_Temp.clear();
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
