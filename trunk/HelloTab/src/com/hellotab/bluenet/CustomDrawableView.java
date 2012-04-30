package com.hellotab.bluenet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class CustomDrawableView extends View {

	int x = 480;
	int y = 700;
	int width = 50;
	int height = 50;
	int a = 100;
	int b = 100;
	Paint paint = new Paint();
	
	int[][] adjMatrix = { {0, 1, 1, 0, 0, 0, 0},
            {1, 0, 0, 1, 1, 1, 0},
            {1, 0, 0, 0, 0, 0, 1},
            {0, 1, 0, 0, 0, 0, 1},
            {0, 1, 0, 0, 0, 0, 1},
            {0, 1, 0, 0, 0, 0 ,0},
            {0, 0, 1, 1, 1, 0, 0}};

	int node = 1;
	int c = 0;
	{
	
	for (int i=0; i<7; i++)
		{
		if (adjMatrix[node][i] == 1)
			{
			 c = c +1;
			}
	
		}
	}
	
	
	public CustomDrawableView(Context context) {
		super(context);
		paint.setColor(Color.GREEN);
		paint.setStrokeWidth(10);

	}	

	public void onDraw(Canvas canvas) {

		canvas.drawCircle(x/2, y/4, 20, paint);
		
		for (int i = 1; i<=c; i++)
		{
			canvas.drawCircle((x/(c+1))*i , y/2, 20, paint);
			canvas.drawLine((x/(c+1))*i , y/2 , x/2 , y/4, paint);
		}
//		canvas.drawLine(x , y , a , b, paint);
//		canvas.drawCircle(x, y, 20, paint);
//		canvas.drawCircle(a, b, 20, paint);

	}
	

	
}
