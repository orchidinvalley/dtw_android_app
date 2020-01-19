/*
 * mfcc.c
 *
 *  Created on: 2019年12月26日
 *      Author: Administrator
 */
#include  "mfcc.h"
#include  "Spl.h"
#include  "fft.h"
#include  <malloc.h>
#include  <math.h>
#include  <string.h>
#include  <assert.h>
#include <stdio.h>

//#define  PI   3.1415926
#define  EPS  0.0000001
//#pragma comment(lib, "libfftw3f.lib")


void PreEmphasise(const float *data, int len, float *out, float preF)//预加重
{
	for(int i = len - 1; i >= 1; i--)
	{
		out[i] = data[i] - preF * data[i-1];
	}
	out[0] = data[0];
}

float HzToMel(float  f)
{
	return 1127*log(1.0 + f/700);
}
float MelToHz(float data)
{
	return  700 * (exp(data/1127) - 1);
}
int  HzToN(float f, int fs, int nfft)
{
	return  f/fs *nfft+1;
}
void MelBank( int fs, int nfft, int low, int high, int nfilters, float** coeff )//三角滤波器组。
{
	float  fre_bin = (float)fs / (nfft+1);
	float  low_mel = HzToMel(low);
	float  high_mel = HzToMel(high);
	float  mel_bw  = (high_mel - low_mel)/(nfilters + 1);
	int  valid_nfft = nfft/2 + 1;


	for(int j = 1; j <= nfilters; j++)
	{
		float  mel_cent  = j * mel_bw + low_mel;
		float  mel_left  = mel_cent - mel_bw;
		float  mel_right = mel_cent + mel_bw;
        float  freq_cent =  MelToHz(mel_cent);
		float  freq_left =  MelToHz(mel_left);
		float  freq_bw_left = freq_cent - freq_left;
		float  freq_right = MelToHz(mel_right);
		float  freq_bw_right = freq_right - freq_cent;
		for(int i = 1; i <= valid_nfft; i++)
		{
			float freq = (i-1) * fre_bin ;
			if( freq > freq_left && freq < freq_right )
			{
				if( freq <= freq_cent)
				{
					coeff[j-1][i-1] = (freq - freq_left) / freq_bw_left;
				}
				else
				{
					coeff[j-1][i-1] = (freq_right - freq) / freq_bw_right;
				}

			}
//			printf("%f  ",coeff[j-1][i-1]);

		}
//		printf("\n ");

	}

}

void DctCoeff( int m, int n, float** coeff )//标准DCT变换。
{
	float coef1= 2*sqrt(1.0/(4*n));
	float coef2=2* sqrt(1.0/(2*n));

	for( int i = 0; i < m; i++)
	{
		for(int j = 0; j < n; j++)
		{
			if(i==0)
				coeff[i][j]=coef1;
			else
				coeff[i][j] =coef2* cos( (2*j + 1) * i *PI /(2* n));

		}

	}
}

void lift_window(float* p, int m)//倒谱提升窗归一化。
{
//	float  max_value = 0.0f;
	for(int i = 0; i < m; i++)
	{
		p[i] = 1+ 0.5 * (2*m-2) * sin( PI * i/(2*m-2) );

//		if( p[i] > max_value)
//		{
//			max_value = p[i];
//		}
	}
//	for(int i = 1; i <= m; i++)
//	{
//		p[i] /= max_value;
//	}
}

float Product(float *data1, float* data2, int len)
{
	float result = 0.0;
	for(int i = 0; i < len; i++)
	{

			result += data1[i] * data2[i];
	}

	return result;
}

float** MallocMatrix(int m, int n)
{
	float **in = (float**)malloc(m * sizeof(float*));
	float* data = (float*)malloc( m*n*sizeof(float));
	memset( data, 0, sizeof(float)*m*n );
	for(int i = 1; i <= m; i++)
	{
		in[i-1] = &data[(i-1)*n];
	}
	return in;
}
void FreeMatrix(float **in)
{
	float *data = *in;
	if(data != NULL)
	{
		free(data);
	}
	if(in != NULL)
	{
		free(in);
	}


}
int Mfcc_Frame_diff1_temp(MfccInfo *p, int iframe, float *out, int len);

