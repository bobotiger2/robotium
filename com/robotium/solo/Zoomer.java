package com.robotium.solo;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerProperties;
import android.view.MotionEvent.PointerCoords;
import android.graphics.PointF;

// 放大手势操作工具类
class Zoomer {
	// Instrument 用于发送事件
	private final Instrumentation _instrument;
	// 手势持续时间1s
	public static final int GESTURE_DURATION_MS = 1000;
    // 事件间隔10ms
	public static final int EVENT_TIME_INTERVAL_MS = 10;
	// 构造函数
	public Zoomer(Instrumentation inst)
	{
		this._instrument = inst;
	}
	// 发送放大动作
	// startPoint1   开始坐标点1
	// startPoint2   开始坐标点2
	// endPoint1         结束坐标点1
	// endPoint2         结束坐标点2
	public void generateZoomGesture(PointF startPoint1, PointF startPoint2, PointF endPoint1, PointF endPoint2) 
	{
		 // 初始化时间变量
		 long downTime = SystemClock.uptimeMillis();
         long eventTime = SystemClock.uptimeMillis();
         // 获取相关坐标值
         float startX1 = startPoint1.x;
         float startY1 = startPoint1.y;
         float startX2 = startPoint2.x;
         float startY2 = startPoint2.y;

         float endX1 = endPoint1.x;
         float endY1 = endPoint1.y;
         float endX2 = endPoint2.x;
         float endY2 = endPoint2.y;

         //pointer 1
         float x1 = startX1;
         float y1 = startY1;

         //pointer 2
         float x2 = startX2;
         float y2 = startY2; 
         // 构造相关坐标点集合
         PointerCoords[] pointerCoords = new PointerCoords[2];
         PointerCoords pc1 = new PointerCoords();
         PointerCoords pc2 = new PointerCoords();
         pc1.x = x1;
         pc1.y = y1;
         pc1.pressure = 1;
         pc1.size = 1;
         pc2.x = x2;
         pc2.y = y2;
         pc2.pressure = 1;
         pc2.size = 1;
         pointerCoords[0] = pc1;
         pointerCoords[1] = pc2;

         PointerProperties[] pointerProperties = new PointerProperties[2];
         PointerProperties pp1 = new PointerProperties();
         PointerProperties pp2 = new PointerProperties();
         pp1.id = 0;
         pp1.toolType = MotionEvent.TOOL_TYPE_FINGER;
         pp2.id = 1;
         pp2.toolType = MotionEvent.TOOL_TYPE_FINGER;
         pointerProperties[0] = pp1;
         pointerProperties[1] = pp2;
         // 开始发送按下事件
         MotionEvent event;
         // send the initial touches
         event = MotionEvent.obtain( downTime,
                                     eventTime,
                                     MotionEvent.ACTION_DOWN,
                                     1,
                                     pointerProperties,
                                     pointerCoords,
                                     0, 0, // metaState, buttonState
                                     1, // x precision
                                     1, // y precision
                                     0, 0, 0, 0 ); // deviceId, edgeFlags, source, flags
         _instrument.sendPointerSync(event);

         event = MotionEvent.obtain( downTime,
                                     eventTime,
                                     MotionEvent.ACTION_POINTER_DOWN + (pp2.id << MotionEvent.ACTION_POINTER_INDEX_SHIFT),
                                     2,
                                     pointerProperties,
                                     pointerCoords,
                                     0, 0,
                                     1,
                                     1,
                                     0, 0, 0, 0 );
         _instrument.sendPointerSync(event);
         // 计算动作步骤 100步
         int numMoves = GESTURE_DURATION_MS / EVENT_TIME_INTERVAL_MS;
         // 计算每步移动的坐标值
         float stepX1 = (endX1 - startX1) / numMoves;
         float stepY1 = (endY1 - startY1) / numMoves;
         float stepX2 = (endX2 - startX2) / numMoves;
         float stepY2 = (endY2 - startY2) / numMoves;
         // 发送构造好的事件
         // send the zoom
         for (int i = 0; i < numMoves; i++)
         {
             eventTime += EVENT_TIME_INTERVAL_MS;
             pointerCoords[0].x += stepX1;
             pointerCoords[0].y += stepY1;
             pointerCoords[1].x += stepX2;
             pointerCoords[1].y += stepY2;

             event = MotionEvent.obtain( downTime,
                                         eventTime,
                                         MotionEvent.ACTION_MOVE,
                                         2,
                                         pointerProperties,
                                         pointerCoords,
                                         0, 0,
                                         1,
                                         1,
                                         0, 0, 0, 0 );
             _instrument.sendPointerSync(event);
         }
	}
}
