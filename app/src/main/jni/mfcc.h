/*
 * mfcc.h
 *
 *  Created on: 2020��1��11��
 *      Author: Administrator
 */

#ifndef SRC_MFCC_H_
#define SRC_MFCC_H_

#define  PI   3.1415926

typedef struct MelBankInfo
{
	float **filter;
	int     nfilters;
	int     nfft;
	int     low;
	int     high;
}MelBankInfo;

typedef struct DctInfo
{
	float  **coeff;
	int     dctlen;
	int		datalen;
}DctInfo;

typedef struct MfccInfo
{
	MelBankInfo   melbank;
	DctInfo       dct;

	int     nframes;
	int     out_nframes;//�������������
	float 	*frame_data;
	float 	*data_in;
	float  	*window;
	float  	*lift_window;
	int 	data_len;
	int     frame_len;
	int     frame_shift;
	float 	preemh;

	float  	*pre1;
	float  	*pre2;
	float  	*cur;
	float  	*next1;
	float  	*next2;

//	float  	*diff_pre1;
//	float  	*diff_pre2;
//	float  	*diff_cur;
//	float  	*diff_next1;
//	float  	*diff_next2;
}MfccInfo;

MfccInfo*  MfccInit(int sample_rate,int frame_len,int frame_shift,int numc,int nfilt,float pre,int nfft);
int Mfcc_Data_Pre(MfccInfo *p,float *data,int datalen);
int Mfcc_Frame_diff(MfccInfo *p, int iframe, float *out, int len);//���һ�ײ��
int Vad_zrc_amp_cal(float *p,int frame_len,int frame_shift, int iframe,  float *zrc, float *amp,float max);
void MfccDestroy(MfccInfo *data);
void FreeMatrix(float **in);
#endif /* SRC_MFCC_H_ */
