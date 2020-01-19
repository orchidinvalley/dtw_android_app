package com.dtw_asr;

public class CallUtils {
    static {
        System.loadLibrary("use_ndk_build");
    }
    public static native String callSimpleInfo();
//    public static native Int runDetection();
    public static native float[][]getMfcc(short[]data);
}
