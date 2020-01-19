package com.dtw_asr.audio;

import com.dtw_asr.Constants;

import java.util.ArrayList;

public class Dtw_check {
	int nwm=10000;
	public ArrayList<int[]>pathList=new ArrayList<int[]>();
	
	public Dtw_check( int nwm) {
		this.nwm=nwm;
	}
	
	  float dtw_detect(float[][]t,float [][]r) {
          pathList.clear();

		if(t==null || r==null)
			return 10000000;
		int m=t.length;
		int n=r.length;
		if(m==0||n==0)
			return  10000000;
		if((2*m-n)<3 ||(2*n-m)<2)
			return 10000000;
		
		float [][]cost=new float[m][n];
		cost[0][0]=distance_caculate(t[0], r[0]);
		
		for (int i=1;i<m;i++)
			cost[i][0]=cost[i-1][0]+distance_caculate(t[i], r[0]);
		
		for (int j=1;j<n;j++)
			cost[0][j]=cost[0][j-1]+distance_caculate(t[0], r[j]);
		
		for(int i=1;i<m;i++)
			for (int j=1;j<n;j++) {
				float minValue=0;
				minValue=cost[i-1][j-1]<cost[i][j-1]?cost[i-1][j-1]:cost[i][j-1];
				minValue=minValue<cost[i-1][j]?minValue:cost[i-1][j];
//                if(cost[i-1][j-1]<cost[i][j-1]){
//                    minValue=cost[i-1][j-1];
//                    int []item={i-1,j-1};
//                    pathList.add(item);
//                }
//                else{
//                    minValue=cost[i][j-1];
//					int []item={i,j-1};
//					pathList.add(item);
//                }
//                if(minValue>cost[i-1][j]){
//                    minValue=cost[i-1][j];
//					int []item={i-1,j};
//					pathList.add(item);
//                }

				cost[i][j]=minValue+distance_caculate(t[i], r[j]);				
			}
		int i=m-1;
		int j=n-1;
		int[] item={i,j};
		pathList.add(item);
		while(i>0 && j>0){
			if(cost[i-1][j-1]<cost[i][j-1] && cost[i-1][j-1]<cost[i-1][j]) {
				int []item1={i-1,j-1};
				pathList.add(item1);
				i=i-1;
				j=j-1;
			}
			else if(cost[i][j-1]<cost[i-1][j-1] && cost[i][j-1]<cost[i-1][j]) {
				int []item1={i,j-1};
				pathList.add(item1);
				j=j-1;
			}else {
				int []item1={i-1,j};
				pathList.add(item1);
				i=i-1;
			}

		}

				
		return cost[m-1][n-1];
		
	}
	float distance_caculate(float []x,float y[]) {
		///x,y的长度必须一致
		float distance=0;
		for(int i=0;i<x.length;i++)
			distance+= Math.pow(x[i]-y[i],2);
		
		return distance;
	}
	

	

}