//初始化，预加重，获取滤波器组系数，DCT系数，倒谱提升窗系数等。
MfccInfo*  MfccInit(AudioInfo audioctx, int nfft, int low, int high, int nfilters, int ndcts, MFCC_TYPE type)
{
	MfccInfo*  p = (MfccInfo*)malloc(sizeof(MfccInfo));
	p->melbank.nfft = nfft;
	p->melbank.low  = low;
	p->melbank.high = high;
	p->melbank.nfilters = nfilters;
	p->dct.dctlen = ndcts;
	p->pre1 = NULL;
	p->pre2 = NULL;
	p->cur  = NULL;
	p->next1 = NULL;
	p->next2 = NULL;
	p->m_type = type;
	p->data_in = audioctx.data_in;//整段语音的数据流
	p->frame_shift = audioctx.frame_shift;
	int valid_nfft = nfft/2 + 1;
	p->melbank.filter = MallocMatrix( nfilters, valid_nfft);
	MelBank( audioctx.sample_rate, nfft, low, high, nfilters, p->melbank.filter);//Mel滤波器系数
	p->dct.coeff = MallocMatrix( ndcts, nfilters);
	DctCoeff( ndcts, nfilters, p->dct.coeff);//DCT系数

	float preF = 0.9375;
	//整段语音高通滤波，预加重
//	PreEmphasise( audioctx.data_in, audioctx.data_len, audioctx.data_in, preF);
	PreEmphasise( audioctx.data_in, audioctx.data_len, p->data_in, preF);
	int nframes = (audioctx.data_len - audioctx.frame_len)/audioctx.frame_shift + 1;
//	p->data_in = audioctx.data_in;//整段语音的数据流
	p->nframes = nframes;
	p->out_nframes = nframes;
	p->frame_len = audioctx.frame_len;
	p->window  = (float*) malloc( audioctx.frame_len * sizeof(float));
	hamming( p->window, audioctx.frame_len);//加窗
	p->lift_window = (float*)malloc( ndcts * sizeof(float));
	lift_window(p->lift_window, ndcts);//倒谱提升窗
	int  buffer_len = audioctx.frame_len > nfft ? audioctx.frame_len:nfft;
	p->frame_data = (float*) malloc( buffer_len * sizeof(float));
	switch(type)
	{
		case MFCC_STD:
		break;
		case MFCC_DIFF_1:
		{
			p->out_nframes = nframes - 4;
			p->pre1 = (float*)malloc( ndcts*sizeof(float));
			p->pre2 = (float*)malloc( ndcts*sizeof(float));
			p->cur  = (float*)malloc( ndcts*sizeof(float));
			p->next1 = (float*)malloc( ndcts*sizeof(float));
			p->next2 = (float*)malloc( ndcts*sizeof(float));
			Mfcc_Frame_std(p, 1, p->pre1, ndcts);
			Mfcc_Frame_std(p, 2, p->pre2,  ndcts);
			Mfcc_Frame_std(p, 3, p->cur, ndcts);
			Mfcc_Frame_std(p, 4, p->next1,  ndcts); //一阶差分需要相邻两帧数据
			break;
		}
		case MFCC_DIFF_2:
		{
			p->out_nframes = nframes - 8;
			p->pre1 = (float*)malloc( ndcts*sizeof(float));
			p->pre2 = (float*)malloc( ndcts*sizeof(float));
			p->cur  = (float*)malloc( ndcts*sizeof(float));
			p->next1 = (float*)malloc( ndcts*sizeof(float));
			p->next2 = (float*)malloc( ndcts*sizeof(float));
			Mfcc_Frame_std(p, 1, p->pre1, ndcts);
			Mfcc_Frame_std(p, 2, p->pre2,  ndcts);
			Mfcc_Frame_std(p, 3, p->cur, ndcts);
			Mfcc_Frame_std(p, 4, p->next1,  ndcts); //一阶差分需要相邻两帧数据

			p->diff_pre1 = (float*)malloc( ndcts*sizeof(float));
			p->diff_pre2 = (float*)malloc( ndcts*sizeof(float));
			p->diff_cur  = (float*)malloc( ndcts*sizeof(float));
			p->diff_next1 = (float*)malloc( ndcts*sizeof(float));
			p->diff_next2 = (float*)malloc( ndcts*sizeof(float));
			Mfcc_Frame_diff1_temp(p,1,p->diff_pre1,ndcts);
			Mfcc_Frame_diff1_temp(p,2,p->diff_pre2,ndcts);
			Mfcc_Frame_diff1_temp(p,3,p->diff_cur,ndcts);
			Mfcc_Frame_diff1_temp(p,4,p->diff_next1,ndcts);//二阶差分需要相邻一阶差分数据
		}
	}
	return p;
}


