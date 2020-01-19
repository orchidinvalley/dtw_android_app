package com.dtw_asr.audio;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;


import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dtw_asr.CallUtils;
import com.dtw_asr.Constants;
import com.dtw_asr.CustomizeKwItem;
import com.dtw_asr.CustomizeKwManager;
import com.dtw_asr.MsgEnum;


public class RecordingThread {
//    static { System.loadLibrary("snowboy-detect-android"); }

    private static final String TAG = RecordingThread.class.getSimpleName();

    private static final String ACTIVE_RES = Constants.ACTIVE_RES;
    private static final String ACTIVE_UMDL = Constants.ACTIVE_UMDL;
    
    private boolean shouldContinue;
    private AudioDataReceivedListener listener = null;
    private Handler handler = null;
    private Thread thread;
    
    private static String strEnvWorkSpace = Constants.DEFAULT_WORK_SPACE;
    private String activeModel = strEnvWorkSpace+ACTIVE_UMDL;
    private String commonRes = strEnvWorkSpace+ACTIVE_RES;
    private String waveFileName=strEnvWorkSpace+"recording";
    

    private MediaPlayer player = new MediaPlayer();

    Mfcc_new mfcc_new=new Mfcc_new(16000,
            (float) 0.025,
            (float) 0.01,
            12,
            24,
            (float) 0.9375,
            512);

    Dtw_check dtw_check=new Dtw_check(10000);

    Activity mActivity;
    ArrayList<float[][]>kwItemList=new ArrayList<float[][]>();
    ArrayList<String>keywords=new ArrayList<String>();


    public RecordingThread(Handler handler, AudioDataReceivedListener listener, Activity mActivity) {
        this.handler = handler;
        this.listener = listener;
        this.mActivity = mActivity;


        try {
            player.setDataSource(strEnvWorkSpace+"ding.wav");
            player.prepare();
        } catch (IOException e) {
            Log.e(TAG, "Playing ding sound error", e);
        }

//        short []data1=mfcc_new.getData(waveFileName+"1.wav");
//        float [][]feature1=CallUtils.getMfcc(data1);
//        short []data2=mfcc_new.getData(waveFileName+"2.wav");
//        float [][]feature2=CallUtils.getMfcc(data2);
//        short []data3=mfcc_new.getData(waveFileName+"3.wav");
//        float [][]feature3=CallUtils.getMfcc(data3);
//        short []data4=mfcc_new.getData(waveFileName+"4.wav");
//        float [][]feature4=CallUtils.getMfcc(data4);
//        short []data5=mfcc_new.getData(waveFileName+"6.wav");
//        float [][]feature5=CallUtils.getMfcc(data5);
//        short []data6=mfcc_new.getData(waveFileName+"7.wav");
//        float [][]feature6=CallUtils.getMfcc(data6);
//        feature=new float[][][]{feature1,feature2,feature4,feature5,feature6};




    }

    public void mfcc_cal(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                kwItemList.clear();
                try {
                    Thread.sleep(1000);
                    for (CustomizeKwItem item : CustomizeKwManager.getInstance().getCustomKwList()) {
                        if (new File(Constants.DEFAULT_WORK_SPACE + item.firstVideoName).exists()) {
                            short[] data1 = mfcc_new.readPcm(Constants.DEFAULT_WORK_SPACE + item.firstVideoName);
                            Log.v(TAG,"min name="+item.firstVideoName);
                            float[][]mfcc1=CallUtils.getMfcc(data1);
                            if(mfcc1!=null) {
                                kwItemList.add(mfcc1);
                                keywords.add(item.firstVideoName);
                            }
                            Thread.sleep(50);
                        }
                        if (new File(Constants.DEFAULT_WORK_SPACE + item.secondVideoName).exists()) {
                            short[] data2 = mfcc_new.readPcm(Constants.DEFAULT_WORK_SPACE + item.secondVideoName);
                            Log.v(TAG,"min name="+item.secondVideoName);
                            float[][]mfcc2=CallUtils.getMfcc(data2);
                            if(mfcc2!=null) {
                                kwItemList.add(mfcc2);
                                keywords.add(item.secondVideoName);
                            }
                            Thread.sleep(50);
                        }
                        if (new File(Constants.DEFAULT_WORK_SPACE + item.thirdVideoName).exists()){
                            short[] data3 = mfcc_new.readPcm(Constants.DEFAULT_WORK_SPACE + item.thirdVideoName);
                            Log.v(TAG,"min name="+item.thirdVideoName);
                            float[][]mfcc3=CallUtils.getMfcc(data3);
                            if(mfcc3!=null) {
                                kwItemList.add(mfcc3);
                                keywords.add(item.thirdVideoName);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }




    private void sendMessage(MsgEnum what, Object obj){
        if (null != handler) {
            Message msg = handler.obtainMessage(what.ordinal(), obj);
            handler.sendMessage(msg);
        }
    }

    public void startRecording() {
        if (thread != null)
            return;

        shouldContinue = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                record();
            }
        });
        thread.start();
    }

    public void stopRecording() {
        if (thread == null)
            return;

        shouldContinue = false;
        thread = null;
    }

    private void record() {
        Log.v(TAG, "Start");
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // Buffer size in bytes: for 0.1 second of audio
        int bufferSize =(int)(Constants.SAMPLE_RATE * 4);
//        int bufferSize=AudioRecord.getMinBufferSize(Constants.SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = Constants.SAMPLE_RATE * 2;
        }

        byte[] audioBuffer = new byte[bufferSize];
        AudioRecord record = new AudioRecord(
            MediaRecorder.AudioSource.MIC,
            Constants.SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "Audio Record can't initialize!");
            return;
        }
        record.startRecording();
        if (null != listener) {
            listener.start();
        }
        Log.v(TAG, "Start recording");

