package com.robotium.solo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


/**
 * 控件搜索工具类，可以按照各种关键信息查找对应控件
 * Contains various search methods. Examples are: searchForEditTextWithTimeout(),
 * searchForTextWithTimeout(), searchForButtonWithTimeout().
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class Searcher {
	// View 操作工具类
	private final ViewFetcher viewFetcher;
	// WebView操作工具类
	private final WebUtils webUtils;
	// 滑动控件操作工具类
	private final Scroller scroller;
	// 延时操作工具类
	private final Sleeper sleeper;
	// 日志打印标签，标识这是 Robotium
	private final String LOG_TAG = "Robotium";
	// 由于存储包含指定text正则的views,检查内容包括显示内容，错误提示信息和友好提示信息
	Set<TextView> uniqueTextViews;
	// WebElement缓存
	List<WebElement> webElements;
	// 统计非重复View数量
	private int numberOfUniqueViews;
	// 默认超时5s
	private final int TIMEOUT = 5000;


	/**
	 * 构造函数
	 * Constructs this object.
	 *
	 * @param viewFetcher the {@code ViewFetcher} instance
	 * @param webUtils the {@code WebUtils} instance
	 * @param scroller the {@code Scroller} instance
	 * @param sleeper the {@code Sleeper} instance.
	 */

	public Searcher(ViewFetcher viewFetcher, WebUtils webUtils, Scroller scroller, Sleeper sleeper) {
		this.viewFetcher = viewFetcher;
		this.webUtils = webUtils;
		this.scroller = scroller;
		this.sleeper = sleeper;
		webElements = new ArrayList<WebElement>();
		uniqueTextViews = new HashSet<TextView>();
	}


	/**
	 * 按照给定的查找条件，检查是否找到了期望的元素，找到返回true,未找到返回false
	 * viewClass       					希望的元素类型
	 * regex           					text正则
	 * expectedMinimumNumberOfMatches   期望符合该条件元素数量，数量相等返回true,不相等返回false
	 * scroll                           是否需要拖动查找，如列表未显示全部内容，拖动可以刷新内容
	 * onlyVisible                      true只查找可见的，false查找全部的
	 * Searches for a {@code View} with the given regex string and returns {@code true} if the
	 * searched {@code Button} is found a given number of times. Will automatically scroll when needed.
	 *
	 * @param viewClass what kind of {@code View} to search for, e.g. {@code Button.class} or {@code TextView.class}
	 * @param regex the text to search for. The parameter <strong>will</strong> be interpreted as a regular expression.
	 * @param expectedMinimumNumberOfMatches the minimum number of matches expected to be found. {@code 0} matches means that one or more
	 * matches are expected to be found
	 * @param scroll whether scrolling should be performed
	 * @param onlyVisible {@code true} if only texts visible on the screen should be searched
	 * 
	 * @return {@code true} if a {@code View} of the specified class with the given text is found a given number of
	 * times, and {@code false} if it is not found
	 */

	public boolean searchWithTimeoutFor(Class<? extends TextView> viewClass, String regex, int expectedMinimumNumberOfMatches, boolean scroll, boolean onlyVisible) {
		// 设定超时时间,当前时间加上5s
		final long endTime = SystemClock.uptimeMillis() + TIMEOUT;
		// 初始化临时变量为null
		TextView foundAnyMatchingView = null;
		// 如果还没到达指定时间还为找到则继续查找
		while (SystemClock.uptimeMillis() < endTime) {
			// 等500ms
			sleeper.sleep();
			// 按照给定的条件调用查询方法,超时设置为0
			foundAnyMatchingView = searchFor(viewClass, regex, expectedMinimumNumberOfMatches, 0, scroll, onlyVisible);
			// 找到则直接返回
			if (foundAnyMatchingView !=null){
				return true;
			}
		}
		return false;
	}


	/**
	 * 按照给定的条件查找TextView类型的View
	 * viewClass          				设置的class类型
	 * regex              				text正则表达式
	 * expectedMinimumNumberOfMatches   期望的View数量,如果总数不是期望的那么返回null,总数一致返回第expectedMinimumNumberOfMatches个
	 * timeout                          超时时间
	 * scroll							是否需要拖动查找，如列表未显示全部内容，拖动可以刷新内容
	 * onlyVisible                      true只查找可见的，false查找全部的
	 * Searches for a {@code View} with the given regex string and returns {@code true} if the
	 * searched {@code View} is found a given number of times.
	 *
	 * @param viewClass what kind of {@code View} to search for, e.g. {@code Button.class} or {@code TextView.class}
	 * @param regex the text to search for. The parameter <strong>will</strong> be interpreted as a regular expression.
	 * @param expectedMinimumNumberOfMatches the minimum number of matches expected to be found. {@code 0} matches means that one or more
	 * matches are expected to be found.
	 * @param timeout the amount of time in milliseconds to wait
	 * @param scroll whether scrolling should be performed
	 * @param onlyVisible {@code true} if only texts visible on the screen should be searched
	 * 
	 * @return {@code true} if a view of the specified class with the given text is found a given number of times.
	 * {@code false} if it is not found.
	 */

	public <T extends TextView> T searchFor(final Class<T> viewClass, final String regex, int expectedMinimumNumberOfMatches, final long timeout, final boolean scroll, final boolean onlyVisible) {
		// 如果设置的期望配匹次数小于1次，则默认配置为1次
		if(expectedMinimumNumberOfMatches < 1) {
			expectedMinimumNumberOfMatches = 1;
		}
		// 构造可在子线程中调用的集合类
		final Callable<Collection<T>> viewFetcherCallback = new Callable<Collection<T>>() {
			@SuppressWarnings("unchecked")
			public Collection<T> call() throws Exception {
				// 等待500ms
				sleeper.sleep();
				// 获取当前屏幕的所有views,类型为viewClass所指定的
				ArrayList<T> viewsToReturn = viewFetcher.getCurrentViews(viewClass);
				// 如果配置了只查找可见view中的内容，那么过滤掉所有非可见的
				if(onlyVisible){
					viewsToReturn = RobotiumUtils.removeInvisibleViews(viewsToReturn);
				}
				// 检查是否是TextView类型的,如果是查找TextView类型的，且当前屏幕内容包含WebView.那么也把WebView中的相关TextView类元素全部加入返回列表
				if(viewClass.isAssignableFrom(TextView.class)) {
					viewsToReturn.addAll((Collection<? extends T>) webUtils.getTextViewsFromWebView());
				}
				// 返回找到的views
				return viewsToReturn;
			}
		};

		try {
			// 查找相关view
			return searchFor(viewFetcherCallback, regex, expectedMinimumNumberOfMatches, timeout, scroll);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 对应class类型的View数量是否<=index
	 * 
	 * unqiueViews     给定的views
	 * viewClass       指定的view类型
	 * index           指定的数量从0开始计数
	 * Searches for a view class.
	 * 
	 * @param uniqueViews the set of unique views
	 * @param viewClass the view class to search for
	 * @param index the index of the view class
	 * @return true if view class if found a given number of times
	 */

	public <T extends View> boolean searchFor(Set<T> uniqueViews, Class<T> viewClass, final int index) {
		// 获取当前界面的所有给定 class类型的可见View
		ArrayList<T> allViews = RobotiumUtils.removeInvisibleViews(viewFetcher.getCurrentViews(viewClass));
		// 返回不重复的view数量，并把allViews加入到uniqueViews集合中
		int uniqueViewsFound = (getNumberOfUniqueViews(uniqueViews, allViews));
		// index位置在总数量中，返回true,可以获取
		if(uniqueViewsFound > 0 && index < uniqueViewsFound) {
			return true;
		}
		// 上面的条件满足,这句代码执行不到
		if(uniqueViewsFound > 0 && index == 0) {
			return true;
		}
		// index值大于等于总数，返回false,越界了
		return false;
	}

	/**
	 * 检查view是否在当前屏幕中.是返回true.不在返回false
	 * Searches for a given view.
	 * 
	 * @param view the view to search
	 * @param scroll true if scrolling should be performed
	 * @return true if view is found
	 */

	public <T extends View> boolean searchFor(View view) {
		// 获取当前屏幕中所有的可见view
		ArrayList<View> views = viewFetcher.getAllViews(true);
		for(View v : views){
			// 判断view是否在当前屏幕中,是返回true
			if(v.equals(view)){
				return true;
			}
		}
		// 不存在返回false
		return false;
	}

	/**
	 * 按照调用方法传入的class类型，text需要匹配的正则，第几个匹配的元素,查找符合条件的 View,找到的元素总数与期望的不一致，那么返回null,符合期望的，那么返回列表中的最后一个元素
	 * regex     					     字符串正则
	 * expectedMinimumNumberOfMatches 查找第几个
	 * timeout                        查找超时时间
	 * scroll                         是否需要滑动，如列表中的不可见元素，滑动一下就可见了，设置了true就可以找到    
	 * Searches for a {@code View} with the given regex string and returns {@code true} if the
	 * searched {@code View} is found a given number of times. Will not scroll, because the caller needs to find new
	 * {@code View}s to evaluate after scrolling, and call this method again.
	 *
	 * @param viewFetcherCallback callback which should return an updated collection of views to search
	 * @param regex the text to search for. The parameter <strong>will</strong> be interpreted as a regular expression.
	 * @param expectedMinimumNumberOfMatches the minimum number of matches expected to be found. {@code 0} matches means that one or more
	 * matches are expected to be found.
	 * @param timeout the amount of time in milliseconds to wait
	 * @param scroll whether scrolling should be performed
	 * 
	 * @return {@code true} if a view of the specified class with the given text is found a given number of times.
	 * {@code false} if it is not found.
	 *
	 * @throws Exception not really, it's just the signature of {@code Callable}
	 */

	public <T extends TextView> T searchFor(Callable<Collection<T>> viewFetcherCallback, String regex, int expectedMinimumNumberOfMatches, long timeout, boolean scroll) throws Exception {
		// 设置超时时间点
		final long endTime = SystemClock.uptimeMillis() + timeout;	
		Collection<T> views;

		while (true) {
			// 检查是否已过设定的超时点
			final boolean timedOut = timeout > 0 && SystemClock.uptimeMillis() > endTime;
			// 已经超时则直接退出查询，并打印相关日志记录
			if(timedOut){
				logMatchesFound(regex);
				return null;
			}
			// 获取给定条件过滤后的所有Views
			views = viewFetcherCallback.call();

			for(T view : views){
				// 检查是否找到了期望的数量，如果找到了期望数量的元素，那么清空缓存，返回找到的对应View
				if (RobotiumUtils.getNumberOfMatches(regex, view, uniqueTextViews) == expectedMinimumNumberOfMatches) {
					uniqueTextViews.clear();
					return view;
				}
			}
			// 如果配置了可拖动，但是当前不允许拖动，那么记录异常日志，返回null,由Config中配置是否可拖动，默认为true
			if(scroll && !scroller.scrollDown()){
				logMatchesFound(regex);
				return null; 
			}
			// 如果未设置可拖动，记录异常日志，返回null
			if(!scroll){
				logMatchesFound(regex);
				return null; 
			}
		}
	}

	/**
	 * 按照给定的By条件，查找 WebView中的WebElement,minimumNumberOfMatches 指定需要返回第几个
	 * Searches for a web element.
	 * 
	 * @param by the By object e.g. By.id("id");
	 * @param minimumNumberOfMatches the minimum number of matches that are expected to be shown. {@code 0} means any number of matches
	 * @return the web element or null if not found
	 */

	public WebElement searchForWebElement(final By by, int minimumNumberOfMatches){
		// 如果传入数量小于1,那么默认设置为1
		if(minimumNumberOfMatches < 1){
			minimumNumberOfMatches = 1;
		}
		// 使用by作为过滤条件.获取当前的所有WebElement
		List<WebElement> viewsFromScreen = webUtils.getCurrentWebElements(by);
		// viewsFromScreen中的元素合并到webElement中，并且去重,text，xy坐标一致作为重复判定条件
		addViewsToList (webElements, viewsFromScreen);
		// 返回指定的WebElement
		return getViewFromList(webElements, minimumNumberOfMatches);
	}

	/**
	 * 列表合并,webElementsOnScreen加入到allWebElements中
	 * 使用 text，xy坐标位置作为2个列表中元素是否重复判断条件
	 * Adds views to a given list.
	 * 
	 * @param allWebElements the list of all views
	 * @param webTextViewsOnScreen the list of views shown on screen
	 */

	private void addViewsToList(List<WebElement> allWebElements, List<WebElement> webElementsOnScreen){
		// 缓存allWebElements中元素的xy坐标
		int[] xyViewFromSet = new int[2];
		// 缓存webZElementOnScreen元素的位置xy坐标
		int[] xyViewFromScreen = new int[2];
		//  遍历
		for(WebElement textFromScreen : webElementsOnScreen){
			boolean foundView = false;
			// 获取屏幕xy坐标
			textFromScreen.getLocationOnScreen(xyViewFromScreen);

			for(WebElement textFromList : allWebElements){
				// 获取屏幕对应的xy坐标
				textFromList.getLocationOnScreen(xyViewFromSet);
				//  allWebElements中已存在的则不重复加入,按照text和xy坐标作为是否相等的条件
				if(textFromScreen.getText().equals(textFromList.getText()) && xyViewFromScreen[0] == xyViewFromSet[0] && xyViewFromScreen[1] == xyViewFromSet[1]) {
					foundView = true;
				}
			}
			// 不符合重复判断条件的加入allWebElements列表
			if(!foundView){
				allWebElements.add(textFromScreen);
			}
		}

	}

	/**
	 * 获取列表中指定位置的元素，数组越界则返回null
	 * Returns a text view with a given match.
	 * 
	 * @param webElements the list of views
	 * @param match the match of the view to return
	 * @return the view with a given match
	 */

	private WebElement getViewFromList(List<WebElement> webElements, int match){

		WebElement webElementToReturn = null;
		// 检查是否超过列表中WebElement总数,超过则返回null
		if(webElements.size() >= match){

			try{
				// 获取对应位置元素
				webElementToReturn = webElements.get(--match);
			}catch(Exception ignored){}
		}
		//找到元素则清空缓存
		if(webElementToReturn != null)
			webElements.clear();
		// 返回元素
		return webElementToReturn;
	}

	/**
	 * 把views中的View 加入到uniqueViews集合中，并且去重，返回unqiueViews中的View总数量
	 * Returns the number of unique views. 
	 * 
	 * @param uniqueViews the set of unique views
	 * @param views the list of all views
	 * @return number of unique views
	 */

	public <T extends View> int getNumberOfUniqueViews(Set<T>uniqueViews, ArrayList<T> views){
		// 把view加入set中,set类型保证了不会存在重复的view
		for(int i = 0; i < views.size(); i++){
			uniqueViews.add(views.get(i));
		}
		// 获取uniqueViews中的View总数
		numberOfUniqueViews = uniqueViews.size();
		return numberOfUniqueViews;
	}

	/**
	 * 获取unqiueViews中的View数量
	 * Returns the number of unique views.
	 * 
	 * @return the number of unique views
	 */

	public int getNumberOfUniqueViews(){
		return numberOfUniqueViews;
	}

	/**
	 * 打印搜索失败日志.并清空缓存内容
	 * Logs a (searchFor failed) message.
	 *  
	 * @param regex the search string to log
	 */

	public void logMatchesFound(String regex){
		// 打印当前TextView总数和搜索条件
		if (uniqueTextViews.size() > 0) {
			Log.d(LOG_TAG, " There are only " + uniqueTextViews.size() + " matches of '" + regex + "'");
		}
		// 打印当前WebView中的Element数量和搜索条件
		else if(webElements.size() > 0){
			Log.d(LOG_TAG, " There are only " + webElements.size() + " matches of '" + regex + "'");
		}
		// 清理缓存内容
		uniqueTextViews.clear();
		webElements.clear();
	}
}
