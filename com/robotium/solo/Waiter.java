package com.robotium.solo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import junit.framework.Assert;
import android.app.Activity;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;


/**
 * View获取工具类，用于等待各类信息出现
 * Contains various wait methods. Examples are: waitForText(),
 * waitForView().
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class Waiter {
	// activity操作工具类
	private final ActivityUtils activityUtils;
	// View查找工具类
	private final ViewFetcher viewFetcher;
	// 控件查找工具类
	private final Searcher searcher;
	// 滑动类空间操作工具类
	private final Scroller scroller;
	// 延时等待工具类
	private final Sleeper sleeper;
	// 延时50ms
	private final int MINISLEEP = 50;


	/**
	 * 构造函数
	 * Constructs this object.
	 *
	 * @param activityUtils the {@code ActivityUtils} instance
	 * @param viewFetcher the {@code ViewFetcher} instance
	 * @param searcher the {@code Searcher} instance
	 * @param scroller the {@code Scroller} instance
	 * @param sleeper the {@code Sleeper} instance
	 */

	public Waiter(ActivityUtils activityUtils, ViewFetcher viewFetcher, Searcher searcher, Scroller scroller, Sleeper sleeper){
		this.activityUtils = activityUtils;
		this.viewFetcher = viewFetcher;
		this.searcher = searcher;
		this.scroller = scroller;
		this.sleeper = sleeper;
	}

	/**
	 * 等待指定名字的activity出现,默认超时时间10s
	 * 超时还未出现返回false,10s内出现则返回true
	 * Waits for the given {@link Activity}.
	 *
	 * @param name the name of the {@code Activity} to wait for e.g. {@code "MyActivity"}
	 * @return {@code true} if {@code Activity} appears before the timeout and {@code false} if it does not
	 *
	 */

	public boolean waitForActivity(String name){
		// 等待指定名字的Activity出现，超时时间默认为10s
		return waitForActivity(name, Timeout.getSmallTimeout());
	}

	/**
	 * 等待给定名字的activity在指定时间内出现,如果未出现返回false,出现返回true
	 * Waits for the given {@link Activity}.
	 *
	 * @param name the name of the {@code Activity} to wait for e.g. {@code "MyActivity"}
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if {@code Activity} appears before the timeout and {@code false} if it does not
	 *
	 */

	public boolean waitForActivity(String name, int timeout){
		// 获取当前最新的activity
		Activity currentActivity = activityUtils.getCurrentActivity(false, false);
		// 设置超时时间点
		final long endTime = SystemClock.uptimeMillis() + timeout;
		// 如期望的 activity未出现，未到超时时间点，则继续刷新判断
		while(SystemClock.uptimeMillis() < endTime){
			// 判断当前 activity是否为指定名字的，找到则退出查找，返回true,未找到则继续刷新查找
			if(currentActivity != null && currentActivity.getClass().getSimpleName().equals(name)) {
				return true;
			}
			// 等待50ms
			sleeper.sleep(MINISLEEP);
			// 继续获取
			currentActivity = activityUtils.getCurrentActivity(false, false);
		}
		// 超过超时点，返回false
		return false;
	}
	
	/**
	 * 等待指定class类型的activity出现，默认超时10s,如在10s内未出现返回false,出现返回true
	 * Waits for the given {@link Activity}.
	 *
	 * @param activityClass the class of the {@code Activity} to wait for
	 * @return {@code true} if {@code Activity} appears before the timeout and {@code false} if it does not
	 *
	 */

	public boolean waitForActivity(Class<? extends Activity> activityClass){
		// 设置超时10s,等待activity出现
		return waitForActivity(activityClass, Timeout.getSmallTimeout());
	}

	/**
	 * 等待 指定class类型的activity出现
	 * timeout 为超时时间，在超时时间内出现返回true,未出现返回false
	 * Waits for the given {@link Activity}.
	 *
	 * @param activityClass the class of the {@code Activity} to wait for
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if {@code Activity} appears before the timeout and {@code false} if it does not
	 *
	 */

	public boolean waitForActivity(Class<? extends Activity> activityClass, int timeout){
		// 获取当前最新activity
		Activity currentActivity = activityUtils.getCurrentActivity(false, false);
		// 设置超时时间点
		final long endTime = SystemClock.uptimeMillis() + timeout;
		// 未找到，还未到达超时时间点，继续查找
		while(SystemClock.uptimeMillis() < endTime){
			// 检查是否为指定class类型的，找到则退出查找返回true
			if(currentActivity != null && currentActivity.getClass().equals(activityClass)) {
				return true;
			}
			// 等待50ms
			sleeper.sleep(MINISLEEP);
			// 刷新当前最新的activity,继续查找
			currentActivity = activityUtils.getCurrentActivity(false, false);
		}
		// 超时时间段内未找到，返回false
		return false;
	}

	/**
	 * 等待指定类型的 view是否出现,出现返回true,未出现返回false.该方法未带超时参数，易导致死循环，建议使用带超时参数的
	 * viewClass  view的 class类型
	 * index      期望次类型的数量
	 * sleep      true 等待500ms后查找,false  立即查找
	 * scroll     true 对于可滑动控件，未找到时滑动下刷新内容，false  不滑动
	 * Waits for a view to be shown.
	 * 
	 * @param viewClass the {@code View} class to wait for
	 * @param index the index of the view that is expected to be shown
	 * @param sleep true if should sleep
	 * @param scroll {@code true} if scrolling should be performed
	 * @return {@code true} if view is shown and {@code false} if it is not shown before the timeout
	 */

	public <T extends View> boolean waitForView(final Class<T> viewClass, final int index, boolean sleep, boolean scroll){
		// 临时views缓存
		Set<T> uniqueViews = new HashSet<T>();
		boolean foundMatchingView;
		// 如果设置了 scroll 为true 直接调用改方法，无法找到则容易进入死循环
		while(true){
			// true,等待500ms.false 不等待
			if(sleep)
				sleeper.sleep();
			// 检查该查询条件是否可以检索到,未符合为 false，符合为true
			foundMatchingView = searcher.searchFor(uniqueViews, viewClass, index);
			// 符合条件,返回true
			if(foundMatchingView)
				return true;
			// 设置了需要滑动，但配置不可滑动 返回false
			if(scroll && !scroller.scrollDown())
				return false;
			// 如果不可滑动，返回false
			if(!scroll)
				return false;
		}
	}

	/**
	 * 带超时参数的，等待 view类型出现方法
	 * Waits for a view to be shown.
	 * 
	 * @param viewClass the {@code View} class to wait for
	 * @param index the index of the view that is expected to be shown. 
	 * @param timeout the amount of time in milliseconds to wait
	 * @param scroll {@code true} if scrolling should be performed
	 * @return {@code true} if view is shown and {@code false} if it is not shown before the timeout
	 */

	public <T extends View> boolean waitForView(final Class<T> viewClass, final int index, final int timeout, final boolean scroll){
		// 临时views缓存
		Set<T> uniqueViews = new HashSet<T>();
		// 设置超时时间点
		final long endTime = SystemClock.uptimeMillis() + timeout;
		boolean foundMatchingView;
		// 未找到指定数量的views,还未超时继续查找
		while (SystemClock.uptimeMillis() < endTime) {
			// 等待500ms
			sleeper.sleep();
			// 检查条件是否满足，满足为true,不满足为false
			foundMatchingView =  searcher.searchFor(uniqueViews, viewClass, index);
			// 满足条件，退出检查，返回true
			if(foundMatchingView)
				return true;
			// 如果设置了可拖动，那么刷新可拖动控件
			if(scroll) 
				scroller.scrollDown();
		}
		// 条件不满足，返回false
		return false;
	}



	/**
	 * 等待一组class类型中的任一class类型的view出现,超时为10s.
	 * 10s内条件达成返回true,为达成返回 false
	 * scrollMethod  true 调用scroller.scroll(Scroller.DOWN), false 调用 scroller.scrollDown()
	 * Waits for two views to be shown.
	 *
	 * @param scrollMethod {@code true} if it's a method used for scrolling
	 * @param classes the classes to wait for 
	 * @return {@code true} if any of the views are shown and {@code false} if none of the views are shown before the timeout
	 */

	public <T extends View> boolean  waitForViews(boolean scrollMethod, Class<? extends T>... classes) {
		// 设置超时时间点
		final long endTime = SystemClock.uptimeMillis() + Timeout.getSmallTimeout();
		// 条件未满足，未达到超时时间到，继续检查
		while (SystemClock.uptimeMillis() < endTime) {
			// 检查是否有其中包含的任一class类型出现,出现则退出检查，返回true
			for (Class<? extends T> classToWaitFor : classes) {
				if (waitForView(classToWaitFor, 0, false, false)) {
					return true;
				}
			}
			// 按照配置调用对应的方法
			if(scrollMethod){
				scroller.scroll(Scroller.DOWN);
			}
			else {
				scroller.scrollDown();
			}
			// 等待500ms
			sleeper.sleep();
		}
		// 条件未满足，返回false
		return false;
	}

	/**
	 * 等待指定的view出现，默认超时20s
	 * Waits for a given view. Default timeout is 20 seconds.
	 * 
	 * @param view the view to wait for
	 * @return {@code true} if view is shown and {@code false} if it is not shown before the timeout
	 */

	public boolean waitForView(View view){
		// 等待指定的view出现,超时设置20s,可拖动，view已渲染
		return waitForView(view, Timeout.getLargeTimeout(), true, true);
	}

	/**
	 * 等待一个指定的view出现，可设置超时时间
	 * timeout  超时时间，单位 ms
	 * Waits for a given view. 
	 * 
	 * @param view the view to wait for
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if view is shown and {@code false} if it is not shown before the timeout
	 */

	public boolean waitForView(View view, int timeout){
		// 等待指定的view出现,设置的超时时间,可拖动，不检查是否shown
		return waitForView(view, timeout, true, false);
	}

	/**
	 * 等待指定的view出现
	 * view         指定的view
	 * timeout      超时时间,单位 ms
	 * scroll       true需要拖动刷新可拖动控件,false 不拖动
 	 * checkIsShown true 调用view.isShown()检查是否为true ,false  不调用
	 * Waits for a given view.
	 * 
	 * @param view the view to wait for
	 * @param timeout the amount of time in milliseconds to wait
	 * @param scroll {@code true} if scrolling should be performed
	 * @param checkIsShown {@code true} if view.isShown() should be used
	 * @return {@code true} if view is shown and {@code false} if it is not shown before the timeout
	 */

	public boolean waitForView(View view, int timeout, boolean scroll, boolean checkIsShown){
		// 参数为null直接返回false
		if(view == null)
			return false;
		// 设置超时时间点
		long endTime = SystemClock.uptimeMillis() + timeout;
		// 未到超时时间点，条件未满足，继续检查
		while (SystemClock.uptimeMillis() < endTime) {
			// 等待500ms
			sleeper.sleep();
			// 检查view是否出现在当前屏幕
			final boolean foundAnyMatchingView = searcher.searchFor(view);
			// 出现返回true
			if (foundAnyMatchingView){
				return true;
			}
			// 还未出现检查现在是否正好被刷新了，通过isShown() 为true 说明检查点时没渲染出来，这个点正好刷新出来了
			else if(checkIsShown && view != null && view.isShown()){
				return true;
			}
			// 设置了拖动，则调用拖动方法刷新可拖动控件
			if(scroll) 
				scroller.scrollDown();
		}
		// 条件未满足返回false
		return false;
	}
	
	/**
	 * 获取指定id，指定数量的view出现，可设置超时时间,如果超时时间设置为0,则默认改成10s,设置为负值则直接返回null
	 * Waits for a certain view.
	 * 
	 * @param view the id of the view to wait for
	 * @param index the index of the {@link View}. {@code 0} if only one is available
	 * @param timeout the timeout in milliseconds
	 * @return the specified View
	 */

	public View waitForView(int id, int index, int timeout){
		// 如果超时时间设置为0,则默认修改为10s
		if(timeout == 0){
			timeout = Timeout.getSmallTimeout();
		}
		// 指定id,数量，超时，不拖动
		return waitForView(id, index, timeout, false);
	}

	/**
	 * 获取指定id指定index的view出现.可设置超时和是否可拖动刷新
	 * Waits for a certain view.
	 * 
	 * @param view the id of the view to wait for
	 * @param index the index of the {@link View}. {@code 0} if only one is available
	 * @return the specified View
	 */

	public View waitForView(int id, int index, int timeout, boolean scroll){
		// 临时views缓存
		Set<View> uniqueViewsMatchingId = new HashSet<View>();
		// 设置超时时间点
		long endTime = SystemClock.uptimeMillis() + timeout;
		// 条件未满足，未达到超时时间点，继续检查
		while (SystemClock.uptimeMillis() <= endTime) {
			// 等待500ms
			sleeper.sleep();
			// 遍历当前所有的view
			for (View view : viewFetcher.getAllViews(false)) {
				// 检查id,符合条件加入views缓存
				Integer idOfView = Integer.valueOf(view.getId());
				
				if (idOfView.equals(id)) {
					uniqueViewsMatchingId.add(view);
					// 已找到需求的index,返回当前的view
					if(uniqueViewsMatchingId.size() > index) {
						return view;
					}
				}
			}
			// 如果设置了拖动，调用拖动方法刷新控件内容
			if(scroll) 
				scroller.scrollDown();
		}
		// 未满足条件，返回false
		return null;
	}

	/**
	 * 按照给定的By条件，查找满足条件的第minimumNumberOfMatches个WebElement,可设置超时时间和是否需要拖动滚动条刷新WebView内容
	 * Waits for a web element.
	 * 
	 * @param by the By object. Examples are By.id("id") and By.name("name")
	 * @param minimumNumberOfMatches the minimum number of matches that are expected to be shown. {@code 0} means any number of matches
	 * @param timeout the the amount of time in milliseconds to wait 
	 * @param scroll {@code true} if scrolling should be performed 
	 */

	public WebElement waitForWebElement(final By by, int minimumNumberOfMatches, int timeout, boolean scroll){
		// 设置超时时间点
		final long endTime = SystemClock.uptimeMillis() + timeout;

		while (true) {	
			// 检查是否已超时
			final boolean timedOut = SystemClock.uptimeMillis() > endTime;
			// 已超时记录异常日志，返回null
			if (timedOut){
				searcher.logMatchesFound(by.getValue());
				return null;
			}
			// 等待500ms
			sleeper.sleep();
			// 获取满足条件的WebElement
			WebElement webElementToReturn = searcher.searchForWebElement(by, minimumNumberOfMatches); 
			// 得到对应的WebElement则返回
			if(webElementToReturn != null)
				return webElementToReturn;
			// 设置了可拖动，则拖动刷新WebView可见内容
			if(scroll) {
				scroller.scrollDown();
			}
		}
	}


	/**
	 * 设置自定义的判定条件做等待,可设置超时时间
	 * Waits for a condition to be satisfied.
	 * 
	 * @param condition the condition to wait for
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if condition is satisfied and {@code false} if it is not satisfied before the timeout
	 */
	public boolean waitForCondition(Condition condition, int timeout){
		// 设置超时时间点
		final long endTime = SystemClock.uptimeMillis() + timeout;

		while (true) {
			// 检查是否已超时
			final boolean timedOut = SystemClock.uptimeMillis() > endTime;
			// 已超时，直接返回false
			if (timedOut){
				return false;
			}
			// 等待500ms
			sleeper.sleep();
			// 条件满足返回true ,为满足则继续检测
			if (condition.isSatisfied()){
				return true;
			}
		}
	}

	/**
	 * 获取指定text的TextView类型元素出现.默认超时20s,超时时间内未出现返回null,出现则返回对应的TextView
	 * Waits for a text to be shown. Default timeout is 20 seconds.
	 *
	 * @param text the text that needs to be shown, specified as a regular expression
	 * @return {@code true} if text is found and {@code false} if it is not found before the timeout
	 */

	public TextView waitForText(String text) {
		// 指定text,找到的第一个，超时20，需要拖动
		return waitForText(text, 0, Timeout.getLargeTimeout(), true);
	}

	/**
	 * 获取指定 text的第expectedMinimumNumberOfMatches个 TextView出现并返回该TextView,可设置超时时间，如设置时间内未出现返回null
	 * Waits for a text to be shown.
	 *
	 * @param text the text that needs to be shown, specified as a regular expression
	 * @param expectedMinimumNumberOfMatches the minimum number of matches of text that must be shown. {@code 0} means any number of matches
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if text is found and {@code false} if it is not found before the timeout
	 */

	public TextView waitForText(String text, int expectedMinimumNumberOfMatches, long timeout)
	{	
		// 指定text.第expectedMinimumNumberOfMatches个，超时时间，可拖动
		return waitForText(text, expectedMinimumNumberOfMatches, timeout, true);
	}

	/**
	 * 获取指定text的第expectedMinimumNumberOfMatches个TextView，可设置超时，是否需要滑动.如果出现则返回该TextView,未出现则返回null
	 * Waits for a text to be shown.
	 *
	 * @param text the text that needs to be shown, specified as a regular expression
	 * @param expectedMinimumNumberOfMatches the minimum number of matches of text that must be shown. {@code 0} means any number of matches
	 * @param timeout the amount of time in milliseconds to wait
	 * @param scroll {@code true} if scrolling should be performed
	 * @return {@code true} if text is found and {@code false} if it is not found before the timeout
	 */

	public TextView waitForText(String text, int expectedMinimumNumberOfMatches, long timeout, boolean scroll) {
		// 设置类型为TextView text index 超时时间，scroll 全部元素 
		return waitForText(TextView.class, text, expectedMinimumNumberOfMatches, timeout, scroll, false, true);	
	}
	
	/**
	 * 获取指定class类型，指定text内容的第expectedMinimumNumberOfMatches个 TextView
	 * 可设置超时时间，是否可以拖动
	 * Waits for a text to be shown.
	 *
	 * @param classToFilterBy the class to filter by
	 * @param text the text that needs to be shown, specified as a regular expression
	 * @param expectedMinimumNumberOfMatches the minimum number of matches of text that must be shown. {@code 0} means any number of matches
	 * @param timeout the amount of time in milliseconds to wait
	 * @param scroll {@code true} if scrolling should be performed
	 * @return {@code true} if text is found and {@code false} if it is not found before the timeout
	 */

	public <T extends TextView> T waitForText(Class<T> classToFilterBy, String text, int expectedMinimumNumberOfMatches, long timeout, boolean scroll) {
		// 默认配置超时后执行完才退出
		return waitForText(classToFilterBy, text, expectedMinimumNumberOfMatches, timeout, scroll, false, true);	
	}

	/**
	 * 获取指定text的第expectedMinimumNumberOfMatches个TextView,可指定超时时间，是否需要拖动
	 * 是否过滤非可见view,是否需要超时后立马退出
	 * Waits for a text to be shown.
	 *
	 * @param text the text that needs to be shown, specified as a regular expression.
	 * @param expectedMinimumNumberOfMatches the minimum number of matches of text that must be shown. {@code 0} means any number of matches
	 * @param timeout the amount of time in milliseconds to wait
	 * @param scroll {@code true} if scrolling should be performed
	 * @param onlyVisible {@code true} if only visible text views should be waited for
	 * @param hardStoppage {@code true} if search is to be stopped when timeout expires
	 * @return {@code true} if text is found and {@code false} if it is not found before the timeout
	 */
	
	public TextView waitForText(String text, int expectedMinimumNumberOfMatches, long timeout, boolean scroll, boolean onlyVisible, boolean hardStoppage) {
		return waitForText(TextView.class, text, expectedMinimumNumberOfMatches, timeout, scroll, onlyVisible, hardStoppage);
	}

	/**
	 * 获取指定clas类型和text的第expectedMinimumNumberOfMatches个view.
	 * classToFilterBy                  指定的class类型
	 * text                             指定的text内容
	 * expectedMinimumNumberOfMatches   view的index
	 * timeout                          超时时间，单位 ms
	 * scroll                           true对于可拖动控件拖动刷新，false 不拖动刷新
	 * onlyVisible                      true 过滤掉非可见的,false  不做过滤
	 * hardStoppage                     true 等所有操作完成后返回,false 超时后强制停止相关操作，立即返回
	 * Waits for a text to be shown.
	 *
	 * @param classToFilterBy the class to filter by
	 * @param text the text that needs to be shown, specified as a regular expression.
	 * @param expectedMinimumNumberOfMatches the minimum number of matches of text that must be shown. {@code 0} means any number of matches
	 * @param timeout the amount of time in milliseconds to wait
	 * @param scroll {@code true} if scrolling should be performed
	 * @param onlyVisible {@code true} if only visible text views should be waited for
	 * @param hardStoppage {@code true} if search is to be stopped when timeout expires
	 * @return {@code true} if text is found and {@code false} if it is not found before the timeout
	 */

	public <T extends TextView> T waitForText(Class<T> classToFilterBy, String text, int expectedMinimumNumberOfMatches, long timeout, boolean scroll, boolean onlyVisible, boolean hardStoppage) {
		// 设置超时时间点
		final long endTime = SystemClock.uptimeMillis() + timeout;

		while (true) {
			// 检查是否超时
			final boolean timedOut = SystemClock.uptimeMillis() > endTime;
			// 超时则返回null
			if (timedOut){
				return null;
			}
			// 等待500ms
			sleeper.sleep();
			// true  searcher方法调用中循环，直到超时退出，false  searcher方法中不循环执行只做一次判断
			if(!hardStoppage)
				timeout = 0;

			final T textViewToReturn = searcher.searchFor(classToFilterBy, text, expectedMinimumNumberOfMatches, timeout, scroll, onlyVisible);

			if (textViewToReturn != null ){
				return textViewToReturn;
			}
		}
	}

	/**
	 * 获取指定class类型的第index个View,默认超时10s,10s内未找到返回null
	 * Waits for and returns a View.
	 * 
	 * @param index the index of the view
	 * @param classToFilterby the class to filter
	 * @return the specified View
	 */

	public <T extends View> T waitForAndGetView(int index, Class<T> classToFilterBy){
		// 设置超时时间点，当前时间+10s
		long endTime = SystemClock.uptimeMillis() + Timeout.getSmallTimeout();
		// 未超时，且指定查找条件还未到达，则继续查找
		while (SystemClock.uptimeMillis() <= endTime && !waitForView(classToFilterBy, index, true, true));
		// 获取找到的view总数
		int numberOfUniqueViews = searcher.getNumberOfUniqueViews();
		// 按照指定的 class类型获取所有的可见view
		ArrayList<T> views = RobotiumUtils.removeInvisibleViews(viewFetcher.getCurrentViews(classToFilterBy));
		// 当前获取的views 数量少于唯一的数量,index做调整
		if(views.size() < numberOfUniqueViews){
			int newIndex = index - (numberOfUniqueViews - views.size());
			if(newIndex >= 0)
				index = newIndex;
		}

		T view = null;
		try{
			// 获取对应的view
			view = views.get(index);
		}catch (IndexOutOfBoundsException exception) {
			// 获取异常记录异常日志
			int match = index + 1;
			if(match > 1) {
				Assert.fail(match + " " + classToFilterBy.getSimpleName() +"s" + " are not found!");
			}
			else {
				Assert.fail(classToFilterBy.getSimpleName() + " is not found!");
			}
		}
		// 释放对象
		views = null;
		return view;
	}

	/**
	 * 等待指定tag id的Fragment.可设置超时时间
	 * 优先查找android.support.v4.app.Fragment
	 * 为找到再查找 android.app.Fragment
	 * 都未找到返回null
	 * Waits for a Fragment with a given tag or id to appear.
	 * 
	 * @param tag the name of the tag or null if no tag	
	 * @param id the id of the tag
	 * @param timeout the amount of time in milliseconds to wait
	 * @return true if fragment appears and false if it does not appear before the timeout
	 */

	public boolean waitForFragment(String tag, int id, int timeout){
		// 设置超时时间
		long endTime = SystemClock.uptimeMillis() + timeout;
		while (SystemClock.uptimeMillis() <= endTime) {
			// 查找 android.support.v4.app.Fragment ，找到返回 android.support.v4.app.Fragment ,未找到继续查找 android.app.Fragment
			if(getSupportFragment(tag, id) != null)
				return true;
			// 查找 android.app.Fragment
			if(getFragment(tag, id) != null)
				return true;
		}
		return false;
	}

	/**
	 * 获取指定tag和id对应的android.support.v4.app.Fragment
	 * Returns a SupportFragment with a given tag or id.
	 * 
	 * @param tag the tag of the SupportFragment or null if no tag
	 * @param id the id of the SupportFragment
	 * @return a SupportFragment with a given tag or id
	 */

	private Fragment getSupportFragment(String tag, int id){
		FragmentActivity fragmentActivity = null;

		try{
			// 获取当前 activity转化成 FragmentActivity类型
			fragmentActivity = (FragmentActivity) activityUtils.getCurrentActivity(false);
		}catch (ClassCastException ignored) {}
		// 获取条件对应的 Fragment
		if(fragmentActivity != null){
			try{
				if(tag == null)
					return fragmentActivity.getSupportFragmentManager().findFragmentById(id);
				else
					return fragmentActivity.getSupportFragmentManager().findFragmentByTag(tag);
			}catch (NoSuchMethodError ignored) {}
		}
		// 未找到对应的,返回null
		return null;
	}

	/**
	 * 指定的日志信息是否在指定超时时间内打印
	 * logMessage   期望出现的日志信息
	 * timeout      超时时间，单位 ms
	 * Waits for a log message to appear.
	 * Requires read logs permission (android.permission.READ_LOGS) in AndroidManifest.xml of the application under test.
	 * 
	 * @param logMessage the log message to wait for
	 * @param timeout the amount of time in milliseconds to wait
	 * @return true if log message appears and false if it does not appear before the timeout
	 */

	public boolean waitForLogMessage(String logMessage, int timeout){
		StringBuilder stringBuilder = new StringBuilder();
		// 设置超时时间点
		long endTime = SystemClock.uptimeMillis() + timeout;
		while (SystemClock.uptimeMillis() <= endTime) {
			// 读取logcat内容检查指定内容是否出现
			if(getLog(stringBuilder).lastIndexOf(logMessage) != -1){
				return true;
			}
			// 等待500ms
			sleeper.sleep();
		}
		// 指定时间内未找到，返回false
		return false;
	}

	/**
	 * 获取当前logcat 
	 * Returns the log in the given stringBuilder. 
	 * 
	 * @param stringBuilder the StringBuilder object to return the log in
	 * @return the log
	 */

	private StringBuilder getLog(StringBuilder stringBuilder) {
		Process p = null;
		BufferedReader reader = null;
		String line = null;  

		try {
            // read output from logcat 执行logcat -d 获取一堆logcat
			p = Runtime.getRuntime().exec("logcat -d");
			reader = new BufferedReader(  
					new InputStreamReader(p.getInputStream())); 

			stringBuilder.setLength(0);
			while ((line = reader.readLine()) != null) {  
				stringBuilder.append(line); 
			}
            reader.close();
            
            // read error from logcat,检查命令执行是否报错了
            StringBuilder errorLog = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(
                    p.getErrorStream()));
            errorLog.append("logcat returns error: ");
            while ((line = reader.readLine()) != null) {
                errorLog.append(line);
            }
            reader.close();

            // Exception would be thrown if we get exitValue without waiting for the process
            // to finish
            p.waitFor();

            // if exit value of logcat is non-zero, it means error
            if (p.exitValue() != 0) {
                destroy(p, reader);

                throw new Exception(errorLog.toString());
            }

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
		// 执行完毕销毁进程关闭reader
		destroy(p, reader);
		return stringBuilder;
	}

	/**
	 * 清空logcat缓存
	 * Clears the log.
	 */

	public void clearLog(){
		Process p = null;
		try {
			// 调用logcat -c 清空logcat缓存
			p = Runtime.getRuntime().exec("logcat -c");
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * 销毁进程，并关闭reader
	 * p      需要销毁的进程
	 * reader 需要关闭的BufferedReader
	 * Destroys the process and closes the BufferedReader.
	 * 
	 * @param p the process to destroy
	 * @param reader the BufferedReader to close
	 */

	private void destroy(Process p, BufferedReader reader){
		p.destroy();
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查找指定tag id的 android.app.Fragment,未找到则返回null
	 * Returns a Fragment with a given tag or id.
	 * 
	 * @param tag the tag of the Fragment or null if no tag
	 * @param id the id of the Fragment
	 * @return a SupportFragment with a given tag or id
	 */

	private android.app.Fragment getFragment(String tag, int id){

		try{
			if(tag == null)
				return activityUtils.getCurrentActivity().getFragmentManager().findFragmentById(id);
			else
				return activityUtils.getCurrentActivity().getFragmentManager().findFragmentByTag(tag);
		}catch (NoSuchMethodError ignored) {}

		return null;
	}
}
