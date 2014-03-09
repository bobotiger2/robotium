package com.robotium.solo;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import android.os.SystemClock;
import android.webkit.WebView;

/**
 * 将WebElement信息解析成WebElement对象
 * Contains TextView related methods. Examples are:
 * getTextViewsFromWebViews(), createTextViewAndAddInList().
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class WebElementCreator {
	// 存储WebElement
	private List<WebElement> webElements;
	// 延时工具类
	private Sleeper sleeper;
	// 标识符号,用于标识WebView内容解析是否已经完成
	private boolean isFinished = false;

	/**
	 * 构造函数
	 * Constructs this object.
	 * 
	 * @param sleeper the {@code Sleeper} instance
	 * 
	 */

	public WebElementCreator(Sleeper sleeper){
		this.sleeper = sleeper;
		// 创建一个存储实例，使用copyOnweite可以保证复制list时重新构造一份新的，对原有不造成影响
		webElements = new CopyOnWriteArrayList<WebElement>();
	}

	/**
	 * 初始化
	 * Prepares for start of creating {@code TextView} objects based on web elements 
	 */

	public void prepareForStart(){
		// 重置为false
		setFinished(false);
		// 清空已存储的WebElement
		webElements.clear();
	}

	/**
	 * 获取当前 WebView中的WebElement
	 * Returns an {@code ArrayList} of {@code TextView} objects based on the web elements shown
	 * 
	 * @return an {@code ArrayList} of {@code TextView} objects based on the web elements shown
	 */

	public ArrayList<WebElement> getWebElementsFromWebViews(){
		// 等待WebView元素被解析
		waitForWebElementsToBeCreated();
		// copy一份对象返回
		return new ArrayList<WebElement>(webElements);
	}

	/**
	 * 获取WebView内容解析状态
	 * true   解析已完成
	 * false  解析还未完成
	 * Returns true if all {@code TextView} objects based on web elements have been created
	 * 
	 * @return true if all {@code TextView} objects based on web elements have been created
	 */

	public boolean isFinished(){
		return isFinished;
	}


	/**
	 * 设置WebView解析是否完成状态
	 * true   已完成解析
	 * false  解析未完成
	 * Set to true if all {@code TextView} objects have been created
	 * 
	 * @param isFinished true if all {@code TextView} objects have been created
	 */

	public void setFinished(boolean isFinished){
		this.isFinished = isFinished;
	}

	/**
	 * 按照指定信息，获取WebView中的WebElement并加入到webElements中
	 * Creates a {@ WebElement} object from the given text and {@code WebView}
	 * 
	 * @param webData the data of the web element 
	 * @param webView the {@code WebView} the text is shown in
	 */

	public void createWebElementAndAddInList(String webData, WebView webView){
		// 获取WebElement
		WebElement webElement = createWebElementAndSetLocation(webData, webView);
		// 非空则加入WebElement列表
		if((webElement!=null)) 
			webElements.add(webElement);
	}

	/**
	 * 设置WebElement坐标属性
	 * webElement 需要设置的WebElement
	 * webView    WebElement所在的WebView
	 * 
	 * Sets the location of a {@code WebElement} 
	 * 
	 * @param webElement the {@code TextView} object to set location 
	 * @param webView the {@code WebView} the text is shown in
	 * @param x the x location to set
	 * @param y the y location to set
	 * @param width the width to set
	 * @param height the height to set
	 */

	private void setLocation(WebElement webElement, WebView webView, int x, int y, int width, int height ){
		// 获取页面缩放信息
		float scale = webView.getScale();
		// 储存屏幕坐标
		int[] locationOfWebViewXY = new int[2];
		// 获取WebView对应手机屏幕中的坐标
		webView.getLocationOnScreen(locationOfWebViewXY);
		// 计算可以点击的x坐标,取WebElement中间位置
		int locationX = (int) (locationOfWebViewXY[0] + (x + (Math.floor(width / 2))) * scale);
		// 计算可操作的 y坐标,取WebElement中间位置
		int locationY = (int) (locationOfWebViewXY[1] + (y + (Math.floor(height / 2))) * scale);

		webElement.setLocationX(locationX);
		webElement.setLocationY(locationY);
	}

	/**
	 * 按照给定信息获取WebView中对应的元素
	 * Creates a {@code WebView} object 
	 * 
	 * @param information the data of the web element
	 * @param webView the web view the text is shown in
	 * 
	 * @return a {@code WebElement} object with a given text and location
	 */

	private WebElement createWebElementAndSetLocation(String information, WebView webView){
		// 解析属性，按照;,划分
		String[] data = information.split(";,");
		String[] elements = null;
		int x = 0;
		int y = 0;
		int width = 0;
		int height = 0;
		Hashtable<String, String> attributes = new Hashtable<String, String>();
		try{
			// 解析对应的x坐标
			x = Math.round(Float.valueOf(data[5]));
			// 解析对应的y坐标
			y = Math.round(Float.valueOf(data[6]));
			// 解析宽度信息
			width = Math.round(Float.valueOf(data[7]));
			// 解析高度信息
			height = Math.round(Float.valueOf(data[8]));	
			// 解析剩余属性
			elements = data[9].split("\\#\\$");
		}catch(Exception ignored){}
		// 属性为key value格式,使用::分隔
		if(elements != null) {
			for (int index = 0; index < elements.length; index++){
				String[] element = elements[index].split("::");
				// 对于只有key的属性，key也作为value使用
				if (element.length > 1) {
					attributes.put(element[0], element[1]);
				} else {
					attributes.put(element[0], element[0]);
				}
			}
		}

		WebElement webElement = null;

		try{
			// 构造WebElement对象
			webElement = new WebElement(data[0], data[1], data[2], data[3], data[4], attributes);
			// 设置位置信息
			setLocation(webElement, webView, x, y, width, height);
		}catch(Exception ignored) {}

		return webElement;
	}

	/**
	 * 检查WebView内容解析是否完成,默认超时5s,
	 * 解析完成返回true,未完成返回false
	 * Waits for {@code WebElement} objects to be created
	 * 
	 * @return true if successfully created before timout
	 */

	private boolean waitForWebElementsToBeCreated(){
		// 5s延时
		final long endTime = SystemClock.uptimeMillis() + 5000;
		// 检查是否超时
		while(SystemClock.uptimeMillis() < endTime){
			// 已解析完成，返回true
			if(isFinished){
				return true;
			}
			// 等待300ms
			sleeper.sleepMini();
		}
		return false;
	}

}
