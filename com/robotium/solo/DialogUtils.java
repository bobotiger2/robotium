package com.robotium.solo;


import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


/**
 * 弹框处理工具类
 * Contains the waitForDialogToClose() method.
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class DialogUtils {
	// activity操作工具类
	private final ActivityUtils activityUtils;
	// view获取工具类
	private final ViewFetcher viewFetcher;
	// 等待工具类
	private final Sleeper sleeper;
	// 1s
	private final static int TIMEOUT_DIALOG_TO_CLOSE = 1000;
	// 200ms
	private final int MINISLEEP = 200;

	/**
	 * 构造函数
	 * Constructs this object.
	 * 
	 * @param activityUtils the {@code ActivityUtils} instance
	 * @param viewFetcher the {@code ViewFetcher} instance
	 * @param sleeper the {@code Sleeper} instance
	 */

	public DialogUtils(ActivityUtils activityUtils, ViewFetcher viewFetcher, Sleeper sleeper) {
		this.activityUtils = activityUtils;
		this.viewFetcher = viewFetcher;
		this.sleeper = sleeper;
	}


	/**
	 * 检查在指定时间内弹框是否关闭了.
	 * Waits for a {@link android.app.Dialog} to close.
	 *
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if the {@code Dialog} is closed before the timeout and {@code false} if it is not closed
	 */

	public boolean waitForDialogToClose(long timeout) {
		// 先等待弹框出现
		waitForDialogToOpen(TIMEOUT_DIALOG_TO_CLOSE, false);
		// 设置超时时间
		final long endTime = SystemClock.uptimeMillis() + timeout;
		// 循环检查弹框是否关闭了
		while (SystemClock.uptimeMillis() < endTime) {

			if(!isDialogOpen()){
				return true;
			}
			// 等待200ms
			sleeper.sleep(MINISLEEP);
		}
		return false;
	}



	/**
	 * 检查指定时间内，是否有弹框出现，
	 * timeout    设置的指定超时时间,单位 ms
	 * sleepFirst 是否需要先等待500ms,再做检查
	 * Waits for a {@link android.app.Dialog} to open.
	 *
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if the {@code Dialog} is opened before the timeout and {@code false} if it is not opened
	 */

	public boolean waitForDialogToOpen(long timeout, boolean sleepFirst) {
		// 设置超时时间
		final long endTime = SystemClock.uptimeMillis() + timeout;
		// 是否需要等待500ms后再查找
		if(sleepFirst)
			sleeper.sleep();
		// 循环检查是否弹框出现了
		while (SystemClock.uptimeMillis() < endTime) {

			if(isDialogOpen()){
				return true;
			}
			// 等待300ms
			sleeper.sleepMini();
		}
		return false;
	}

	/**
	 * 检查是否有弹框出现
	 * Checks if a dialog is open. 
	 * 
	 * @return true if dialog is open
	 */

	private boolean isDialogOpen(){
		// 获取当前显示的activity
		final Activity activity = activityUtils.getCurrentActivity(false);
		// 获取当前的所有DecorView类型View
		final View[] views = viewFetcher.getWindowDecorViews();
		// 获取最新的DecorView,DecorView是根
		View view = viewFetcher.getRecentDecorView(views);	
		// 遍历检查是否有打开的弹框
		if(!isDialog(activity, view)){
			for(View v : views){
				if(isDialog(activity, v)){
					return true;
				}
			}
		}
		else {
			return true;
		}
		return false;
	}
	
	/**
	 * 判断decorView是否是给定activity的，即检查弹框是否是当前activity的
	 * Checks that the specified DecorView and the Activity DecorView are not equal.
	 * 
	 * @param activity the activity which DecorView is to be compared
	 * @param decorView the DecorView to compare
	 * @return true if not equal
	 */
	
	private boolean isDialog(Activity activity, View decorView){
		// 检查decorView是都可见的，不可见直接返回false
		if(decorView == null || !decorView.isShown()){
			return false;
		}
		// 获取Context
		Context viewContext = null;
		if(decorView != null){
			viewContext = decorView.getContext();
		}
		// 获取需要的Context
		if (viewContext instanceof ContextThemeWrapper) {
			ContextThemeWrapper ctw = (ContextThemeWrapper) viewContext;
			viewContext = ctw.getBaseContext();
		}
		// 获取activity对应的Context
		Context activityContext = activity;
		Context activityBaseContext = activity.getBaseContext();
		// 检查Context 是否是一致的,并且 activity不是在弹框中的
		return (activityContext.equals(viewContext) || activityBaseContext.equals(viewContext)) && (decorView != activity.getWindow().getDecorView());
	}

	/**
	 * 隐藏软键盘
	 * editText 指定的编辑框
	 * shouldSleepFirst 是否要先等待500ms再操作
	 * shouldSleepAfter 执行完后是否要等待500ms再返回,仅对传入editText非null有效
	 * Hides the soft keyboard
	 * 
	 * @param shouldSleepFirst whether to sleep a default pause first
	 * @param shouldSleepAfter whether to sleep a default pause after
	 */

	public void hideSoftKeyboard(EditText editText, boolean shouldSleepFirst, boolean shouldSleepAfter) {
		// 获取当前activity
		Activity activity = activityUtils.getCurrentActivity(shouldSleepFirst);
		// 获取输入控制管理器服务
		InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		// 调用隐藏软键盘方法
		if(editText != null) {
			inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
			return;
		}
		// 如果没有指定editText,获取当前焦点所在的View
		View focusedView = activity.getCurrentFocus();
		// 如果获取的 View不是EditText
		if(!(focusedView instanceof EditText)) {
			// 获取当前页面的最新 EditText
			EditText freshestEditText = viewFetcher.getFreshestView(viewFetcher.getCurrentViews(EditText.class));
			// 如果可以取到EditText那么设置可用的
			if(freshestEditText != null){
				focusedView = freshestEditText;
			}
		}
		// 隐藏软键盘
		if(focusedView != null) {
			inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
		}
		// 如果设置了等待，那么等待500ms后返回
		if(shouldSleepAfter){
			sleeper.sleep();
		}
	}
}
