package com.robotium.solo;

import android.app.Instrumentation;
import android.graphics.PointF;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
//  屏幕方向操作
//  API 要求14
class Rotator
{
	// Instrument 用于发送事件,C风格代码，带个 _
	private final Instrumentation _instrument;
	// 10ms
	private static final int EVENT_TIME_INTERVAL_MS = 10;
	// 放大
	public static final int LARGE = 0;
	// 缩小
	public static final int SMALL = 1;
	// 构造函数
	public Rotator(Instrumentation inst)
	{
		this._instrument = inst;
	}
	// 发送屏幕转动模拟用户画圈
	// size    0 每次转动3.6角度,1每次转动36角度
	// center1   第一个手指的点
	// center2   第二个手指的点
	public void generateRotateGesture(int size, PointF center1, PointF center2)
	{
		double incrementFactor = 0;
		// 获取第一个坐标点的X
		float startX1 = center1.x;
		// 获取第一个坐标点的Y
		float startY1 = center1.y;
		// 获取第二个坐标点的X
		float startX2 = center2.x;
		// 获取第二个坐标点的Y
		float startY2 = center2.y;
		// 获取当前时间值，
		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis();

		// pointer 1
		float x1 = startX1;
		float y1 = startY1;

		// pointer 2
		float x2 = startX2;
		float y2 = startY2;
		// 构造坐标点集合
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
		// 构造坐标点集合,手指按住屏幕
		PointerProperties[] pointerProperties = new PointerProperties[2];
		PointerProperties pp1 = new PointerProperties();
		PointerProperties pp2 = new PointerProperties();
		pp1.id = 0;
		pp1.toolType = MotionEvent.TOOL_TYPE_FINGER;
		pp2.id = 1;
		pp2.toolType = MotionEvent.TOOL_TYPE_FINGER;
		pointerProperties[0] = pp1;
		pointerProperties[1] = pp2;
		// 发送转动事件
		MotionEvent event;
		// send the initial touches
		event = MotionEvent.obtain(downTime, eventTime,
				MotionEvent.ACTION_DOWN, 1, pointerProperties, pointerCoords,
				0, 0, // metaState, buttonState
				1, // x precision
				1, // y precision
				0, 0, // deviceId, edgeFlags
				InputDevice.SOURCE_TOUCHSCREEN, 0); // source, flags
		_instrument.sendPointerSync(event);

		event = MotionEvent.obtain(downTime, eventTime,
				MotionEvent.ACTION_POINTER_DOWN
				+ (pp2.id << MotionEvent.ACTION_POINTER_INDEX_SHIFT),
				2, pointerProperties, pointerCoords, 0, 0, 1, 1, 0, 0,
				InputDevice.SOURCE_TOUCHSCREEN, 0);
		_instrument.sendPointerSync(event);
		// 按照设定值，指定转动速率
		switch(size)
		{
		case 0:
		{
			incrementFactor = 0.01;
		}
		break;
		case 1:
		{
			incrementFactor = 0.1;
		}
		break;
		}
		// 发送手指滑动事件
		for (double i = 0; i < Math.PI; i += incrementFactor)
		{
			eventTime += EVENT_TIME_INTERVAL_MS;
			pointerCoords[0].x += Math.cos(i);
			pointerCoords[0].y += Math.sin(i);
			pointerCoords[1].x += Math.cos(i + Math.PI);
			pointerCoords[1].y += Math.sin(i + Math.PI);

			event = MotionEvent.obtain(downTime, eventTime,
					MotionEvent.ACTION_MOVE, 2, pointerProperties,
					pointerCoords, 0, 0, 1, 1, 0, 0,
					InputDevice.SOURCE_TOUCHSCREEN, 0);
			_instrument.sendPointerSync(event);
		}
		// 松开手指
		// and remove them fingers from the screen
		eventTime += EVENT_TIME_INTERVAL_MS;
		event = MotionEvent.obtain(downTime, eventTime,
				MotionEvent.ACTION_POINTER_UP
				+ (pp2.id << MotionEvent.ACTION_POINTER_INDEX_SHIFT),
				2, pointerProperties, pointerCoords, 0, 0, 1, 1, 0, 0,
				InputDevice.SOURCE_TOUCHSCREEN, 0);
		_instrument.sendPointerSync(event);

		eventTime += EVENT_TIME_INTERVAL_MS;
		event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP,
				1, pointerProperties, pointerCoords, 0, 0, 1, 1, 0, 0,
				InputDevice.SOURCE_TOUCHSCREEN, 0);
		_instrument.sendPointerSync(event);
	}
}