int Mfcc_Frame_std(MfccInfo *p, int  iframe, float *out, int len)//输出mfcc，任意帧输出
{
	if(iframe > p->nframes)
	{
		return -1;
	}
	memcpy(p->frame_data, p->data_in + (iframe - 1) * p->frame_shift, sizeof(float) * p->frame_len);
//	apply_window( p->frame_data, p->window, p->frame_len);



	int  nfft = pow(2,ceil(log(p->melbank.nfft)/log(2)));
	int  valid_nfft = p->melbank.nfft/2 + 1;

	complex* tmp=(complex*)malloc(sizeof(complex)*nfft);
	for(int i=0;i<nfft;i++){
		if(i<p->frame_len)
			tmp[i].real=p->frame_data[i];
		else
			tmp[i].real=0;
		tmp[i].imag=0;


	}
	fft(nfft,tmp);

//	fftwf_plan r2cP;
//	fftwf_complex* temp = (fftwf_complex*)fftwf_malloc(sizeof( fftwf_complex ) * valid_nfft);
//	r2cP = fftwf_plan_dft_r2c_1d( p->frame_len, p->frame_data, temp, FFTW_ESTIMATE ); //完成FFT运算
//	fftwf_execute( r2cP );
//
	for (int j = 0; j < valid_nfft; ++j)
	{

		p->frame_data[j] = pow( tmp[j].real, 2 ) + pow( tmp[j].imag, 2 );//平方能量值，也可以用谱幅度值

	}


//	fftwf_destroy_plan( r2cP );

	for(int i = 0; i < p->dct.dctlen; i++)
	{
		float temp = 0.0;
		for(int j = 0; j < p->melbank.nfilters; j++)
		{
			//DCT变换，解卷积
//			temp += p->dct.coeff[i-1][j-1] * log(Product(p->frame_data, p->melbank.filter[j-1], valid_nfft)+ EPS)/log(10.0);
			if(i==0)
				temp	+=p->frame_data[j];
			else
				temp += p->dct.coeff[i][j] * log(Product(p->frame_data, p->melbank.filter[j], valid_nfft)+ EPS);
		}
		if(i==0){
			out[i]=log(temp);

		}
		else
			out[i] = temp * p->lift_window[i];//倒谱提升

	}


//	fftwf_free(temp);
	free(tmp);

	return 0;
}

int Mfcc_Frame_diff1(MfccInfo *p, int iframe, float *out, int len)//标准一阶差分，输出 mfcc + 一阶差分。 逐帧输出
{
//   assert(p->nframes >= 5 && iframe <= p->nframes -4 && p->m_type == MFCC_DIFF_1);
   int ret = Mfcc_Frame_std(p, iframe + 4, p->next2, len);
   int dctlen = p->dct.dctlen;
   memcpy( out, p->cur, sizeof(float)* dctlen);//mfcc
   float  factor = sqrt(10.0);
   for(int i = 0; i < dctlen; i++)
   {
	   out[i + dctlen] = (2 * p->next2[i] + p->next1[i] - 2*p->pre1[i] - p->pre2[i])/factor ;//一阶差分
   }

   float *temp = p->pre1;
   p->pre1 = p->pre2;
   p->pre2 = p->cur;
   p->cur  = p->next1;
   p->next1 = p->next2;
   p->next2 = temp;
   return ret;
}

