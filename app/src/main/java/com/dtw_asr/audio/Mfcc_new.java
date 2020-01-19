package com.dtw_asr.audio;


import android.util.Log;

import com.dtw_asr.Constants;
import com.dtw_asr.WaveFileReader;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class Mfcc_new {
	int samplerate=16000;//sample rate
	float winlen	=(float) 0.025;//25ms
	float winstep =(float)  0.01;//10ms
	int numc =13;
	int nfilt = 26;
	int lowfreq=0;
	int highfreq=samplerate/2;
	float preemh=(float) 0.97;
	int nfft =512;//默认512，要求分帧的长度<512;
	float data[];//语音文件
	float melcoef[][];//mel滤波器组系数
	float dctcoef[][];//dct系数
	float frames[][];//分帧
	float mfcc[][];//特征参数
	float stren_win[];//倒谱提升
	static float Pi=(float) Math.PI;
	float amp1 = (float) 10.0;
	float amp2 =(float)  2.0;
	float zrc1 = 10;
	float zrc2 = 15;
	float maxSilence = 8;
	float minLen= 15;
	int start=0;
	int end=0;

	boolean valid=false;
	
	
	private float CHANGE=(float) Math.pow(2,15);
	
	public Mfcc_new() {
		
	}
	
	public Mfcc_new(int samplerate,
									float winlen,
									float winstep,
									int numc,
									int nfilt,
									float preemh,
									int nffft) {
		this.samplerate =samplerate;
		this.winlen=winlen;
		this.winstep=winstep;
		this.numc=numc;
		this.nfilt	= nfilt;
		this.preemh =preemh;
		this.nfft=nffft;
		meilfiltcoef();
		dcttranscoef();
		stren_win_cal();
	}
	
	void readWav(String filename){
		WaveFileReader reader=new WaveFileReader(filename);
		int predata[] = null;
		int sounlen=0;
		if (reader.isSuccess()) {
			predata=reader.getData()[0];
			sounlen=predata.length;
		}
		
		data=new float[sounlen];
		for(int i=0;i<sounlen;i++)
			data[i]=predata[i];///CHANGE;
		return ;
	}

	public short[] readPcm(String filename){
		try {
			File recordFile = new File(filename);
			InputStream inputStream = null;
			inputStream = new FileInputStream(recordFile);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
			DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

			byte[] audioData = new byte[(int)recordFile.length()];

			dataInputStream.read(audioData);
			dataInputStream.close();
			short[]data=new short[(int)(audioData.length/2)];
			for(int i=0;i<audioData.length;i=i+2){
				data[i/2]=(short) ((audioData[i]&0x000000FF) | (((int)audioData[i+1])<<8));
			}
			return  data;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	short[] getData(String filename){
		WaveFileReader reader=new WaveFileReader(filename);
		int predata[] = null;
		int sounlen=0;
		if (reader.isSuccess()) {
			predata=reader.getData()[0];
			sounlen=predata.length;
		}
		short[]data=new short[sounlen];

		for(int i=0;i<sounlen;i++)
			data[i]=(short)predata[i];///CHANGE;
		return data;
	}

	void setData(short []audoData){
//		int len=audoData.length;
//		data=new float[len/2];
//
//		for(int i=0;i<data.length;i++){
//			data[i]=(audoData[i*2]&0x000000ff)|(((int)audoData[i*2+1])<<8);
//		}

		data=new float[audoData.length];
		for(int i=0;i<audoData.length;i++)
			data[i]=(float)audoData[i];
	}
	
	void vad(float []data) {
		float []signal=vadCommon(data);
		float []data1=new float[data.length-1];
		float []data2=new float[data.length-1];
		
		System.arraycopy(signal, 0, data1, 0, data1.length);
		System.arraycopy(signal, 1, data2, 0, data2.length);
		float [][]frame1=enframe(data1);
		float [][]frame2=enframe(data2);
		float[][]frame3=enframe(signal);
		
		float [][]signs=new float[frame1.length][frame1[0].length];
		float [][]diffs=new float[frame1.length][frame1[0].length];
		float []zrc=new float[frame1.length];
		float []amp=new float[frame1.length];
		
		int status=0;
		int count=0;
		int silence=0;

		valid=false;
		
		for(int i=0;i<frame1.length;i++)
			for(int j=0;j<frame1[0].length;j++) {
				signs[i][j]=frame1[i][j]*frame2[i][j]<0?1:0;
				diffs[i][j]=(frame1[i][j]-frame2[i][j]>0.02)?1:0;
			}
		for(int i=0;i<frame1.length;i++) {
			float sum1=0,sum2=0;
			for (int j=0;j<frame1[0].length;j++) {
				sum1+=signs[i][j]*diffs[i][j];
				sum2+= Math.abs(frame3[i][j]);
			}
			zrc[i]=sum1;
			amp[i]=sum2;
		}
		float sum=0;
		for(int i=0;i<amp.length;i++) {

			sum += amp[i];
		}
		sum=(float) (sum*0.7/amp.length);
		amp1=(amp1<sum)?sum:amp1;
		Log.v("MFCC","amp1 dis="+amp1);
		amp2=(amp2<sum/2)?sum/2:amp2;
		
		for (int i=0;i<zrc.length;i++) {
			switch (status) {
			case 0:
			case 1:
				if(amp[i]>amp1) {
					start = (i-count-1)>0?(i-count-1):0;
					status = 2;
					silence = 0;
					count += 1;
					valid=true;
				}
				else if(amp[i] > amp2 && zrc[i] > zrc2) {
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
				break;
			default:
				status=0;
				count=0;
				silence=0;
				break;
			}
		}
		count =count - silence/2;

		Log.v("MFCC","start dis="+start);


		end =start+count-1;
		end =end>data.length?data.length:end;
		Log.v("MFCC","end dis="+end);

		if(start>=end)
			valid=false;

	}
	
	
	float [] vadCommon(float[]data ){
		float []result=new float[data.length];
		float maxData=0;
		for( int i = 0; i < data.length; i++)
		{
		if(maxData< Math.abs(data[i]))
			maxData= Math.abs(data[i]);
		}
		for( int i = 0; i < data.length; i++)
		{
			result[i] = data[i]/maxData;
		}
		return  result;
	} 
	
	void preemphasis(){
		float tmp1=data[0];
		float tmp2;
		for(int i=1;i<data.length;i++) {
			tmp2=data[i];
			data[i]=data[i]-preemh*tmp1;
			tmp1=tmp2;
		}
		return;
	}
	void meilfiltcoef() {
		float  lowmel=hztomel(lowfreq);
		float  highmel=hztomel(highfreq);		
		float []melpoint=new float[nfilt+2];
		
	    melpoint[0]=lowmel;
	    melpoint[melpoint.length-1]=highmel;
	    for(int i=1;i<=nfilt;i++)
	    	melpoint[i]=i*highmel/(nfilt+1);
	    for(int i=0;i<melpoint.length;i++)
	    	melpoint[i]=(int) Math.floor((nfft+1)*(meltohz(melpoint[i])/samplerate));
	    melcoef=new float[nfilt][(int) (Math.floor(nfft/2)+1)];
	    for (int i=0;i<nfilt;i++)
	    	for(int j=0;j<melcoef[0].length;j++)
	    		melcoef[i][j]=0;
	    
	    for(int j=0;j<nfilt;j++) {
	    	for (int i=(int)melpoint[j];i<(int)melpoint[j+1];i++)
	    		melcoef[j][i]=(i-melpoint[j])/(melpoint[j+1]-melpoint[j]);
	    	for(int i=(int)melpoint[j+1];i<(int)melpoint[j+2];i++)
	    		melcoef[j][i]=(melpoint[j+2]-i)/(melpoint[j+2]-melpoint[j+1]);
	    }
	       return ;
	    
	}
	
	float  hztomel(float hz) {
		return (float) (2595* Math.log10(1+hz/700));
	}
	
	float meltohz(float mel) {
		return (float) (700*(Math.pow(10, mel/2595)-1));
	}
	
	float [][] enframe(float []data) {
		int framelen=(int) (winlen*samplerate);
		int framestep=(int)(winstep*samplerate);
		int framenum=0;
		if(data.length<=framelen)
			framenum=1;
		else
			framenum=1+(int)(Math.ceil( (1.0*data.length-framelen)/framestep ));
		int padlen=(framenum-1)*framestep+framelen;
		
		float [][] frame=new float[framenum][framelen];
		float paddata[]=new float[padlen];
		System.arraycopy(data, 0, paddata, 0, data.length);
		for(int  i=data.length;i<padlen;i++)
			paddata[i]=0;
		
		for(int i=0;i<framenum;i++)
			for(int j=0;j<framelen;j++) {
				frame[i][j]=paddata[i*160+j];
			}
		
		return frame;			
	}
	
	
	float [][]mfcccaculate(){
		amp1 =10;
		amp2= 2;
		start=0;
		end=0;
		vad(data);
		if(! valid)
			return null;

		preemphasis();

		int K=2;//导数时间差
		frames=enframe(data);
		Log.v("MFCC", "frames length dis: ."+frames.length);

		mfcc=new float[frames.length][nfilt];
		float mfcc_result[][]=new float[frames.length][nfilt];
		float mfcc_pad[][]=new float[frames.length+2*K][nfilt/2];
	
		for(int i=0;i<mfcc.length;i++) {
			float []pspec=fftcaculate(frames[i]);
			for(int j=0;j<nfilt;j++) {
				float sum=0;
				for(int k=0;k<melcoef[j].length;k++)
					sum+=pspec[k]*melcoef[j][k];
				mfcc[i][j]=(float) Math.log(sum);
			}
			float energy=0;
			for(int j=0;j<pspec.length;j++)
				energy+=pspec[j];


			for(int k=0;k<dctcoef.length;k++) {
				float sum=0;
				for( int n=0;n<nfilt;n++)
					sum +=dctcoef[k][n]*mfcc[i][n];
				if(k==0)
					mfcc_result[i][k]=(float) Math.log(energy);
				else
					mfcc_result[i][k]=sum*stren_win[k];
			}
		}

		for(int i=0;i<mfcc_pad.length;i++) {

			if(i==0 ||i==1||i==2)
				System.arraycopy(mfcc_result[0], 0, mfcc_pad[i], 0, mfcc_result[0].length/2);
			else if(i==mfcc_pad.length-1||i==mfcc_pad.length-2||i==mfcc_pad.length-3)
				System.arraycopy(mfcc_result[mfcc_result.length-1], 0, mfcc_pad[i], 0, mfcc_result[0].length/2);
			else
				System.arraycopy(mfcc_result[i-2], 0, mfcc_pad[i], 0, mfcc_result[0].length/2);
		}



		for(int i=0;i<mfcc_result.length;i++)
			for(int j=0;j<mfcc_result[0].length/2;j++) {
				mfcc_result[i][j+nfilt/2]=(-2*mfcc_pad[i][j]-mfcc_pad[i+1][j]+mfcc_pad[i+3][j]+2*mfcc_pad[i+4][j])/(2*5);
			}


		float [][]mfcc_part=new float[end-start+1][numc*2];

		for(int i = start; i< Math.min(end+1,mfcc_result.length-1); i++)
			System.arraycopy(mfcc_result[i], 0, mfcc_part[i-start], 0, mfcc_result[i].length);

		return mfcc_part;
	}
	
	void stren_win_cal() {
		stren_win=new float[numc];
		int L=nfilt-2;

		for(int i=0;i<numc;i++) {
			stren_win[i]=(float) (1+L/2* Math.sin(Pi*i/L));
		}
		
		
	}
	
	
	
	void dcttranscoef() {
		dctcoef=new float[numc][nfilt];
		float coef1=(float) ( 2* Math.sqrt(1.0/(4*nfilt)));
		float coef2=(float) (2* Math.sqrt(1.0/(2*nfilt)));
		
		for (int k=0;k<numc;k++)
			for (int n=0;n<nfilt;n++)
				if(k==0)
					dctcoef[k][n]=(float) coef1;
				else
					dctcoef[k][n]=(float) (coef2* Math.cos((n+0.5)*k*Pi/nfilt));
	}
	
	
	float []fftcaculate(float[]data){
		float result[]=new float[nfft];
		int  len =data.length;
		System.arraycopy(data, 0, result, 0, len);
		if(len<nfft) {
			for(int i=len;i<nfft;i++)
				result[i]=0;
		}
		complex []x=new complex[nfft];	
		
		for(int j=0; j<nfft;j++) 
			x[j]=new complex(result[j], 0);
		float []tmp=fft(x);
		
		return tmp;
	}
	
	float[]fft(complex []x) {//欧拉公式
		int n=x.length;

		int []pos=new int[n];
		for(int i=0;i<n/2;i+=2) {
			pos[i]=bitreverse(i, n);
			pos[i+1]=n/2+pos[i];
			pos[n/2+i]=pos[i]+1;
			pos[n/2+i+1]=n/2+pos[i]+1;
		}
			
		for(int i=0;i<n;i++)
	        if(i<pos[i])
	            {
	        	complex t=x[i];
	        	x[i]=x[pos[i]];
	        	x[pos[i]]=t;
	            }
		for(int i=2,mid=1;i<=n;i<<=1,mid<<=1){
	        complex wm=new complex((float) Math.cos(2.0*Pi/i),(float) Math.sin(2*Pi/i));
	        for(int j=0;j<n;j+=i){
	        	complex w=new complex(1,0);
	            for(int k=j;k<j+mid;k++,w=w.multiple(wm)){
	                complex l=x[k];
	                complex r=w.multiple(x[k+mid]);
	                x[k]=l.plus(r);
	                x[k+mid]=l.sub(r);
	            }
	        }
    }
	
	
	float[]result=new float[n/2+1];
	for(int i=0;i<result.length;i++) {
		float sum =(float) Math.pow(x[i].real,2)+(float) Math.pow(x[i].img, 2);
		result[i]=sum/512;
	}

		return result;
	}
	
	int bitreverse(int j,int n) {
		int i,p;
		p=0;
		int wide=(int)(Math.log(n)/ Math.log(2));
		for(i=0;i<wide;i++)
			if( (j&(1<<i))>0) {
				int  shft=wide-i-1;
				p+=1<<shft;
				
			}
		return p;
	}
	
	
	complex []dft(complex x[]){
		int n=x.length;
		if (n==1)
			return x;
		complex []result =new complex[n];
		for (int i=0;i<n;i++) {
			result[i]=new complex(0, 0);
			for (int k=0;k<n;k++) {
				float p=(float) (-2*k* Math.PI/n);
				complex m= new complex((float) Math.cos(p), (float) Math.sin(p));
				result[i]=result[i].plus(x[k].multiple(m));
			}
		}
		
		return result;
	}
	
	
	class complex{
		float real;
		float img;
		
		public complex(float r,float i) {
			real=r;
			img=i;
		}
		complex plus(complex x) {
			complex c=new complex(real+x.real,img+x.img);
			return c;
		}
		complex sub(complex x) {
			complex c=new complex(real-x.real, img-x.img);
			return c;
		}
		
		complex multiple(complex x) {
			complex c=new complex(real*x.real-img*x.img, real*x.img+img*x.real);
			return c;
		}
		
	}
	
}
