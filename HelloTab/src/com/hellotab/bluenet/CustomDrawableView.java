package com.hellotab.bluenet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;



public class CustomDrawableView extends View {

	Queue<Integer> queue = new LinkedList<Integer>();
	Queue<Integer> q = new LinkedList<Integer>();
	BluetoothAdapter myAdaptor;

	Paint paint = new Paint();
	Paint paint1 = new Paint();
	Paint paint2 = new Paint();



	public CustomDrawableView(Context context) {
		super(context);
		paint.setColor(Color.GRAY);
		paint.setStrokeWidth(15);
		paint.setTextSize(20);
		
		paint1.setColor(Color.YELLOW);
		paint1.setTextSize(20);
		
		
		
		paint2.setColor(Color.WHITE);
		//paint2.setStrokeWidth(4);

	}	


	public static int[][] adjMatrix1;

	@Override
	public void onDraw(Canvas canvas) {
		CustomDrawableView.adjMatrix1 = HelloTabActivity.netMap;
		Set<String> keys = HelloTabActivity.netMap_List.keySet();
		Iterator<String> keyList = keys.iterator();
		myAdaptor = BluetoothAdapter.getDefaultAdapter();
		ThirdActivity.macs[0] = MyAppActivity.mServerAddress;
		ThirdActivity.macs[1] = myAdaptor.getAddress();
		int count = 2;
		while(keyList.hasNext()){
			String key = keyList.next();
			if(!key.equals(ThirdActivity.macs[0]) && !key.equals(ThirdActivity.macs[1]))
			{
				ThirdActivity.macs[count] = key;
				++count;
			}
		}
		for(int i =0; i < 5; ++i)
			if(ThirdActivity.macs[i] == null)
				ThirdActivity.macs[i] = "";
		
		
		
		
		
		int node = 1;
		int x = 480;
		int y = 40;

		int[][] adjMatrix = adjMatrix1;
		queue.add(node);
		canvas.drawCircle(x/2, y, 20, paint1);
		canvas.drawText(ThirdActivity.macs[node], x/2 -80 , y + 10, paint);

		while(!queue.isEmpty())
		{
			int c = 0;
			node = queue.remove();


			for (int i=0; i<5; i++)
			{
				if (adjMatrix[node][i] == 1)
				{
					c = c + 1;
					queue.add(i);
					adjMatrix[node][i] = 0;
					adjMatrix[i][node] = 0;
				}
			}

			if(c==1){
				canvas.drawLine(x/2 , y + 100 , x/2 , y, paint2);
				canvas.drawCircle(x/2 , y + 100, 20, paint);
				canvas.drawText(ThirdActivity.macs[(Integer)queue.peek()], x/2 - 80 , y + 110, paint1);
				y = y +100;
			}


			else if (c==2){
				Object intArray[] = queue.toArray();
				for(int i =0; i<2; i++){
					canvas.drawLine(x/3 * (i+1), y + 100 , x/2 , y, paint2);
					canvas.drawCircle(x/3 *(i+1), y + 100, 20, paint);
					canvas.drawText(ThirdActivity.macs[(Integer) intArray[i]], (x/3 *(i+1))- 80 , y + 110, paint1);

					//y = y + 100;

					int d = 0;
					node = queue.remove();
					for (int k=0; k<5; k++)
					{
						if (adjMatrix[node][k] == 1)
						{
							d = d + 1;
							q.add(k);
							adjMatrix[node][k] = 0;
							adjMatrix[k][node] = 0;
						}
					}	 


					if(d==1){
						Object intArray1[] = q.toArray();
						canvas.drawLine(x/3 *(i+1), y +200 , x/3 *(i+1), y + 100, paint2);
						canvas.drawCircle(x/3 *(i+1), y + 200, 20, paint);
						canvas.drawText(ThirdActivity.macs[(Integer)intArray1[0]], (x/3 *(i+1)) - 80 , y + 210, paint1);
						//y = y +100;
						int e = 0;
						node = q.remove();
						for (int j=0; j<5; j++)
						{
							if (adjMatrix[node][j] == 1)
							{
								e = e + 1;
								q.add(j);
								adjMatrix[node][j] = 0;
								adjMatrix[j][node] = 0;
							}
						}	
						if(e==1){
							Object intArray11[] = q.toArray();
							canvas.drawLine(x/3 *(i+1) , y + 300 , x/3 *(i+1) , y + 200, paint2);
							canvas.drawCircle(x/3 *(i+1) , y + 300, 20, paint);
							canvas.drawText(ThirdActivity.macs[(Integer)intArray11[0]], (x/3 *(i+1)) - 80 , y + 310, paint1);
						}

					}
					else if (d==2){
						Object intArray1[] = q.toArray();
						for(int i1 =0; i1<2; i1++){
							canvas.drawLine(x/3 * (i1+1), y + 200 , x/3 *(i+1), y + 100, paint2);
							canvas.drawCircle(x/3 *(i1+1), y + 200, 20, paint);
							canvas.drawText(ThirdActivity.macs[(Integer)intArray1[i1]], (x/3 *(i1+1))- 80 , y + 210, paint1);
						}
						int e = 0;
						node = q.remove();
						for (int i1=0; i1<5; i1++)
						{
							if (adjMatrix[node][i1] == 1)
							{
								e = e + 1;
								q.add(i1);
								adjMatrix[node][i1] = 0;
								adjMatrix[i1][node] = 0;
							}
						}	 
						if(e==1){
							canvas.drawLine(x/3 *2, y + 100 , x/3 *2, y, paint2);
							canvas.drawCircle(x/3 *2, y + 100, 20, paint);
							canvas.drawText(ThirdActivity.macs[(Integer)q.peek()], (x/3 *2) - 80 , y + 110, paint1);
							y = y +100;
						}
						else if (e==2){
							Object intArray11[] = q.toArray();
							for(int i1 =0; i1<2; i1++){
								canvas.drawLine(x/3 * (i1+1), y + 100 , x/3 *2, y, paint2);
								canvas.drawCircle(x/3 *(i1+1), y + 100, 20, paint);
								canvas.drawText(ThirdActivity.macs[(Integer)intArray11[i1]], (x/3 *(i1+1))- 80 , y + 110, paint1);
							}
						}
					}
				}
			}


			else if (c == 3){
				Object intArray1[] = queue.toArray();
				for(int i =0; i<3; i++){
					canvas.drawLine(x/4 * (i+1), y + 100 , x/2 , y, paint2);
					canvas.drawCircle(x/4 *(i+1), y + 100, 20, paint);
					canvas.drawText(ThirdActivity.macs[(Integer)intArray1[i]], (x/4 *(i+1))- 80 , y + 110, paint1);

					//y = y +100;


					int f=0;
					node = queue.remove();
					for (int j=0; j<5; j++)
					{
						if (adjMatrix[node][j] == 1)
						{
							f = f + 1;
							q.add(j);
							adjMatrix[node][j] = 0;
							adjMatrix[j][node] = 0;
						}
					}
					if(f==1)
					{	
						//int z = y + 100;
						canvas.drawLine(x/2 , y +200, x/4 * (i+1) , y + 100, paint2);
						canvas.drawCircle(x/2 , y + 200 , 20, paint);
						canvas.drawText(ThirdActivity.macs[(Integer)q.peek()], x/2 - 80 , y + 210, paint1);
					}
				}

			}
			else if (c == 4){
				for(int i =1; i<5; i++){
					canvas.drawLine(x/5 * i, y + 100 , x/2 , y, paint2);
					canvas.drawCircle(x/5 *i, y + 100, 20, paint);
					canvas.drawText(ThirdActivity.macs[(Integer)queue.remove()], (x/5 *i)- 80 , y + 110, paint1);
				}
			}				
		}
		queue.clear();
		q.clear();
	}
}





