package com.robotium.solo;

import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.SlidingDrawer;
import android.widget.TimePicker;


/**
 * 设置类控件操作工具类
 * Contains set methods. Examples are setDatePicker(),
 * setTimePicker().
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class Setter{
	// 关闭为0
	private final int CLOSED = 0;
	// 打开为1
	private final int OPENED = 1;
	// activity操作工具类
	private final ActivityUtils activityUtils;

	/**
	 * 构造函数
	 * Constructs this object.
	 *
	 * @param activityUtils the {@code ActivityUtils} instance
	 */

	public Setter(ActivityUtils activityUtils) {
		this.activityUtils = activityUtils;
	}


	/**
	 * 设置控件日期
	 * datePicker   需要设置日期的控件
	 * year         年
	 * monthOfYear  月
	 * dayOfMonth   日
	 * Sets the date in a given {@link DatePicker}.
	 *
	 * @param datePicker the {@code DatePicker} object.
	 * @param year the year e.g. 2011
	 * @param monthOfYear the month which is starting from zero e.g. 03
	 * @param dayOfMonth the day e.g. 10
	 */

	public void setDatePicker(final DatePicker datePicker, final int year, final int monthOfYear, final int dayOfMonth) {
		// 非空判断
		if(datePicker != null){
			// 在当前activity的Ui线程中执行,直接调用会引发跨线程权限异常
			activityUtils.getCurrentActivity(false).runOnUiThread(new Runnable()
			{
				public void run()
				{
					try{
						// 设置年月日属性
						datePicker.updateDate(year, monthOfYear, dayOfMonth);
					}catch (Exception ignored){}
				}
			});
		}
	}


	/**
	 * 设置时间属性
	 * timePicker   需要设置属性的TimePicker控件
	 * hour			时
	 * minute	           分
	 * Sets the time in a given {@link TimePicker}.
	 *
	 * @param timePicker the {@code TimePicker} object.
	 * @param hour the hour e.g. 15
	 * @param minute the minute e.g. 30
	 */

	public void setTimePicker(final TimePicker timePicker, final int hour, final int minute) {
		// 非空检查
		if(timePicker != null){
			// 在当前activity的Ui线程中执行,直接调用会引发跨线程权限异常
			activityUtils.getCurrentActivity(false).runOnUiThread(new Runnable()
			{
				public void run()
				{
					try{
						// 设置时
						timePicker.setCurrentHour(hour);
						// 设置分
						timePicker.setCurrentMinute(minute);
					}catch (Exception ignored){}
				}
			});
		}
	}
	

	/**
	 * 设置进度条控件属性
	 * progressBar   需要设置的进度条
	 * progress      设置的值
	 * Sets the progress of a given {@link ProgressBar}. Examples are SeekBar and RatingBar.
	 * @param progressBar the {@code ProgressBar}
	 * @param progress the progress that the {@code ProgressBar} should be set to
	 */

	public void setProgressBar(final ProgressBar progressBar,final int progress) {
		// 非空检查
		if(progressBar != null){
			// 在当前activity的Ui线程中执行,直接调用会引发跨线程权限异常
			activityUtils.getCurrentActivity(false).runOnUiThread(new Runnable()
			{
				public void run()
				{
					try{
						// 设置进度属性
						progressBar.setProgress(progress);
					}catch (Exception ignored){}
				}
			});
		}
	}


	/**
	 * 设置选择开关属性，开 关 
	 * slidingDrawer   需要设置的选择开关
	 * status          Solo.CLOSED  Solo.OPENED
	 * Sets the status of a given SlidingDrawer. Examples are Solo.CLOSED and Solo.OPENED.
	 *
	 * @param slidingDrawer the {@link SlidingDrawer}
	 * @param status the status that the {@link SlidingDrawer} should be set to
	 */

	public void setSlidingDrawer(final SlidingDrawer slidingDrawer, final int status){
		// 非空判断
		if(slidingDrawer != null){
			// 在当前activity的Ui线程中执行,直接调用会引发跨线程权限异常
			activityUtils.getCurrentActivity(false).runOnUiThread(new Runnable()
			{
				public void run()
				{
					try{
						// 按照给定值，设定状态
						switch (status) {
						case CLOSED:
							slidingDrawer.close();
							break;
						case OPENED:
							slidingDrawer.open();
							break;
						}
					}catch (Exception ignored){}
				}
			});
		}

	}
}
