/*
 * mfcc.c
 *
 *  Created on: 2020��1��11��
 *      Author: Administrator
 */
#include "mfcc.h"
#include "fft.h"
#include <malloc.h>
#include <math.h>
#include  <string.h>
#include <stdio.h>

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

float HzToMel(float  f)
{
	return 1127*log(1.0 + f/700);
}
float MelToHz(float data)
{
	return  700 * (exp(data/1127) - 1);
}


void MelBank( int fs, int nfft, int low, int high, int nfilters, float** coeff )//�����˲����顣
{
			float  lowmel=HzToMel(low);
			float  highmel=HzToMel(high);
			float melpoint[nfilters+2];


		    melpoint[0]=lowmel;
		    melpoint[nfilters+1]=highmel;
		    for(int i=1;i<=nfilters;i++)
		    	melpoint[i]=i*highmel/(nfilters+1);
		    for(int i=0;i<nfilters+2;i++)
		    	melpoint[i]=(int) floor((nfft+1)*(MelToHz(melpoint[i])/fs));

//			int  valid_nfft = nfft/2 + 1;
//		    for (int i=0;i<nfilters;i++)
//		    	for(int j=0;j<valid_nfft;j++)
//		    		coeff[i][j]=0;

		    for(int j=0;j<nfilters;j++) {
		    	for (int i=(int)melpoint[j];i<(int)melpoint[j+1];i++)
		    		coeff[j][i]=(i-melpoint[j])/(melpoint[j+1]-melpoint[j]);
		    	for(int i=(int)melpoint[j+1];i<(int)melpoint[j+2];i++)
		    		coeff[j][i]=(melpoint[j+2]-i)/(melpoint[j+2]-melpoint[j+1]);
		    }
//		    for(int i=0;i<nfilters;i++){
//		    		    	for(int j=0;j<valid_nfft;j++)
//		    		    		printf("%f ",coeff[i][j]);
//		    		    	printf("\n");
//		    		    }


		       return ;

}

void DctCoeff( int m, int n, float** coeff )//��׼DCT�任��
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
//			printf("%f  ",coeff[i][j]);
		}
//		printf("\n");

	}
}


void lift_window(float* p, int m)//������������һ����
{
	for(int i = 0; i < m; i++)
	{
		p[i] = 1+ 0.5 * (2*m-2) * sin( PI * i/(2*m-2) );
//		printf("%f  ",p[i]);
	}
}

void PreEmphasise(float *data, int len, float *out, float preF)//Ԥ����
{
	for(int i = len - 1; i >= 1; i--)
	{
		out[i] = data[i] - preF * data[i-1];
	}
	out[0] = data[0];
}

float Product(float *data1, float* data2, int len)
{
	float result = 0.0;
	for(int i = 0; i < len; i++)
	{

			result += data1[i] * data2[i];
	}
	return log(result);
}



MfccInfo* MfccInit(int sample_rate,int frame_len,int frame_shift,int numc,int nfilt,float pre,int nfft){

	MfccInfo* p=(MfccInfo*)malloc(sizeof(MfccInfo));
	p->melbank.nfft = nfft;
	p->melbank.low  = 0;
	p->melbank.high = sample_rate/2;
	p->melbank.nfilters = nfilt;
	p->dct.dctlen = numc;
	p->dct.datalen = nfilt;
	p->preemh=pre;
	p->frame_len = frame_len;
	p->frame_shift = frame_shift;

	p->pre1 = NULL;
	p->pre2 = NULL;
	p->cur  = NULL;
	p->next1 = NULL;
	p->next2 = NULL;

	int valid_nfft = nfft/2 + 1;
	p->melbank.filter = MallocMatrix( p->melbank.nfilters, valid_nfft);
	MelBank( sample_rate, p->melbank.nfft, p->melbank.low, p->melbank.high, p->melbank.nfilters, p->melbank.filter);//Mel�˲���ϵ��
	p->dct.coeff = MallocMatrix( p->dct.dctlen, p->dct.datalen);
	DctCoeff( p->dct.dctlen, p->dct.datalen, p->dct.coeff);//DCTϵ��
	p->lift_window = (float*)malloc( numc * sizeof(float));
	lift_window(p->lift_window, numc);//����������

	return p;

}


