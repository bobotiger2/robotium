package com.robotium.solo;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import junit.framework.Assert;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.TextView;

/**
 * 点击操作工具类，用于触发屏幕点击事件
 * Contains various click methods. Examples are: clickOn(),
 * clickOnText(), clickOnScreen().
 *
 * @author Renas Reda, renas.reda@robotium.com
 *
 */

class Clicker {
	// 常量用于日志打印，标记这是Robotium的东西
	private final String LOG_TAG = "Robotium";
	// activity操作工具类
	private final ActivityUtils activityUtils;
	// view查找工具类
	private final ViewFetcher viewFetcher;
	// Instrument，用于发送各类事件
	private final Instrumentation inst;
	// 按键信息发送工具类
	private final Sender sender;
	// 等待工具类
	private final Sleeper sleeper;
	// 各种条件判断工具类
	private final Waiter waiter;
	// WebView操作工具类
	private final WebUtils webUtils;
	// 弹框类操作工具类
	private final DialogUtils dialogUtils;
	// 100ms
	private final int MINISLEEP = 100;
	// 200ms
	private final int TIMEOUT = 200;
	// 1.5s
	private final int WAIT_TIME = 1500;


	/**
	 * 构造函数
	 * Constructs this object.
	 *
	 * @param activityUtils the {@code ActivityUtils} instance
	 * @param viewFetcher the {@code ViewFetcher} instance
	 * @param sender the {@code Sender} instance
	 * @param inst the {@code android.app.Instrumentation} instance
	 * @param sleeper the {@code Sleeper} instance
	 * @param waiter the {@code Waiter} instance
	 * @param webUtils the {@code WebUtils} instance
	 * @param dialogUtils the {@code DialogUtils} instance
	 */

	public Clicker(ActivityUtils activityUtils, ViewFetcher viewFetcher, Sender sender, Instrumentation inst, Sleeper sleeper, Waiter waiter, WebUtils webUtils, DialogUtils dialogUtils) {

		this.activityUtils = activityUtils;
		this.viewFetcher = viewFetcher;
		this.sender = sender;
		this.inst = inst;
		this.sleeper = sleeper;
		this.waiter = waiter;
		this.webUtils = webUtils;
		this.dialogUtils = dialogUtils;
	}

	/**
	 * 点击屏幕上的一个特定坐标，如果点击失败会重试10次
	 * x    x坐标值
	 * y    y坐标值
	 * Clicks on a given coordinate on the screen.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */

	public void clickOnScreen(float x, float y) {
		// 设置标记位，标识点击事件发送还未成功
		boolean successfull = false;
		// 设置重试计数器
		int retry = 0;
		// 异常信息
		SecurityException ex = null;
		// 如果还没发送成功且重试次数少于10次，那么继续重试
		while(!successfull && retry < 10) {
			// 构造点击事件
			long downTime = SystemClock.uptimeMillis();
			long eventTime = SystemClock.uptimeMillis();
			MotionEvent event = MotionEvent.obtain(downTime, eventTime,
					MotionEvent.ACTION_DOWN, x, y, 0);
			MotionEvent event2 = MotionEvent.obtain(downTime, eventTime,
					MotionEvent.ACTION_UP, x, y, 0);
			try{
				// 发送点击事件
				inst.sendPointerSync(event);
				inst.sendPointerSync(event2);
				// 事件发送未抛异常，则标记为成功
				successfull = true;
				// 等待100ms
				sleeper.sleep(MINISLEEP);
			}catch(SecurityException e){
				ex = e;
				// 关闭可能导致异常的软键盘影响，屏蔽软键盘，继续重试
				dialogUtils.hideSoftKeyboard(null, false, true);
				retry++;
			}
		}
		// 如果达到10次还未成功，则不再重试，记录异常日志，退出
		if(!successfull) {
			Assert.fail("Click at ("+x+", "+y+") can not be completed! ("+(ex != null ? ex.getClass().getName()+": "+ex.getMessage() : "null")+")");
		}
	}

	/**
	 * 发送一个长按事件，可设置长按时间
	 * x    x坐标
	 * y    y坐标
	 * time 长按时间，单位ms
	 * Long clicks a given coordinate on the screen.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param time the amount of time to long click
	 */

