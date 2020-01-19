package com.dtw_asr.audio;

public interface AudioDataReceivedListener {
	void start();
    void onAudioDataReceived(byte[] data, int length);
    void stop();
}
