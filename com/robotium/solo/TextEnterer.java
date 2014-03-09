package com.robotium.solo;

import junit.framework.Assert;
import android.app.Instrumentation;
import android.text.InputType;
import android.widget.EditText;


/**
 * 文本内容输出工具类
 * Contains setEditText() to enter text into text fields.
 * 
 * @author Renas Reda, renas.reda@robotium.com
 *
 */

class TextEnterer{
	// Instrument 用于事件发送
	private final Instrumentation inst;
	// 点击操作工具类
	private final Clicker clicker;
	// 弹框操作工具类
	private final DialogUtils dialogUtils;

	/**
	 * 构造函数
	 * Constructs this object.
	 * 
	 * @param inst the {@code Instrumentation} instance
	 * @param clicker the {@code Clicker} instance
	 * @param dialogUtils the {@code DialogUtils} instance
	 * 
	 */

	public TextEnterer(Instrumentation inst, Clicker clicker, DialogUtils dialogUtils) {
		this.inst = inst;
		this.clicker = clicker;
		this.dialogUtils = dialogUtils;
	}

	/**
	 * 设置EditText内容,如设置文本不为空，则在原有内容上追加，空则清空原有内容
	 * editText  需要设置内容的editText
	 * text      设置的文本内容
	 * Sets an {@code EditText} text
	 * 
	 * @param index the index of the {@code EditText} 
	 * @param text the text that should be set
	 */

	public void setEditText(final EditText editText, final String text) {
		// 非空判断
		if(editText != null){
			// 获取原有的文本内容
			final String previousText = editText.getText().toString();
			// 在主线程中执行，避免跨线程报错
			inst.runOnMainSync(new Runnable()
			{
				public void run()
				{
					// 清空原有内容
					editText.setInputType(InputType.TYPE_NULL); 
					// 把焦点切换到editText
					editText.performClick();
					// 隐藏软键盘
					dialogUtils.hideSoftKeyboard(editText, false, false);
					// 如果文本内容为空，则设置为空
					if(text.equals(""))
						editText.setText(text);
					// 如果非空，则在原有内容上追加
					else{
						editText.setText(previousText + text);
						// 移除焦点
						editText.setCursorVisible(false);
					}
				}
			});
		}
	}
	
	/**
	 * 录入文本内容到EditText
	 * editText  需要设置内容的editText
	 * text      录入的文本内容
	 * Types text in an {@code EditText} 
	 * 
	 * @param index the index of the {@code EditText} 
	 * @param text the text that should be typed
	 */

	public void typeText(final EditText editText, final String text){
		// 非空判断
		if(editText != null){
			// 清空原有内容
			inst.runOnMainSync(new Runnable()
			{
				public void run()
				{
					editText.setInputType(InputType.TYPE_NULL);
				}
			});
			// editText成为当前焦点
			clicker.clickOnScreen(editText, false, 0);
			// 隐藏软键盘
			dialogUtils.hideSoftKeyboard(editText, true, true);

			boolean successfull = false;
			int retry = 0;
			// 录入文本内容，如录入失败会重试，最多10次
			while(!successfull && retry < 10) {

				try{
					inst.sendStringSync(text);
					successfull = true;
					// 可能由软键盘导致异常
				}catch(SecurityException e){
					// 隐藏软键盘
					dialogUtils.hideSoftKeyboard(editText, true, true);
					// 增加重试次数
					retry++;
				}
			}
			// 录入失败，抛错
			if(!successfull) {
				Assert.fail("Text can not be typed!");
			}
		}
	}
}
