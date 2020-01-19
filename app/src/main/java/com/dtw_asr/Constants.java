package com.dtw_asr;

import android.os.Environment;

import java.io.File;

public class Constants {
    public static final String ASSETS_RES_DIR = "snowboy";
    public static final String DEFAULT_WORK_SPACE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dtw_asr/";
    public static final String DEFAULT_MFCC_SPACE = Environment.getExternalStorageDirectory().getAbsolutePath() + "mfcc";
    public static final String ACTIVE_UMDL = "view_glass.umdl";
    public static final String ACTIVE_RES = "common.res";
    public static final String SAVE_AUDIO = Constants.DEFAULT_WORK_SPACE + File.separatorChar + "recording.pcm";
    public static final int SAMPLE_RATE = 16000;
}