int Mfcc_Frame_diff1_temp(MfccInfo *p, int iframe, float *out, int len)//输出一阶差分
{
	int ret = Mfcc_Frame_std(p, iframe + 4, p->next2, len);
	int dctlen = p->dct.dctlen;
	float  factor = sqrt(10.0);
	for(int i = 0; i < dctlen; i++)
	{
		out[i] = (2 * p->next2[i] + p->next1[i] - 2*p->pre1[i] - p->pre2[i])/factor ;//一阶差分
	}

	float *temp = p->pre1;
	p->pre1 = p->pre2;
	p->pre2 = p->cur;
	p->cur  = p->next1;
	p->next1 = p->next2;
	p->next2 = temp;
	return ret;
}

int Mfcc_Frame_diff2(MfccInfo *p, int iframe, float *out, int len)//输出mfcc+1+2
{
//	assert(p->nframes >= 9 && iframe <= p->nframes -8 && p->m_type == MFCC_DIFF_2);

	int ret = Mfcc_Frame_diff1_temp(p, iframe + 8, p->diff_next2, len);

	int dctlen = p->dct.dctlen;
	memcpy( out, p->next2, sizeof(float)* dctlen);//mfcc
	memcpy( out + dctlen, p->diff_cur, sizeof(float)* dctlen);//一阶差分
	float  factor = sqrt(10.0);
	for(int i = 0; i < dctlen; i++)
	{
		out[i + 2*dctlen] = (2 * p->diff_next2[i] + p->diff_next1[i] - 2*p->diff_pre1[i] - p->diff_pre2[i])/factor ;//二阶差分
	}

	float *temp = p->diff_pre1;
	p->diff_pre1 = p->diff_pre2;
	p->diff_pre2 = p->diff_cur;
	p->diff_cur  = p->diff_next1;
	p->diff_next1 = p->diff_next2;
	p->diff_next2 = temp;
	return ret;
}

int Vad_zrc_amp_cal(float *p,int frame_len,int frame_shift, int iframe,  float *zrc, float *amp,float max){


	float *frame1=(float*)malloc(sizeof(float) * frame_len);
	float *frame2=(float*)malloc(sizeof(float) * frame_len);
	memcpy(frame1, p + (iframe - 1) * frame_shift, sizeof(float) * frame_len);
	memcpy(frame2, p + (iframe - 1) * frame_shift+1, sizeof(float) * frame_len);

	float sum1=0;
	float sum2=0;

//	for(int i=0;i<frame_len;i++){
//		printf("%f %f",frame1[i],frame2[i]);
//		printf("\n");
//	}


//	printf("\n");
	for(int i=0;i<frame_len;i++){
		int sign=(frame1[i]*frame2[i])<0?1:0;
		int diff=(fabs(frame1[i]-frame2[i])/max)>0.02?1:0;
		sum1+=sign*diff;
//		printf("i=%d,sum1=%f \n",i,sum1);
		sum2+=fabs(frame1[i]);

	}
	*zrc=sum1;
	*amp=sum2/max;

	free(frame1);
	free(frame2);
	return 0;
}


void MfccDestroy(MfccInfo *data)
{
	FreeMatrix(data->melbank.filter);
	FreeMatrix(data->dct.coeff);
	if(data->window)
	{
		free(data->window);
		data->window = NULL;
	}
	if(data->lift_window)
	{
		free(data->lift_window);
		data->lift_window = NULL;
	}
	if(data->pre1)
	{
		free(data->pre1);
		data->pre1 = NULL;
	}
	if(data->pre2)
	{
		free(data->pre2);
		data->pre2 = NULL;
	}
	if(data->cur)
	{
		free(data->cur);
		data->cur = NULL;
	}
	if(data->next1)
	{
		free(data->next1);
		data->next1 = NULL;
	}
	if(data->next2)
	{
		free(data->next2);
		data->next2 = NULL;
	}

	if(data->frame_data)
	{
		free(data->frame_data);
		data->frame_data = NULL;
	}

	if(data->diff_pre1)
	{
		free(data->pre1);
		data->pre1 = NULL;
	}
	if(data->diff_pre2)
	{
		free(data->pre2);
		data->pre2 = NULL;
	}

	if(data->diff_cur)
	{
		free(data->cur);
		data->cur = NULL;
	}
	if(data->diff_next1)
	{
		free(data->next1);
		data->next1 = NULL;
	}
	if(data->diff_next2)
	{
		free(data->next2);
		data->next2 = NULL;
	}
}



