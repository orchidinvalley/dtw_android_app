package com.dtw_asr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dtw_asr.audio.RecordPlayThread;

import java.io.File;

import androidx.annotation.Nullable;

import static com.dtw_asr.Constants.DEFAULT_WORK_SPACE;

public class CustomizeKwVoiceRecordActivity extends Activity implements View.OnClickListener {
    ImageView quitView;
    TextView hintText;
    CustomizeKwItem kwItem;
    ImageView recordImg1;
    TextView recordTxt1;
    ImageView playImg1;
    ImageView delImg1;
    boolean rcdFlag1=false;
    boolean playFlag1=false;

    ImageView recordImg2;
    TextView recordTxt2;
    ImageView playImg2;
    ImageView delImg2;
    boolean rcdFlag2=false;
    boolean playFlag2=false;

    ImageView recordImg3;
    TextView recordTxt3;
    ImageView playImg3;
    ImageView delImg3;
    boolean rcdFlag3=false;
    boolean playFlag3=false;

    Button rcdNxt;

    Handler mHandle;
    private final static int MSG_FINISH=1001;
    private final static int WAIT_SHOW=1002;

//    String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/keyword";
    RecordPlayThread recordPlayThread=new RecordPlayThread();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.customize_kw_voice_record);
        initView();
        mHandle=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(isFinishing()||isDestroyed())
                    return;
                switch (msg.what){
                    case MSG_FINISH:
                        CustomizeKwManager.getInstance().clrCustomizeKwItem();
                        if(!rcdFlag1){
                            File file=new File(DEFAULT_WORK_SPACE+kwItem.firstVideoName);
                            if(file!=null && file.exists())
                                file.delete();
                        }
                        if(!rcdFlag2){
                            File file=new File(DEFAULT_WORK_SPACE+kwItem.secondVideoName);
                            if(file!=null && file.exists())
                                file.delete();
                        }
                        if(!rcdFlag3){
                            File file=new File(DEFAULT_WORK_SPACE+kwItem.thirdVideoName);
                            if(file!=null && file.exists())
                                file.delete();
                        }
                        recordPlayThread.destroy();
                        finish();
                        break;
                }
            }
        };
    }
    void  initView(){
        kwItem=CustomizeKwManager.getInstance().getCustomizeKwItem();
        quitView =findViewById(R.id.customize_kw_rcd_quit);
        quitView.setOnClickListener(this);
        hintText = findViewById(R.id.customize_kw_rcd_hint);
        hintText.setText("录音"+"“"+kwItem.name+"”三次\n录音时请选择安静的环境。");

        recordImg1  =findViewById(R.id.customize_kw_rcd_1);
        recordImg1.setOnClickListener(this);
        recordTxt1  =findViewById(R.id.customize_kw_rcd_txt1);
        recordTxt1.setText("点击录音按钮开始录音\n说唤醒词“"+kwItem.name+"”");
        playImg1    =findViewById(R.id.customize_kw_rcd_play1);
        playImg1.setOnClickListener(this);
        playImg1.setVisibility(View.INVISIBLE);
        delImg1     =findViewById(R.id.customize_kw_rcd_del1);
        delImg1.setOnClickListener(this);
        delImg1.setVisibility(View.INVISIBLE);

        recordImg2  =findViewById(R.id.customize_kw_rcd_2);
        recordImg2.setOnClickListener(this);
        recordTxt2  =findViewById(R.id.customize_kw_rcd_txt2);
        recordTxt2.setText("点击录音按钮开始录音\n说唤醒词“"+kwItem.name+"”");
        playImg2    =findViewById(R.id.customize_kw_rcd_play2);
        playImg2.setOnClickListener(this);
        playImg2.setVisibility(View.INVISIBLE);
        delImg2     =findViewById(R.id.customize_kw_rcd_del2);
        delImg2.setOnClickListener(this);
        delImg2.setVisibility(View.INVISIBLE);

        recordImg3  =findViewById(R.id.customize_kw_rcd_3);
        recordImg3.setOnClickListener(this);
        recordTxt3  =findViewById(R.id.customize_kw_rcd_txt3);
        recordTxt3.setText("点击录音按钮开始录音\n说唤醒词“"+kwItem.name+"”");
        playImg3    =findViewById(R.id.customize_kw_rcd_play3);
        playImg3.setVisibility(View.INVISIBLE);
        playImg3.setOnClickListener(this);
        delImg3     =findViewById(R.id.customize_kw_rcd_del3);
        delImg3.setOnClickListener(this);
        delImg3.setVisibility(View.INVISIBLE);

        rcdNxt=findViewById(R.id.customize_kw_rcd_nxt);
        rcdNxt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.customize_kw_rcd_quit:
                mHandle.obtainMessage(MSG_FINISH).sendToTarget();
                break;
            case R.id.customize_kw_rcd_1:
                if(!rcdFlag1) {
                    recordPlayThread.startRecord(DEFAULT_WORK_SPACE + kwItem.firstVideoName);
                    rcdFlag1=true;
                    recordImg1.setImageResource(R.drawable.rcd_stop);
                }
                else {
                    recordImg1.setImageResource(R.drawable.kw_voc);
                    recordTxt1.setText("录音完成    ");
                    recordPlayThread.stopRecord();
                    playImg1.setVisibility(View.VISIBLE);
                    delImg1.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.customize_kw_rcd_2:
                if(!rcdFlag2) {
                    recordPlayThread.startRecord(DEFAULT_WORK_SPACE + kwItem.secondVideoName);
                    rcdFlag2=true;
                    recordImg2.setImageResource(R.drawable.rcd_stop);
                }
                else {
                    recordImg2.setImageResource(R.drawable.kw_voc);
                    recordTxt2.setText("录音完成    ");
                    recordPlayThread.stopRecord();
                    playImg2.setVisibility(View.VISIBLE);
                    delImg2.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.customize_kw_rcd_3:
                if(!rcdFlag3) {
                    recordPlayThread.startRecord(DEFAULT_WORK_SPACE + kwItem.thirdVideoName);
                    rcdFlag3=true;
                    recordImg3.setImageResource(R.drawable.rcd_stop);
                }
                else {
                    recordImg3.setImageResource(R.drawable.kw_voc);
                    recordTxt3.setText("录音完成    ");
                    recordPlayThread.stopRecord();
                    playImg3.setVisibility(View.VISIBLE);
                    delImg3.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.customize_kw_rcd_play1:
                if(!playFlag1) {
                    recordPlayThread.startPlay(DEFAULT_WORK_SPACE + kwItem.firstVideoName);
                    playFlag1=true;
                    playImg1.setImageResource(R.drawable.rcd_stop);
                }
                else {
                    recordPlayThread.stopPlay();
                    playFlag1=false;
                    playImg1.setImageResource(R.drawable.float_view_play);
                }
                break;
            case R.id.customize_kw_rcd_play2:
                if(!playFlag2) {
                    recordPlayThread.startPlay(DEFAULT_WORK_SPACE + kwItem.secondVideoName);
                    playFlag2=true;
                    playImg2.setImageResource(R.drawable.rcd_stop);
                }
                else {
                    recordPlayThread.stopPlay();
                    playFlag2=false;
                    playImg2.setImageResource(R.drawable.float_view_play);
                }
                break;
            case R.id.customize_kw_rcd_play3:
                if(!playFlag3) {
                    recordPlayThread.startPlay(DEFAULT_WORK_SPACE + kwItem.thirdVideoName);
                    playFlag3=true;
                    playImg3.setImageResource(R.drawable.rcd_stop);
                }
                else {
                    recordPlayThread.stopPlay();
                    playFlag3=false;
                    playImg3.setImageResource(R.drawable.float_view_play);
                }
                break;
            case R.id.customize_kw_rcd_del1:
                File file1=new File(DEFAULT_WORK_SPACE+kwItem.firstVideoName);
                if(file1.exists())
                    file1.delete();
                rcdFlag1=false;
                recordTxt1.setText("点击录音按钮开始录音\n说唤醒词“"+kwItem.name+"”");
                recordImg1.setImageResource(R.drawable.kw_rcd);
                playImg1.setVisibility(View.INVISIBLE);
                delImg1.setVisibility(View.INVISIBLE);

                break;
            case R.id.customize_kw_rcd_del2:
                File file2=new File(DEFAULT_WORK_SPACE+kwItem.secondVideoName);
                if(file2.exists())
                    file2.delete();
                rcdFlag2=false;
                recordTxt2.setText("点击录音按钮开始录音\n说唤醒词“"+kwItem.name+"”");
                recordImg2.setImageResource(R.drawable.kw_rcd);
                playImg2.setVisibility(View.INVISIBLE);
                delImg2.setVisibility(View.INVISIBLE);

                break;
            case R.id.customize_kw_rcd_del3:
                File file3=new File(DEFAULT_WORK_SPACE+kwItem.thirdVideoName);
                if(file3.exists())
                    file3.delete();
                rcdFlag3=false;
                recordTxt3.setText("点击录音按钮开始录音\n说唤醒词“"+kwItem.name+"”");
                recordImg3.setImageResource(R.drawable.kw_rcd);
                playImg3.setVisibility(View.INVISIBLE);
                delImg3.setVisibility(View.INVISIBLE);

                break;
            case  R.id.customize_kw_rcd_nxt:
                if(rcdFlag1&&rcdFlag2&&rcdFlag3){
//                    Intent intent=new Intent(CustomizeKwVoiceRecordActivity.this,CustomizeKwTestActivity.class);
////                    startActivity(intent);
                    recordPlayThread.stopRecord();
                    recordPlayThread.stopPlay();
                    CustomizeKwManager.getInstance().addCustomizeKwItem();
                    CustomizeKwManager.getInstance().saveShareReference();
                    mHandle.obtainMessage(MSG_FINISH).sendToTarget();
                }
                else {
                    Toast toast=Toast.makeText(CustomizeKwVoiceRecordActivity.this,"唤醒词录音未完成",Toast.LENGTH_SHORT);
                    toast.setText("唤醒词录音未完成");
                    toast.show();
                }
                break;

        }
    }


    void generateMfccFile(){
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode==KeyEvent.KEYCODE_BACK){
            CustomizeKwManager.getInstance().clrCustomizeKwItem();
            if(recordPlayThread!=null)
                recordPlayThread.destroy();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(recordPlayThread!=null) {
            recordPlayThread.destroy();
            recordPlayThread=null;
        }
    }
}
