package com.dtw_asr.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import com.dtw_asr.Constants;
import com.dtw_asr.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

public class RecordPlayThread {
    private MediaPlayer mediaPlayer=new MediaPlayer();
    private Thread thread=null;
    private boolean isRecording=false;
    private boolean isPlaying =false;
    AudioRecord audioRecord=null;
    int rcdBufferSize=0;
    AudioTrack audioTrack=null;
    int plyBufferSize=0;


    public RecordPlayThread( ){

        rcdBufferSize= AudioRecord.getMinBufferSize(Constants.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
        if(rcdBufferSize==AudioRecord.ERROR||rcdBufferSize==AudioRecord.ERROR_BAD_VALUE)
            rcdBufferSize=Constants.SAMPLE_RATE*2;
         audioRecord=new AudioRecord(MediaRecorder.AudioSource.MIC,
                Constants.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                rcdBufferSize
        );
        plyBufferSize=AudioTrack.getMinBufferSize(Constants.SAMPLE_RATE,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);
//        AudioFormat audioFormat= new AudioFormat.Builder().setSampleRate(Constants.SAMPLE_RATE).setEncoding(AudioFormat.ENCODING_PCM_16BIT)
//                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build();
        audioTrack=new AudioTrack(AudioManager.STREAM_MUSIC,
                Constants.SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                plyBufferSize,
                AudioTrack.MODE_STREAM
                );
    }
    public void startRecord(final String filePath){
        if(thread!=null&&isRecording)
            return;
        if(audioTrack!=null)
            audioTrack.stop();
        thread=null;
        thread=new Thread(new Runnable() {
            @Override
            public void run() {
                record(filePath);
            }
        });
        thread.start();
    }

    private void record(final String filePath){
        Log.v(getClass().toString(),"start record");
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        if(audioRecord.getState()!=AudioRecord.STATE_INITIALIZED)
            return;
        isRecording=true;
        audioRecord.startRecording();

        final int finalBufferSize = rcdBufferSize;
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream fileOutputStream=new FileOutputStream(new File(filePath));
                    byte buffer[]=new byte[finalBufferSize];
                    while (isRecording){
                        int read=audioRecord.read(buffer,0, finalBufferSize);
                        if(read!=AudioRecord.ERROR_INVALID_OPERATION)
                            fileOutputStream.write(buffer);
                    }
                    fileOutputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });




    }

    public void stopRecord(){
        if (thread == null)
            return;
        if(audioRecord!=null) {
            audioRecord.stop();
        }
        isRecording = false;
        thread = null;

    }

    public void startPlay(final String filePath){
        if(thread!=null ||isPlaying)
            return;
        thread =new Thread(new Runnable() {
            @Override
            public void run() {
                play(filePath);
            }
        });
        thread.start();

    }
    public void play(final String filePath){
        if(audioTrack.getState()!=AudioTrack.STATE_INITIALIZED)
            return;
        isPlaying=true;
        audioTrack.play();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                while(isPlaying) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(new File(filePath));
                        byte buffer[] = new byte[plyBufferSize];

                        while (fileInputStream.available() > 0) {
                            int readcnt = fileInputStream.read(buffer);
                            if (readcnt == AudioTrack.ERROR_INVALID_OPERATION || readcnt == AudioTrack.ERROR_BAD_VALUE)
                                continue;
                            if (readcnt != 0 && readcnt != -1)
                                audioTrack.write(buffer, 0, plyBufferSize);
                        }
                        fileInputStream.close();
                        Thread.sleep(500);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void stopPlay(){

        if(audioTrack!=null)
            audioTrack.stop();
        isPlaying=false;
        thread =null;
    }

    public void destroy(){
        if(audioRecord!=null) {
            audioRecord.release();
            audioRecord=null;
        }
        if(audioTrack!=null) {
            audioTrack.release();
        }
        if(thread!=null){
            thread=null;
        }
    }

}