int Mfcc_Frame_std(MfccInfo *p, int  iframe, float *out, int len)//���mfcc������֡���
{
	if(iframe>p->nframes)
		return -1;
	memcpy(p->frame_data,p->data_in+iframe*p->frame_shift,sizeof(float)*p->frame_len);

//	int  nfft = pow(2,ceil(log2(p->melbank.nfft)));
	int  nfft = pow(2,ceil(log(p->melbank.nfft)/log(2)));
	int  valid_nfft = p->melbank.nfft/2 + 1;

	complex* tmp=(complex*)malloc(sizeof(complex)*nfft);
	for(int i=0;i<nfft;i++){
		if(i<p->frame_len){
			tmp[i].real=p->frame_data[i];
		}
		else
			tmp[i].real=0;
	tmp[i].imag=0;
	}
	fft(nfft,tmp);

	for (int j = 0; j < valid_nfft; ++j)
		{

			p->frame_data[j] = pow( tmp[j].real, 2 ) + pow( tmp[j].imag, 2 );//ƽ������ֵ��Ҳ�������׷���ֵ

		}


	for(int i = 0; i < p->dct.dctlen; i++)
	{
		float temp = 0.0;


		if(i==0){
			for (int j=0;j<valid_nfft;j++)
				temp+=p->frame_data[j];

			out[i]=log(temp);

		}
		else{
			for(int j = 0; j < p->melbank.nfilters; j++)
					{
						//DCT�任������

						temp += p->dct.coeff[i][j] * (Product(p->frame_data, p->melbank.filter[j], valid_nfft));
					}
			out[i] = temp * p->lift_window[i];//��������
		}

	}


	return 0;
}

int Mfcc_Frame_diff(MfccInfo *p, int iframe, float *out, int len)//���һ�ײ��
{
		if(iframe==0 ){
			Mfcc_Frame_std(p, iframe , p->pre1, len);
			Mfcc_Frame_std(p, iframe , p->pre2, len);
			Mfcc_Frame_std(p, iframe , p->cur, len);
			Mfcc_Frame_std(p, iframe+1 , p->next1, len);
			Mfcc_Frame_std(p, iframe+2 , p->next2, len);
		}
		else if(iframe!=p->nframes-2 || iframe!=p->nframes-1){

			Mfcc_Frame_std(p, iframe+2 , p->next2, len);
		}

	   int dctlen = p->dct.dctlen;
	   memcpy( out, p->cur, sizeof(float)* dctlen);//mfcc
	   float  factor = 10;
	   for(int i = 0; i < dctlen; i++)
	   {
		   out[i + dctlen] = (2 * p->next2[i] + p->next1[i] - 2*p->pre1[i] - p->pre2[i])/factor ;//һ�ײ��
	   }

	   if(iframe==p->nframes-1){
		   return 0;
	   }

	   	   float *temp = p->pre1;
		   p->pre1 = p->pre2;
		   p->pre2 = p->cur;
		   p->cur  = p->next1;
		   if(iframe!=p->nframes-3){
		   			 p->next1 = p->next2;
		   		 }

		 if(iframe!=p->nframes-2){
			 p->next1 = p->next2;
			 p->next2 = temp;
		 }





return 1;
}

int Mfcc_Data_Pre(MfccInfo *p,float *data,int datalen){
	p->data_len = datalen;
	int nframes = (p->data_len - p->frame_len)/p->frame_shift + 1;
	p->nframes = nframes;
	p->out_nframes = nframes;

	int  buffer_len = p->frame_len > p->melbank.nfft ? p->frame_len:p->melbank.nfft;
	p->frame_data = (float*) malloc( buffer_len * sizeof(float));
	p->data_in = (float*)malloc(sizeof(float)*datalen);

	PreEmphasise(data,datalen,p->data_in,p->preemh);



	p->pre1 = (float*)malloc( p->dct.dctlen*sizeof(float));
	p->pre2 = (float*)malloc( p->dct.dctlen*sizeof(float));
	p->cur  = (float*)malloc( p->dct.dctlen*sizeof(float));
	p->next1 = (float*)malloc( p->dct.dctlen*sizeof(float));
	p->next2 = (float*)malloc( p->dct.dctlen*sizeof(float));





	return 1;
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
//	if(data->next2)
//	{
//		free(data->next2);
//		data->next2 = NULL;
//	}

	if(data->frame_data)
	{
		free(data->frame_data);
		data->frame_data = NULL;
	}

}
