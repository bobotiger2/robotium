package com.robotium.solo;


import junit.framework.Assert;
import android.app.Instrumentation;
import android.view.KeyEvent;

/**
 * 用于发送各类按键事件
 * Contains send key event methods. Examples are:
 * sendKeyCode(), goBack()
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class Sender {
	// Instrument,用于发送各类事件
	private final Instrumentation inst;
	// 等待工具类
	private final Sleeper sleeper;

	/**
	 * 构造函数
	 * Constructs this object.
	 * 
	 * @param inst the {@code Instrumentation} instance
	 * @param sleeper the {@code Sleeper} instance
	 */

	Sender(Instrumentation inst, Sleeper sleeper) {
		this.inst = inst;
		this.sleeper = sleeper;
	}

	/**
	 * 发送各类按键事件.
	 * Tells Robotium to send a key code: Right, Left, Up, Down, Enter or other.
	 * 
	 * @param keycode the key code to be sent. Use {@link KeyEvent#KEYCODE_ENTER}, {@link KeyEvent#KEYCODE_MENU}, {@link KeyEvent#KEYCODE_DEL}, {@link KeyEvent#KEYCODE_DPAD_RIGHT} and so on
	 */

	public void sendKeyCode(int keycode)
	{
		sleeper.sleep();
		try{
			inst.sendCharacterSync(keycode);
			// 捕获可能遇到的权限问题
		}catch(SecurityException e){
			// 日志提醒，该操作无权和相关错误日志
			Assert.fail("Can not complete action! ("+(e != null ? e.getClass().getName()+": "+e.getMessage() : "null")+")");
		}
	}

	/**
	 * 发送返回事件，即点击一下返回按钮
	 * Simulates pressing the hardware back key.
	 */

	public void goBack() {
		// 等待500ms
		sleeper.sleep();
		try {
			// 发送返回事件
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
			// 等待500ms
			sleeper.sleep();
		} catch (Throwable ignored) {}
	}
}
