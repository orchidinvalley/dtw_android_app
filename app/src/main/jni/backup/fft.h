/*
 * fft.h
 *
 *  Created on: 2019��12��27��
 *      Author: Administrator
 */

#ifndef __FFT_H__
#define __FFT_H__

typedef struct complex //��������
{
  float real;		//ʵ��
  float imag;		//�鲿
}complex;

#define  PI   3.1415926


///////////////////////////////////////////
void conjugate_complex(int n,complex in[],complex out[]);
void c_plus(complex a,complex b,complex *c);//������
void c_mul(complex a,complex b,complex *c) ;//������
void c_sub(complex a,complex b,complex *c);	//��������
void c_div(complex a,complex b,complex *c);	//��������
void fft(int N,complex f[]);//����Ҷ�任 ���Ҳ��������f��
void ifft(int N,complex f[]); // ����Ҷ��任
void c_abs(complex f[],float out[],int n);//��������ȡģ
////////////////////////////////////////////
#endif
