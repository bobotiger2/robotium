package com.robotium.solo;

import android.content.Context;
import android.widget.TextView;

/**
 * Robotium定制化TextView,可以用于WebElement对象的表示
 * Used to create a TextView object that is based on a web element. Contains the web element text and location.  
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class RobotiumTextView extends TextView {
	// 屏幕中对应的X坐标
	private int locationX = 0;
	// 屏幕中对应的Y坐标
	private int locationY = 0;

	/**
	 * 构造函数
	 * Constructs this object
	 * 
	 * @param context the given context
	 */
	
	public RobotiumTextView(Context context){
		super(context);
	}
	
	/**
	 * 构造函数
	 * Constructs this object 
	 * 
	 * @param context the given context
	 * @param text the given text to be set
	 */
	
	public RobotiumTextView(Context context, String text, int locationX, int locationY) {
		super(context);
		this.setText(text);
		setLocationX(locationX);
		setLocationY(locationY);
	}

	/**
	 * 获取控件对应的屏幕坐标
	 * Returns the location on screen of the {@code TextView} that is based on a web element
	 */
	
	@Override
	public void getLocationOnScreen(int[] location) {

		location[0] = locationX;
		location[1] = locationY;
	}
	
	/**
	 * 设置对应屏幕的X坐标
	 * Sets the X location of the TextView
	 * 
	 * @param locationX the X location of the {@code TextView}
	 */
	
	public void setLocationX(int locationX){
		this.locationX = locationX;
	}
	
	
	/**
	 * 设置对应屏幕的Y坐标
	 * Sets the Y location
	 * 
	 * @param locationY the Y location of the {@code TextView}
	 */
	
	public void setLocationY(int locationY){
		this.locationY = locationY;
	}

}
