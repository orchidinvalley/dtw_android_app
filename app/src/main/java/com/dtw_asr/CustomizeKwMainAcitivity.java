package com.dtw_asr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dtw_asr.audio.RecordPlayThread;
import com.dtw_asr.audio.RecordingThread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.Nullable;

import static com.dtw_asr.Constants.DEFAULT_WORK_SPACE;


public class CustomizeKwMainAcitivity extends Activity implements View.OnClickListener {


    ImageView   quitView;
    ImageView   addView;
    ListView    contentView;
    RelativeLayout testLayout;
    ImageView   testView;

    CustomizeKwListItemAdapter itemAdapter;
    ArrayList<CustomizeKwItem>kwItemList=null;
    String  TAG;
    Handler mHandle;
    private final static int MSG_FINISH=1000;
    private final static int MSG_START_PlAY=1001;
    private final static int MSG_STOP_PLAY=1002;
    private final static int MSG_REFRESH=1003;
    private final static int MSG_MATCH=1004;
    RecordPlayThread recordPlayThread=null;
    Boolean isTesting=false;
    RecordingThread recordingThread=null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.customize_keyword_main);
        kwItemList=CustomizeKwManager.getInstance().getCustomKwList();

        TAG=getPackageName();
        mHandle=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(isDestroyed()||isFinishing())
                    return;
                switch (msg.what){
                    case MSG_FINISH:
                        if(recordPlayThread!=null)
                            recordPlayThread.destroy();
                        if(kwItemList!=null && kwItemList.size()>0) {
                            File file = new File(Constants.DEFAULT_WORK_SPACE + "list_kw.txt");

                                try {
                                    if (!file.exists())
                                        file.createNewFile();
                                    BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(file));
                                    for(CustomizeKwItem kwItem:kwItemList) {
                                        if (kwItem != null) {
                                            bufferedWriter.write(kwItem.name + "_" + kwItem.label);
                                            bufferedWriter.write("\n");
                                        }
                                    }
                                    bufferedWriter.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                        }
                        break;
                    case MSG_START_PlAY:
                        int index=msg.arg1;
                        if(recordPlayThread!=null){
                            recordPlayThread.destroy();
                            recordPlayThread=null;
                        }
                        recordPlayThread=new RecordPlayThread();
                        String fileName=null;
                        if(new File(DEFAULT_WORK_SPACE+kwItemList.get(index).firstVideoName).exists())
                            fileName=DEFAULT_WORK_SPACE+kwItemList.get(index).firstVideoName;
                        else if(new File(DEFAULT_WORK_SPACE+kwItemList.get(index).secondVideoName).exists())
                            fileName=DEFAULT_WORK_SPACE+kwItemList.get(index).secondVideoName;
                        else if (new File(DEFAULT_WORK_SPACE+kwItemList.get(index).thirdVideoName).exists())
                            fileName=DEFAULT_WORK_SPACE+kwItemList.get(index).thirdVideoName;
                        else {
                            recordPlayThread.destroy();
                            recordPlayThread=null;
                            break;
                        }
                        recordPlayThread.startPlay(fileName);
                        break;
                    case MSG_STOP_PLAY:
                        if(recordPlayThread!=null){
                            recordPlayThread.stopPlay();
                            recordPlayThread.destroy();
                            recordPlayThread=null;
                        }
                        break;
                    case MSG_REFRESH:
                        if(kwItemList!=null && kwItemList.size()>0)
                            testLayout.setVisibility(View.VISIBLE);
                        else
                            testLayout.setVisibility(View.GONE);
                        break;
                    case MSG_MATCH:
                        recordingThread.stopRecording();
                        testView.setImageResource(R.drawable.kw_test_suc);
                        isTesting=false;
                        break;
                }
            }
        };
        initView();
        recordingThread=new RecordingThread(mHandle,null,CustomizeKwMainAcitivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        itemAdapter.itemList=CustomizeKwManager.getInstance().getCustomKwList();
        itemAdapter.notifyDataSetChanged();
        if(kwItemList!=null && kwItemList.size()>0) {
            testLayout.setVisibility(View.VISIBLE);
            recordingThread.mfcc_cal();
        }
        else
            testLayout.setVisibility(View.GONE);
    }

    void initView(){
        quitView = findViewById(R.id.customize_kw_quit);
        quitView.setOnClickListener(this);
        addView = findViewById(R.id.add_keyword);
        addView.setOnClickListener(this);
        contentView = findViewById(R.id.customize_kw_list);
        testLayout  = findViewById(R.id.customize_kw_test_back);
        testView    = findViewById(R.id.customize_kw_test);
        testView.setOnClickListener(this);
        initList();

    }

    void initList(){

        itemAdapter=new CustomizeKwListItemAdapter(CustomizeKwMainAcitivity.this,kwItemList,mHandle);
        contentView.setAdapter(itemAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add_keyword:
                if(recordPlayThread!=null) {
                    recordPlayThread.destroy();
                    recordPlayThread=null;
                }
                Intent addIntent=new Intent(CustomizeKwMainAcitivity.this,CustomizeKwNameAddActivity.class);
                startActivity(addIntent);
                break;
            case R.id.customize_kw_quit:
                mHandle.obtainMessage(MSG_FINISH).sendToTarget();
                break;
            case R.id.customize_kw_test:
                if(!isTesting){
                    testView.setImageResource(R.drawable.rcd_stop);
                    isTesting=true;
                    testKeyword();
                }else{
                    testView.setImageResource(R.drawable.kw_test_start);
                    isTesting=false;
                    recordingThread.stopRecording();
                }
                break;
        }
    }

    void testKeyword(){

        recordingThread.startRecording();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode==KeyEvent.KEYCODE_BACK){
            mHandle.obtainMessage(MSG_FINISH).sendToTarget();
        }
        return super.onKeyDown(keyCode, event);
    }


    class CustomizeKwListItemAdapter extends BaseAdapter  {
        private LayoutInflater mInflater;
        private ArrayList<CustomizeKwItem>itemList=new ArrayList<CustomizeKwItem>();
        private Handler mHandle;

        public CustomizeKwListItemAdapter(Context mContex,ArrayList<CustomizeKwItem> list,Handler mHandle){
            mInflater = LayoutInflater.from(mContex);
            itemList=list;
            this.mHandle=mHandle;
        }




        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }



        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View view=null;
            view=mInflater.inflate(R.layout.customize_keyword_list_item,null);
            TextView nameView= view.findViewById(R.id.customize_kw_item_txt);
            CustomizeKwItem item=itemList.get(position);
            nameView.setText(item.name);
            final ImageView playView = view.findViewById(R.id.customize_kw_item_play);
            playView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(kwItemList.get(position).isPlaying){
                        playView.setImageResource(R.drawable.float_view_play);
                        mHandle.obtainMessage(MSG_STOP_PLAY).sendToTarget();
                        kwItemList.get(position).isPlaying=false;
                    }else{
                        playView.setImageResource(R.drawable.rcd_stop);
                        Message msg= mHandle.obtainMessage(MSG_START_PlAY,position,0);
                        mHandle.sendMessage(msg);
                        kwItemList.get(position).isPlaying=true;
                    }
                }
            });
            ImageView delView = view.findViewById(R.id.customize_kw_item_del);
            delView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File file1=new File(DEFAULT_WORK_SPACE+kwItemList.get(position).firstVideoName);
                    if(file1.exists())
                        file1.delete();
                    File file2=new File(DEFAULT_WORK_SPACE+kwItemList.get(position).secondVideoName);
                    if(file2.exists())
                        file2.delete();
                    File file3=new File(DEFAULT_WORK_SPACE+kwItemList.get(position).thirdVideoName);
                    if(file3.exists())
                        file3.delete();
//                    kwItemList.get(position).isDeleted=true;
//                    kwItemList.remove(position);
                    CustomizeKwManager.getInstance().getCustomKwList().remove(position);
                    CustomizeKwManager.getInstance().saveShareReference();
                    notifyDataSetChanged();
                    mHandle.obtainMessage(MSG_REFRESH).sendToTarget();
                }
            });

            return view;
        }


    }
}
