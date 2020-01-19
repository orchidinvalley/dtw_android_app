package com.dtw_asr;

import android.content.SharedPreferences;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.SimpleFormatter;

public class CustomizeKwManager {

    private ArrayList<CustomizeKwItem>kwItemList= new ArrayList<CustomizeKwItem>();
    private CustomizeKwItem kwItem=null;
    private static CustomizeKwManager kwManager;

    public CustomizeKwManager(){

        loadShareReference();
    }

    public  static  CustomizeKwManager getInstance(){
        synchronized (CustomizeKwManager.class){
            if(kwManager==null)
                kwManager=new CustomizeKwManager();
            return kwManager;
        }
    }

    private void loadShareReference(){

        File file=new File(Constants.DEFAULT_WORK_SPACE+"list_kw.txt");
        if(file!=null && file.exists()){
            try {
                BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
                String line=null;
                while ((line=bufferedReader.readLine())!=null){
                    if(line !=null && !"".equals(line)) {
                        CustomizeKwItem item=new CustomizeKwItem(line.split("_")[0],line.split("_")[1]);
                        kwItemList.add(item);
                    }
                }
                bufferedReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveShareReference(){
        File file=new File(Constants.DEFAULT_WORK_SPACE+"list_kw.txt");
        if(file!=null){

                try {
                    if (!file.exists())
                        file.createNewFile();
                    BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(file,true));
//                    FileWriter fileWriter=new FileWriter(file);
                    if (kwItem!=null) {
                        bufferedWriter.write(kwItem.name+"_"+kwItem.label);
                        bufferedWriter.write("\n");
                    }
                    bufferedWriter.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public ArrayList<CustomizeKwItem> getCustomKwList(){
        return kwItemList;
    }

    public CustomizeKwItem getCustomizeKwItem(){
        return kwItem;
    }

    public void addCustomizeKwItem(){
        if (kwItemList!=null && kwItem!=null)
            kwItemList.add(kwItem);
    }

    public void setCustomizeKwItem(String name){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date=new Date(System.currentTimeMillis());
        kwItem=new CustomizeKwItem(name,simpleDateFormat.format(date));
    }

    public void clrCustomizeKwItem(){
        kwItem=null;
    }

}
