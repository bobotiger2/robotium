package com.robotium.solo;

import com.robotium.solo.Solo.Config;




/**
 * 超时设置类
 * Used to get and set the default timeout lengths of the various Solo methods. 
 * 
 * @author Renas Reda, renas.reda@robotium.com
 *
 */

public class Timeout{
	// 长超时
	private static int largeTimeout;
	// 短超时
	private static int smallTimeout;

	
	/**
	 * 设置长超时时间，未设置则使用Config设置的默认超时20s
	 * Sets the default timeout length of the waitFor methods. Will fall back to the default values set by {@link Config}.
	 * <br><br>
	 * Timeout can also be set through adb shell (requires root access):
	 * <br><br>
	 * 'adb shell setprop solo_large_timeout milliseconds' 
	 * 
	 * @param milliseconds the default timeout length of the waitFor methods
	 * 
	 */
	public static void setLargeTimeout(int milliseconds){
		largeTimeout = milliseconds;
	}

	/**
	 * 设置短超时时间，未设置则使用Config设置的默认超时10s
	 * Sets the default timeout length of the get, is, set, assert, enter, type and click methods. Will fall back to the default values set by {@link Config}.
	 * <br><br>
	 * Timeout can also be set through adb shell (requires root access):
	 * <br><br>
	 * 'adb shell setprop solo_small_timeout milliseconds' 
	 * 
	 * @param milliseconds the default timeout length of the get, is, set, assert, enter and click methods
	 * 
	 */
	public static void setSmallTimeout(int milliseconds){
		smallTimeout = milliseconds;
	}

	/**
	 * 获取当前设置的长超时时间
	 * Gets the default timeout length of the waitFor methods. 
	 * 
	 * @return the timeout length in milliseconds
	 * 
	 */
	public static int getLargeTimeout(){
		return largeTimeout;
	}

	/**
	 * 获取当前设置的短超时时间
	 * Gets the default timeout length of the get, is, set, assert, enter, type and click methods. 
	 * 
	 * @return the timeout length in milliseconds
	 * 
	 */
	public static int getSmallTimeout(){
		return smallTimeout;
	}
}
