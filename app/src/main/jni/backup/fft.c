/*
 * fft.c
 *
 *  Created on: 2019��12��27��
 *      Author: Administrator
 */
#include "math.h"
#include "fft.h"
//����0.0001����

void conjugate_complex(int n,complex in[],complex out[])
{
  int i = 0;
  for(i=0;i<n;i++)
  {
    out[i].imag = -in[i].imag;
    out[i].real = in[i].real;
  }
}

void c_abs(complex f[],float out[],int n)
{
  int i = 0;
  float t;
  for(i=0;i<n;i++)
  {
    t = f[i].real * f[i].real + f[i].imag * f[i].imag;
    out[i] = sqrt(t);
  }
}


void c_plus(complex a,complex b,complex *c)
{
  c->real = a.real + b.real;
  c->imag = a.imag + b.imag;
}

void c_sub(complex a,complex b,complex *c)
{
  c->real = a.real - b.real;
  c->imag = a.imag - b.imag;
}

void c_mul(complex a,complex b,complex *c)
{
  c->real = a.real * b.real - a.imag * b.imag;
  c->imag = a.real * b.imag + a.imag * b.real;
}

void c_div(complex a,complex b,complex *c)
{
  c->real = (a.real * b.real + a.imag * b.imag)/(b.real * b.real +b.imag * b.imag);
  c->imag = (a.imag * b.real - a.real * b.imag)/(b.real * b.real +b.imag * b.imag);
}

#define SWAP(a,b)  tempr=(a);(a)=(b);(b)=tempr

void Wn_i(int n,int i,complex *Wn,char flag)
{
  Wn->real = cos(2*PI*i/n);
  if(flag == 1)
  Wn->imag = -sin(2*PI*i/n);
  else if(flag == 0)
  Wn->imag = -sin(2*PI*i/n);
}

//����Ҷ�仯
void fft(int N,complex f[])
{
  complex t,wn;//�м����
  int i,j,k,m,n,l,r,M;
  int la,lb,lc;
  /*----����ֽ�ļ���M=log2(N)----*/
  for(i=N,M=1;(i=i/2)!=1;M++);
  /*----���յ�λ����������ԭ�ź�----*/
  for(i=1,j=N/2;i<=N-2;i++)
  {
    if(i<j)
    {
      t=f[j];
      f[j]=f[i];
      f[i]=t;
    }
    k=N/2;
    while(k<=j)
    {
      j=j-k;
      k=k/2;
    }
    j=j+k;
  }

  /*----FFT�㷨----*/
  for(m=1;m<=M;m++)
  {
    la=pow(2,m); //la=2^m������m��ÿ�����������ڵ���
    lb=la/2;    //lb������m��ÿ�������������ε�Ԫ��
                 //ͬʱ��Ҳ��ʾÿ�����ε�Ԫ���½ڵ�֮��ľ���
    /*----��������----*/
    for(l=1;l<=lb;l++)
    {
      r=(l-1)*pow(2,M-m);
      for(n=l-1;n<N-1;n=n+la) //����ÿ�����飬��������ΪN/la
      {
        lc=n+lb;  //n,lc�ֱ����һ�����ε�Ԫ���ϡ��½ڵ���
        Wn_i(N,r,&wn,1);//wn=Wnr
        c_mul(f[lc],wn,&t);//t = f[lc] * wn��������
        c_sub(f[n],t,&(f[lc]));//f[lc] = f[n] - f[lc] * Wnr
        c_plus(f[n],t,&(f[n]));//f[n] = f[n] + f[lc] * Wnr
      }
    }
  }
}

//����Ҷ��任
void ifft(int N,complex f[])
{
  int i=0;
  conjugate_complex(N,f,f);
  fft(N,f);
  conjugate_complex(N,f,f);
  for(i=0;i<N;i++)
  {
    f[i].imag = (f[i].imag)/N;
    f[i].real = (f[i].real)/N;
  }
}