        long shortsRead = 0;

        while (shouldContinue) {
            record.read(audioBuffer, 0, audioBuffer.length);
            
            // Converts to short array.
            short[] audioData = new short[audioBuffer.length / 2];
            ByteBuffer.wrap(audioBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioData);

            shortsRead += audioData.length;
            Log.v(TAG,"audioData dis len ="+audioData.length);


            long start= System.currentTimeMillis();
            float [][]audio_feature=CallUtils.getMfcc(audioData);
//            mfcc_new.setData(audioData);
//            float[][]audio_feature=mfcc_new.mfcccaculate();
            if(audio_feature!=null)
            Log.v(TAG,"feature length="+audio_feature.length);
//            double minValue=1000000;
            int result=0;
            if(audio_feature!=null) {
                for (int i = 0; i < kwItemList.size(); i++) {
                    double value = dtw_check.dtw_detect(audio_feature, kwItemList.get(i));
                    Log.v(TAG,"minValue dis="+value);
                    if(audio_feature.length<50) {
                        if (value < 100000) {
                            result = 1;
                            Log.d(TAG, "minName =" + keywords.get(i));
                            int m=dtw_check.pathList.size();
                            int n=kwItemList.get(i)[0].length;
                            float[][]tmp=new float[m][n];
                            for(int k=m-1;k>=0;k--){
                                for(int j=0;j<n;j++)
                                    tmp[m-k-1][j]=(audio_feature[dtw_check.pathList.get(k)[0]][j]+kwItemList.get(i)[dtw_check.pathList.get(k)[1]][j])/2;
                            }
                            kwItemList.set(i,tmp);
                            break;
                        }
                    }
                    else if(audio_feature.length<85) {
                        if (value < 130000) {
                            result = 1;
                            Log.d(TAG, "minName =" + keywords.get(i));
                            int m=dtw_check.pathList.size();
                            int n=kwItemList.get(i)[0].length;
                            float[][]tmp=new float[m][n];
                            for(int k=m-1;k>=0;k--){
                                for(int j=0;j<n;j++)
                                    tmp[m-k-1][j]=(audio_feature[dtw_check.pathList.get(k)[0]][j]+kwItemList.get(i)[dtw_check.pathList.get(k)[1]][j])/2;
                            }
                            kwItemList.set(i,tmp);
                            break;
                        }
                    }
                    else{
                        if(value<180000)
                        {
                            result=1;
                            Log.d(TAG, "minName ="+keywords.get(i));
                            int m=dtw_check.pathList.size();
                            int n=kwItemList.get(i)[0].length;
                            float[][]tmp=new float[m][n];
                            for(int k=m-1;k>=0;k--){
                                for(int j=0;j<n;j++)
                                    tmp[m-k-1][j]=(audio_feature[dtw_check.pathList.get(k)[0]][j]+kwItemList.get(i)[dtw_check.pathList.get(k)[1]][j])/2;
                            }
                            kwItemList.set(i,tmp);
                            break;
                        }
                    }


//                    minValue = minValue < value ? minValue : value;
//                    if (minValue<130000) {
//                        Log.v(TAG,"minValue i="+i);
//                        Log.d(TAG, "minName ="+keywords.get(i));
//                        break;
//                    }
                }
//                Log.v(TAG,"minValue dis="+minValue);
            }

//            if(minValue<130000)
//                result=1;
//            else
//                result=0;
            Log.v(TAG,"time dis="+(System.currentTimeMillis()-start));

            if (result == -2) {
//                sendMessage(MsgEnum.MSG_VAD_NOSPEECH, null);
            } else if (result == -1) {
                sendMessage(MsgEnum.MSG_ERROR, "Unknown Detection Error");
            } else if (result == 0) {
//                sendMessage(MsgEnum.MSG_VAD_SPEECH, null);
            } else if (result > 0) {
                handler.obtainMessage(1004).sendToTarget();
                Log.i("Snowboy: ", "Hotword " + Integer.toString(result) + "dis detected!");
                player.start();
                break;

            }

//            break;
        }

        record.stop();
        record.release();

        if (null != listener) {
            listener.stop();
        }
        Log.v(TAG, String.format("Recording stopped. Samples read: %d", shortsRead));
    }


}
