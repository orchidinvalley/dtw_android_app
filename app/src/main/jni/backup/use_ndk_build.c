/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include <com_dtw_asr_CallUtils.h>
#include <mfcc.h>
#include  <malloc.h>
#include <math.h>

#include <android/log.h>

#define LOG_TAG "-------lin---------"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)


/* Header for class com_dtw_asr_CallUtils */

/*
 * Class:     com_dtw_asr_CallUtils
 * Method:    callSimpleInfo
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_dtw_1asr_CallUtils_callSimpleInfo
  (JNIEnv *env, jclass ojb){
  	
//  	int samples = 4000;
//	float *data = (float*)malloc(samples * sizeof(float));
//	for(int i = 0; i < samples; i++)
//	{
//		data[i] = i;
//		LOGI("data=%f",data[i]);
//	}
//	AudioInfo audioctx;
//	audioctx.data_in = data;
//	audioctx.data_len = samples;
//	audioctx.frame_len = 400;
//	audioctx.frame_shift = 128;
//	audioctx.sample_rate = 16000;
//
//	int nfft = audioctx.frame_len;
//	int low = 0;
//	int high = audioctx.sample_rate/2;
//	int nfilters = 24;
//	int ndcts = 12;
//
//
//	    MFCC_TYPE type = MFCC_DIFF_1;
//    	MfccInfo* p = MfccInit( audioctx, nfft, low, high, nfilters, ndcts, type);
//    	int len = ndcts * 2;
//    	float *out = (float*) malloc( sizeof(float)* (len));
//    	float *zrc = (float*) malloc( sizeof(float)*p->out_nframes);
//    	float *amp = (float*) malloc( sizeof(float)*p->out_nframes);
//
//    	float amp1 = 10.0;
//    	float amp2 = 2.0;
//    	float zrc2 = 5;
//
//    	int status=0;
//    	int count=0;
//    	int silence=0;
//
//    	float sumAmp =0;
//
//    	int start=0;
//    	int end=0;
//
//    	int valid=0;
//
//    	for(int j = 0; j < p->out_nframes; j++)//必须按顺序输出，不能直接获取指定帧的mfcc差分
//    	{
//    		Mfcc_Frame_diff1(p, j+1, out, len);
////    		Vad_zrc_amp_cal(p,j+1,zrc+j,amp+j);
//    		if(j<10)
//    			sumAmp+=amp[j];
//    	}
//
//    	sumAmp*=3;
//
//    	amp1=(amp1<sumAmp)?sumAmp:amp1;
//    	amp2=(amp2<sumAmp/2)?sumAmp/2:amp2;
//
//    	for(int i=0;i<p->out_nframes;i++){
//
//    		switch(status){
//    		case 0:
//
//    		case 1:
//    			if(amp[i]>amp1) {
//    				start = (i-count-1)>0?(i-count-1):0;
//    				status = 2;
//    				silence = 0;
//    				count += 1;
//    				valid=1;
//    			}
//    			else if(amp[i] > amp2 || zrc[i] > zrc2) {
//    				count +=1;
//    				status = 1;
//    			}
//    			else {
//    				count = 0;
//    				status = 0;
//    			}
//    			break;
//    		case 2:
//    			break;
//    		case 3:
//    			break;
//    		default:
//    			status=0;
//    			count=0;
//    			silence=0;
//    			break;
//    		}
//
//    	}
//
//    	count =count - silence/2;
//    	end =start+count-1;
//    	end =end>p->out_nframes?p->out_nframes:end;
//    	if(start>=end)
//    		valid=0;


  return (*env) -> NewStringUTF(env,"Hello, I'm an info come from use ndk-build");

}

JNIEXPORT jobjectArray JNICALL Java_com_dtw_1asr_CallUtils_getMfcc
  (JNIEnv *env, jclass obj, jshortArray Attr){

        int samples =(*env)->GetArrayLength(env,Attr);
        short *tmp  =  (short*)malloc(samples*sizeof(short));
    	(*env)->GetShortArrayRegion(env,Attr,0,samples,tmp);
//
//    	samples = 4000;
//        	float *data = (float*)malloc(samples * sizeof(float));
//        	for(int i = 0; i < samples; i++)
//        	{
//        		data[i] = i;
//        	}
//        LOGI("samples=%d",samples);
        float max=0;
    	float *data = (float*)malloc(samples * sizeof(float));
    	for(int i = 0; i < samples; i++)
        		{
//        		        			if(i<200)
//                        			    LOGI("data =%d",tmp[i]);
        			data[i] = (float)tmp[i]/(32768.0);

        			if(max<fabs(data[i]))
        				max=fabs(data[i]);
        		}

//            LOGI("max=%f",max);
        	AudioInfo audioctx;
        	    	audioctx.data_in = data;
        	    	audioctx.data_len = samples;
        	    	audioctx.frame_len = 400;
        	    	audioctx.frame_shift = 160;
        	    	audioctx.sample_rate = 16000;

        	    	int nfft = audioctx.frame_len;
        	    	int low = 0;
        	    	int high = audioctx.sample_rate/2;
        	    	int nfilters = 24;
        	    	int ndcts = 12;


        	        //printf("----2");
        	        int nframes = (audioctx.data_len - audioctx.frame_len)/audioctx.frame_shift + 1;
//        	        LOGI("nframes=%d",nframes);
        	    	float *zrc = (float*) malloc( sizeof(float)*nframes);
        	    	float *amp = (float*) malloc( sizeof(float)*nframes);
        	    	float amp1 = 10.0;
        	    	float amp2 = 2.0;
        	    	float zrc2 = 5;

        	    	int status=0;
        	    	int count=0;
        	    	int silence=0;
        	    	double maxSilence = 8;
        	    	double minLen= 15;

        	    	float sumAmp =0;

        	    	int start=0;
        	    	int end=0;
        	    	int valid=0;

        	    	for(int j = 0; j <nframes ; j++)//必须按顺序输出，不能直接获取指定帧的mfcc差分
        	    	{
        	    		Vad_zrc_amp_cal(data,audioctx.frame_len,audioctx.frame_shift,j+1,zrc+j,amp+j,1);

                        if(sumAmp<amp[j])
                            sumAmp=amp[j];

        	    	}
        	    	LOGI("sumAmp=%f",sumAmp);
                    sumAmp/=4;

        	    	amp1=(amp1<sumAmp)?sumAmp:amp1;
        	    	amp2=(amp2<sumAmp/2)?sumAmp/2:amp2;
                    LOGI("amp1=%f",amp1);
//                    LOGI("amp2=%f",amp2);

        	    	for(int i=0;i<nframes;i++){
        	    		switch(status){
        	    		case 0:
        	    		case 1:
        	    			if(amp[i]>amp1) {
        	    				start = (i-count-1)>0?(i-count-1):0;
//        	    				LOGI("i=%d",i);
//        	    				LOGI("count=%d",count);
        	    				status = 2;
        	    				silence = 0;
        	    				count += 1;
        	    			}
        	    			else if(amp[i] > amp2 || zrc[i] > zrc2) {
        	    				count +=1;
        	    				status = 1;
        	    			}
        	    			else {
        	    				count = 0;
        	    				status = 0;
        	    			}
        	    			break;
        	    		case 2:
        	    			if (amp[i] > amp2 || zrc[i] >zrc2)
        	    				count += 1;
        	    			else  {
        	    				silence+=1;
        	    				if (silence < maxSilence)
        	    					count += 1;
        	    			    else if( count < minLen) {
        	    			    	status = 0;
        	    			        count = 0;
        	    			        silence = 0;
        	    			    }
        	    			    else
        	    			    	status =3;
        	    			 }
        	    			break;
        	    		case 3:

        	    				valid=1;
        	    			break;
        	    		default:
        	    			status=0;
        	    			count=0;
        	    			silence=0;
        	    			break;
        	    		}

        	    	}
        	        //printf("----4");
        	        if(valid==0){
                       LOGI("no voice");
                       return NULL;
                    }
        	    	count =count - silence/2;
        	    	end =start+count-1;
        	    	end =end>nframes?nframes:end;
                    LOGI("start=%d",start);
                     LOGI("end=%d",end);
        	    	if(start+5>=end){
        	    	    LOGI("----return");
        	    	    return NULL;
        	    	}




       	MFCC_TYPE type = MFCC_DIFF_1;
       	LOGI("----5");
        MfccInfo* p = MfccInit( audioctx, nfft, low, high, nfilters, ndcts, type);
        LOGI("----6");
        int len = ndcts * 2;
        float *out = (float*) malloc( sizeof(float)* (len));
        jobjectArray ret;
        int x=end-start+1;
        int y=len;
         LOGI("x=%d",x);
         LOGI("y=%d",y);
        jclass floatCls=(*env)->FindClass(env,"[F");
        if(floatCls==NULL){
        LOGI("----null");
            return NULL;
            }
        ret = (*env)->NewObjectArray(env,x,floatCls,NULL);
        LOGI("----5");
        for(int j=0;j<p->out_nframes;j++){
            Mfcc_Frame_diff1(p, j+1, out, len);
            if(j>=start && j<=end){

                jfloatArray floatArr = (*env)->NewFloatArray(env,y);
                (*env)->SetFloatArrayRegion(env,floatArr,0,y,out);
                (*env)->SetObjectArrayElement(env,ret,j-start,floatArr);
                (*env)->DeleteLocalRef(env,floatArr);
            }

        }
        LOGI("----6");
        free(tmp);
        tmp=NULL;
        free(data);
	    data = NULL;
	    free(out);
	    out = NULL;
	    MfccDestroy(p);
        return ret;


  }
  