	public void clickLongOnScreen(float x, float y, int time) {
		// 设置标记位，标识点击事件发送还未成功
		boolean successfull = false;
		// 设置重试计数器
		int retry = 0;
		// 异常信息
		SecurityException ex = null;
		// 构造按下事件
		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis();
		MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
		// 如果事件发送失败，重试少于10次，则继续重试
		while(!successfull && retry < 10) {
			try{
				inst.sendPointerSync(event);
				// 事件发送成功，标记位设置为true
				successfull = true;
			}catch(SecurityException e){
				ex = e;
				// 关闭可能导致异常的软键盘影响，屏蔽软键盘，继续重试
				dialogUtils.hideSoftKeyboard(null, false, true);
				retry++;
			}
		}
		// 如果达到10次还未成功，则不再重试，记录异常日志，退出
		if(!successfull) {
			Assert.fail("Long click at ("+x+", "+y+") can not be completed! ("+(ex != null ? ex.getClass().getName()+": "+ex.getMessage() : "null")+")");
		}
		// 构造一个移动事件,相对原先按下坐标滑动1个像素
		eventTime = SystemClock.uptimeMillis();
		event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x + 1.0f, y + 1.0f, 0);
		inst.sendPointerSync(event);
		// 如果设置了长按事件，且时间大于0，则等待相应的时间
		if(time > 0)
			sleeper.sleep(time);
		// 如果设置的值小于等于0,那么使用默认长按时间的2.5倍
		else
			sleeper.sleep((int)(ViewConfiguration.getLongPressTimeout() * 2.5f));
		// 构造松开事件
		eventTime = SystemClock.uptimeMillis();
		event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
		inst.sendPointerSync(event);
		// 等待500ms
		sleeper.sleep();
	}


	/**
	 * 点击指定的view
	 * Clicks on a given {@link View}.
	 *
	 * @param view the view that should be clicked
	 */

	public void clickOnScreen(View view) {
		// 设置为非长按点击
		clickOnScreen(view, false, 0);
	}

	/**
	 * 点击给定的view,可设置是否长点击，并设置长按时间
	 * view      需要点击的view
	 * longClick 是否长按
	 * time      需要长按的时间
	 * Private method used to click on a given view.
	 *
	 * @param view the view that should be clicked
	 * @param longClick true if the click should be a long click
	 * @param time the amount of time to long click
	 */

	public void clickOnScreen(View view, boolean longClick, int time) {
		// 如果传入null参数，给出异常提示
		if(view == null)
			Assert.fail("View is null and can therefore not be clicked!");
		// 获取view的xy坐标
		float[] xyToClick = getClickCoordinates(view);
		// 获取x坐标
		float x = xyToClick[0];
		// 获取y坐标
		float y = xyToClick[1];
		// 如果获取的xy坐标存在0那么重新查找，期望找到正常可点击的view
		if(x == 0 || y == 0){
			// 等待300ms
			sleeper.sleepMini();
			try {
				view = getIdenticalView(view);
			} catch (Exception ignored){}
			// 如果可以找到，那么重新获取一次坐标
			if(view != null){
				xyToClick = getClickCoordinates(view);
				x = xyToClick[0];
				y = xyToClick[1];
			}
		}
		// 如果设置了长按，那么发送长按情况
		if (longClick)
			clickLongOnScreen(x, y, time);
		// 发送点击请求
		else
			clickOnScreen(x, y);
	}

	/**
	 * 按照给定的view,获取当前页面展示的同个view,如果找不到就返回null
	 * Returns an identical View to the one specified.
	 * 
	 * @param view the view to find
	 * @return identical view of the specified view
	 */
	
	private View getIdenticalView(View view) {
		View viewToReturn = null;
		// 查找相同类型的view
		List<? extends View> visibleViews = RobotiumUtils.removeInvisibleViews(viewFetcher.getCurrentViews(view.getClass()));
		// 查找相同id的view
		for(View v : visibleViews){
			if(v.getId() == view.getId()){
				// 判断2个view是否是一致的mParents
				if(isParentsEqual(v, view)){
					viewToReturn = v;
					break;
				}
			}
		}
		return viewToReturn;
	}
	
	/**
	 * 比较2个 view的mParents属性是否一致
	 * Compares the parent views of the specified views.
	 * 
	 * @param firstView the first view
	 * @param secondView the second view
	 * @return true if parents of the specified views are equal
	 */
	
	private boolean isParentsEqual(View firstView, View secondView){
		// 如果id或者类型不一致，那么直接返回false
		if(firstView.getId() != secondView.getId() || !firstView.getClass().isAssignableFrom(secondView.getClass())){
			return false;
		}
		// 判断mParent是否一致，一致返回true,不一致返回false
		if (firstView.getParent() != null && firstView.getParent() instanceof View && 
				secondView.getParent() != null && secondView.getParent() instanceof View) {

			return isParentsEqual((View) firstView.getParent(), (View) secondView.getParent());
		} else {
			return true;
		}
	}

	

	/**
	 * 获取View点击的中间位置坐标
	 * view 需要点击的view
	 * Returns click coordinates for the specified view.
	 * 
	 * @param view the view to get click coordinates from
	 * @return click coordinates for a specified view
	 */

	private float[] getClickCoordinates(View view){
		// 存储view的高度和宽度
		int[] xyLocation = new int[2];
		// 存储view的xy坐标，左下角坐标值
		float[] xyToClick = new float[2];
		// 获取左下角坐标值
		view.getLocationOnScreen(xyLocation);
		// 获取宽度
		final int viewWidth = view.getWidth();
		// 获取高度
		final int viewHeight = view.getHeight();
		// 计算中间点x坐标
		final float x = xyLocation[0] + (viewWidth / 2.0f);
		// 计算中间点y坐标
		float y = xyLocation[1] + (viewHeight / 2.0f);

		xyToClick[0] = x;
		xyToClick[1] = y;

		return xyToClick;
	}


	/**
	 * 长按指定text内容的第1个View,等待弹框出现，向下 按键index次，再点击回车键.确认
	 * Long clicks on a specific {@link TextView} and then selects
	 * an item from the context menu that appears. Will automatically scroll when needed.
	 *
	 * @param text the text that should be clicked on. The parameter <strong>will</strong> be interpreted as a regular expression.
	 * @param index the index of the menu item that should be pressed
	 */

	public void clickLongOnTextAndPress(String text, int index)
	{
		// 长按点击指定 text的View
		clickOnText(text, true, 0, true, 0);
		// 等待弹框出现
		dialogUtils.waitForDialogToOpen(Timeout.getSmallTimeout(), true);
		try{
			// 发送向下 按键 事件
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
		}catch(SecurityException e){
			Assert.fail("Can not press the context menu!");
		}
		// 发送指定次数的向下 按键
		for(int i = 0; i < index; i++)
		{	// 等待300ms
			sleeper.sleepMini();
			// 发送向下 按键事件
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
		}
		// 发送确认 事件
		inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
	}

	/**
	 * 打开菜单
	 * Opens the menu and waits for it to open.
	 */

	private void openMenu(){
		// 等待300ms
		sleeper.sleepMini();
		// 如果200ms内未发现菜单出现
		if(!dialogUtils.waitForDialogToOpen(TIMEOUT, false)) {
			try{
				// 发送菜单按钮
				sender.sendKeyCode(KeyEvent.KEYCODE_MENU);
				// 等待菜单出现
				dialogUtils.waitForDialogToOpen(WAIT_TIME, true);
			}catch(SecurityException e){
				Assert.fail("Can not open the menu!");
			}
		}
	}

	/**
	 * 点击菜单中指定的 text
	 * Clicks on a menu item with a given text.
	 *
	 * @param text the menu text that should be clicked on. The parameter <strong>will</strong> be interpreted as a regular expression.
	 */

	public void clickOnMenuItem(String text)
	{	// 打开菜单
		openMenu();
		// 点击指定text内容的项
		clickOnText(text, false, 1, true, 0);
	}

	/**
	 * 点击菜单中指定text内容的项，可设置是否主菜单未发现继续查找子级菜单目录
	 * text    指定的文本内容
	 * subMenu 是否点击子级菜单
	 * Clicks on a menu item with a given text.
	 *
	 * @param text the menu text that should be clicked on. The parameter <strong>will</strong> be interpreted as a regular expression.
	 * @param subMenu true if the menu item could be located in a sub menu
	 */

	public void clickOnMenuItem(String text, boolean subMenu)
	{
		// 等待300ms
		sleeper.sleepMini();

		TextView textMore = null;
		// 设置xy左边存储遍历
		int [] xy = new int[2];
		int x = 0;
		int y = 0;
		// 检查菜单是否已打开，未打开则打开菜单,超时200ms
		if(!dialogUtils.waitForDialogToOpen(TIMEOUT, false)) {
			try{
				// 发送打开菜单事件
				sender.sendKeyCode(KeyEvent.KEYCODE_MENU);
				// 等待打开,超时1.5s
				dialogUtils.waitForDialogToOpen(WAIT_TIME, true);
			}catch(SecurityException e){
				Assert.fail("Can not open the menu!");
			}
		}
		// 检查指定的内容菜单是否出现
		boolean textShown = waiter.waitForText(text, 1, WAIT_TIME, true) != null;
		// 如果设置了子级目录，那么继续查找子级菜单,不关注指定内容，只按照数量大于5,那么找出最右边的菜单点击
		if(subMenu && (viewFetcher.getCurrentViews(TextView.class).size() > 5) && !textShown){
			// 查找最下位置的菜单项点击
			for(TextView textView : viewFetcher.getCurrentViews(TextView.class)){
				x = xy[0];
				y = xy[1];
				textView.getLocationOnScreen(xy);

				if(xy[0] > x || xy[1] > y)
					textMore = textView;
			}
		}
		// 如果找到，那么发送点击事件
		if(textMore != null)
			clickOnScreen(textMore);
		// 菜单中未找到，可能展示在其他控件中，尝试发送点击事件。可能用户误操作api,猜测用户意图，给予修正
		clickOnText(text, false, 1, true, 0);
	}

	/**
	 * 点击actionbar 按照id查找
	 * Clicks on an ActionBar item with a given resource id
	 *
	 * @param resourceId the R.id of the ActionBar item
	 */

	public void clickOnActionBarItem(int resourceId){
		// 发送点击事件
		inst.invokeMenuActionSync(activityUtils.getCurrentActivity(), resourceId, 0);
	}

	/**
	 * 点击 ActionBar的 home或up
	 * Clicks on an ActionBar Home/Up button.
	 */

	public void clickOnActionBarHomeButton() {
		// 获取当前的activity
		Activity activity = activityUtils.getCurrentActivity();
		MenuItem homeMenuItem = null;

		try {
			// 通过反射，构造MenuItem对象
			Class<?> cls = Class.forName("com.android.internal.view.menu.ActionMenuItem");
			Class<?> partypes[] = new Class[6];
			partypes[0] = Context.class;
			partypes[1] = Integer.TYPE;
			partypes[2] = Integer.TYPE;
			partypes[3] = Integer.TYPE;
			partypes[4] = Integer.TYPE;
			partypes[5] = CharSequence.class;
			Constructor<?> ct = cls.getConstructor(partypes);
			Object argList[] = new Object[6];
			argList[0] = activity;
			argList[1] = 0;
			argList[2] = android.R.id.home;
			argList[3] = 0;
			argList[4] = 0;
			argList[5] = "";
			// 构造ActionBar的home
			homeMenuItem = (MenuItem) ct.newInstance(argList);
		} catch (Exception ex) {
			Log.d(LOG_TAG, "Can not find methods to invoke Home button!");
		}

		if (homeMenuItem != null) {
			try{
				// 发送home事件
				activity.getWindow().getCallback().onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, homeMenuItem);
			}catch(Exception ignored) {}
		}
	}

	/**
	 * 点击指定条件的第match个WebElement,可以设置是否使用js发送点击事件.设置焦点到对应的WebElement
	 * by     					 给定查找条件
	 * match   					给定的index
	 * scroll  					是否需要拖动刷新查找
	 * useJavaScriptToClick     是否使用js发送点击事件
	 * Clicks on a web element using the given By method.
	 *
	 * @param by the By object e.g. By.id("id");
	 * @param match if multiple objects match, this determines which one will be clicked
	 * @param scroll true if scrolling should be performed
	 * @param useJavaScriptToClick true if click should be perfomed through JavaScript
	 */

	public void clickOnWebElement(By by, int match, boolean scroll, boolean useJavaScriptToClick){
		WebElement webElement = null;
		// 如果设置了js点击那么调用js点击
		if(useJavaScriptToClick){
			// 查找指定的webElement
			webElement = waiter.waitForWebElement(by, match, Timeout.getSmallTimeout(), false);
			// 未找到，提示异常
			if(webElement == null){
				Assert.fail("WebElement with " + webUtils.splitNameByUpperCase(by.getClass().getSimpleName()) + ": '" + by.getValue() + "' is not found!");
			}
			// 调用js点击WebElement
			webUtils.executeJavaScript(by, true);
			return;
		}
		// 查找指定的WebElement
		WebElement webElementToClick = waiter.waitForWebElement(by, match, Timeout.getSmallTimeout(), scroll);
		// 为找到提示异常
		if(webElementToClick == null){
			if(match > 1) {
				Assert.fail(match + " WebElements with " + webUtils.splitNameByUpperCase(by.getClass().getSimpleName()) + ": '" + by.getValue() + "' are not found!");
			}
			else {
				Assert.fail("WebElement with " + webUtils.splitNameByUpperCase(by.getClass().getSimpleName()) + ": '" + by.getValue() + "' is not found!");
			}
		}
		// 找到通过instrument发送点击事件
		clickOnScreen(webElementToClick.getLocationX(), webElementToClick.getLocationY());
	}


	/**
	 * 点击指定的内容的TextView控件,可设置是否长按，长按时间，需要点符合条件的第几个
	 * regex      设置的文本内容
	 * longClick  是否长按
	 * match      第几个
	 * scroll     是否允许拖动刷新，如列表之类的控件，拖动可以刷新内容
	 * time       长按时间
	 * Clicks on a specific {@link TextView} displaying a given text.
	 *
	 * @param regex the text that should be clicked on. The parameter <strong>will</strong> be interpreted as a regular expression.
	 * @param longClick {@code true} if the click should be a long click
	 * @param match the regex match that should be clicked on
	 * @param scroll true if scrolling should be performed
	 * @param time the amount of time to long click
	 */

	public void clickOnText(String regex, boolean longClick, int match, boolean scroll, int time) {
		// 获取指定条件的TextView
		TextView textToClick = waiter.waitForText(regex, match, Timeout.getSmallTimeout(), scroll, true, false);
		// 如果找到对应  TextView，发送相关点击事件
		if (textToClick != null) {
			clickOnScreen(textToClick, longClick, time);
		}
		// 如果没找到
		else {
			// 设置了match 大于1，那么提示异常信息,并退出
			if(match > 1){
				Assert.fail(match + " matches of text string: '" + regex +  "' are not found!");
			}
			// 如果设置的小于等于1,打印出当前所有的当前所有TextView类控件信息,并退出
			else{
				ArrayList<TextView> allTextViews = RobotiumUtils.removeInvisibleViews(viewFetcher.getCurrentViews(TextView.class));
				allTextViews.addAll((Collection<? extends TextView>) webUtils.getTextViewsFromWebView());

				for (TextView textView : allTextViews) {
					Log.d(LOG_TAG, "'" + regex + "' not found. Have found: '" + textView.getText() + "'");
				}
				allTextViews = null;
				Assert.fail("Text string: '" + regex + "' is not found!");
			}
		}
	}


	/**
	 * 点击指定类型和文本内容的TextView
	 * Clicks on a {@code View} of a specific class, with a given text.
	 *
	 * @param viewClass what kind of {@code View} to click, e.g. {@code Button.class} or {@code TextView.class}
	 * @param nameRegex the name of the view presented to the user. The parameter <strong>will</strong> be interpreted as a regular expression.
	 */

	public <T extends TextView> void clickOn(Class<T> viewClass, String nameRegex) {
		// 查找指定类型的view
		T viewToClick = (T) waiter.waitForText(viewClass, nameRegex, 0, Timeout.getSmallTimeout(), true, true, false);
		// 找到了，发送点击事件
		if (viewToClick != null) {
			clickOnScreen(viewToClick);
			// 未找到，打印日志，记录当前所有的TextView,并退出
		} else {
			ArrayList <T> allTextViews = RobotiumUtils.removeInvisibleViews(viewFetcher.getCurrentViews(viewClass));

			for (T view : allTextViews) {
				Log.d(LOG_TAG, "'" + nameRegex + "' not found. Have found: '" + view.getText() + "'");
			}
			Assert.fail(viewClass.getSimpleName() + " with text: '" + nameRegex + "' is not found!");
		}
	}

	/**
	 * 点击指定类型的第index个View
	 * Clicks on a {@code View} of a specific class, with a certain index.
	 *
	 * @param viewClass what kind of {@code View} to click, e.g. {@code Button.class} or {@code ImageView.class}
	 * @param index the index of the {@code View} to be clicked, within {@code View}s of the specified class
	 */

	public <T extends View> void clickOn(Class<T> viewClass, int index) {
		// 点击指定条件的view
		clickOnScreen(waiter.waitForAndGetView(index, viewClass));
	}


	/**
	 *	点击找到的第1个列表的第line行，并返回此行中的所有TextView类型的View
	 * Clicks on a certain list line and returns the {@link TextView}s that
	 * the list line is showing. Will use the first list it finds.
	 *
	 * @param line the line that should be clicked
	 * @return a {@code List} of the {@code TextView}s located in the list line
	 */

	public ArrayList<TextView> clickInList(int line) {
		return clickInList(line, 0, false, 0);
	}

	/**
	 * 点击指定的第index个列表的第line行，可设置是否长按,并返回此行中的所有TextView类型的View
	 * line      指定的列
	 * index     指定的列表
	 * longClick 是否长按
	 * time      长按时间
	 * Clicks on a certain list line on a specified List and
	 * returns the {@link TextView}s that the list line is showing.
	 *
	 * @param line the line that should be clicked
	 * @param index the index of the list. E.g. Index 1 if two lists are available
	 * @return an {@code ArrayList} of the {@code TextView}s located in the list line
	 */

	public ArrayList<TextView> clickInList(int line, int index, boolean longClick, int time) {
		// 设置超时时间点
		final long endTime = SystemClock.uptimeMillis() + Timeout.getSmallTimeout();
		// 设置index,因排序从0开始，因此减1
		int lineIndex = line - 1;
	    // 异常情况修正回0
		if(lineIndex < 0)
			lineIndex = 0;
		// 获取指定的第index个列表
		ArrayList<View> views = new ArrayList<View>();
		final AbsListView absListView = waiter.waitForAndGetView(index, AbsListView.class);
		// 未找到，提示异常
		if(absListView == null)
			Assert.fail("ListView is null!");
		// 如果设置的index大于列表中的内容，还未超时，那么不断重试，存在列表中内容不断增加的情况
		while(lineIndex > absListView.getChildCount()){
			// 检查是否超时
			final boolean timedOut = SystemClock.uptimeMillis() > endTime;
			// 超时提示异常
			if (timedOut){
				int numberOfLines = absListView.getChildCount();
				Assert.fail("Can not click on line number " + line + " as there are only " + numberOfLines + " lines available");
			}
			// 等待500ms
			sleeper.sleep();
		}
		// 找到列表中指定的列
		View view = getViewOnListLine(absListView, lineIndex);
		// 找到
		if(view != null){
			// 获取所有指定条件的View
			views = viewFetcher.getViews(view, true);
			// 剔除不可见的
			views = RobotiumUtils.removeInvisibleViews(views);
			// 长按对应的 view
			clickOnScreen(view, longClick, time);
		}
		// 过滤掉所有非TextView类型的
		return RobotiumUtils.filterViews(TextView.class, views);
	}

	/**
	 * 获取列表指定行的view
	 * absListView   给定的列表
	 * lineIndex     指定的行
	 * Returns the view in the specified list line
	 * 
	 * @param absListView the ListView to use
	 * @param lineIndex the line index of the View
	 * @return the View located at a specified list line
	 */

	private View getViewOnListLine(AbsListView absListView, int lineIndex){
		// 设置超时时间点
		final long endTime = SystemClock.uptimeMillis() + Timeout.getSmallTimeout();
		// 获取指定行的 View
		View view = absListView.getChildAt(lineIndex);
		// 获取不到,还未超时，继续重试
		while(view == null){
			// 检查是否超时
			final boolean timedOut = SystemClock.uptimeMillis() > endTime;
			// 超时提示异常
			if (timedOut){
				Assert.fail("View is null and can therefore not be clicked!");
			}
			// 等500ms
			sleeper.sleep();
			// 重试获取view
			view = absListView.getChildAt(lineIndex);
		}
		return view;
	}
}
