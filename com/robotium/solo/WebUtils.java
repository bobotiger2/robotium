package com.robotium.solo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import com.robotium.solo.Solo.Config;
import android.app.Instrumentation;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;


/**
 * Contains web related methods. Examples are:
 * enterTextIntoWebElement(), getWebTexts(), getWebElements().
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class WebUtils {
	// View操作工具类
	private ViewFetcher viewFetcher;
	// Instrument,用于各种事件发送
	private Instrumentation inst;
	// Activity操作工具类
	private ActivityUtils activityUtils;
	// Robotium定制的WebClient
	RobotiumWebClient robotiumWebCLient;
	// WebElement构造工具方法
	WebElementCreator webElementCreator;
	// 原生WebChromeClient 保留，不需要Robotium修改的使用原生的执行
	WebChromeClient originalWebChromeClient = null;
	// 配置文件
	private Config config;


	/**
	 * 构造函数
	 * Constructs this object.
	 * 
	 * @param config the {@code Config} instance
	 * @param instrumentation the {@code Instrumentation} instance
	 * @param activityUtils the {@code ActivityUtils} instance
	 * @param viewFetcher the {@code ViewFetcher} instance
	 */

	public WebUtils(Config config, Instrumentation instrumentation, ActivityUtils activityUtils, ViewFetcher viewFetcher, Sleeper sleeper){
		this.config = config;
		this.inst = instrumentation;
		this.activityUtils = activityUtils;
		this.viewFetcher = viewFetcher;
		webElementCreator = new WebElementCreator(sleeper);
		robotiumWebCLient = new RobotiumWebClient(instrumentation, webElementCreator);
	}

	/**
	 * 调用RoBotiumWeb.js获取所有的Text的WebElement,使用NodeFilter.SHOW_TEXT过滤
	 * Returns {@code TextView} objects based on web elements shown in the present WebViews
	 * 
	 * @param onlyFromVisibleWebViews true if only from visible WebViews
	 * @return an {@code ArrayList} of {@code TextViews}s created from the present {@code WebView}s 
	 */

	public ArrayList<TextView> getTextViewsFromWebView(){
		// true标识执行完成，false标识未执行成功
		boolean javaScriptWasExecuted = executeJavaScriptFunction("allTexts();");	
		// WebElement转换成TextView
		return createAndReturnTextViewsFromWebElements(javaScriptWasExecuted);	
	}

	/**
	 * WebElement转换成 TextView，javaScriptWasExecuted 为true则执行转换，false不执行转换
	 * 
	 * Creates and returns TextView objects based on WebElements
	 * 
	 * @return an ArrayList with TextViews
	 */

	private ArrayList <TextView> createAndReturnTextViewsFromWebElements(boolean javaScriptWasExecuted){
		ArrayList<TextView> webElementsAsTextViews = new ArrayList<TextView>();
		// js脚本执行成功，则遍历所有获取到的WebElement信息，并转换成TextView对象
		if(javaScriptWasExecuted){
			// 编译所有的WebElement
			for(WebElement webElement : webElementCreator.getWebElementsFromWebViews()){
				// 可见控件转换成TextView对象
				if(isWebElementSufficientlyShown(webElement)){
					// 转换成TextView对象
					RobotiumTextView textView = new RobotiumTextView(inst.getContext(), webElement.getText(), webElement.getLocationX(), webElement.getLocationY());
					// 添加到返回列表
					webElementsAsTextViews.add(textView);
				}
			}	
		}
		return webElementsAsTextViews;		
	}

	/**
	 * 获取当前WebView中的所有WebElements
	 * Returns an ArrayList of WebElements currently shown in the active WebView.
	 * 
	 * @return an {@code ArrayList} of the {@link WebElement} objects currently shown in the active WebView
	 */

	public ArrayList<WebElement> getCurrentWebElements(){
		// 执行获取所所有 WebElement的JavaScript脚本
		boolean javaScriptWasExecuted = executeJavaScriptFunction("allWebElements();");
		// 过滤掉非可见WebElement,返回所有剩余的
		return getSufficientlyShownWebElements(javaScriptWasExecuted);
	}

	/**
	 * 获取By参数指定属性的所有WebElement
	 * Returns an ArrayList of WebElements of the specified By object currently shown in the active WebView.
	 * 
	 * @param by the By object. Examples are By.id("id") and By.name("name")
	 * @return an {@code ArrayList} of the {@link WebElement} objects currently shown in the active WebView 
	 */

	public ArrayList<WebElement> getCurrentWebElements(final By by){
		// 获取By属性对应的所有WebElement
		boolean javaScriptWasExecuted = executeJavaScript(by, false);
		// 该判断目前还没使用,2条路径相同业务逻辑
		if(config.useJavaScriptToClickWebElements){
			if(!javaScriptWasExecuted){
				return new ArrayList<WebElement>();
			}
			return webElementCreator.getWebElementsFromWebViews();
		}
		// 过滤掉非可见WebElement,返回所有剩余的
		return getSufficientlyShownWebElements(javaScriptWasExecuted);
	}

	/**
	 * 过滤掉非可见WebElement,返回所有剩余的
	 * Returns the sufficiently shown WebElements
	 * 
	 * @return the sufficiently shown WebElements
	 */

	private ArrayList<WebElement> getSufficientlyShownWebElements(boolean javaScriptWasExecuted){
		ArrayList<WebElement> currentWebElements = new ArrayList<WebElement>();
		// 检查 JavaScript是否执行成功
		if(javaScriptWasExecuted){
			for(WebElement webElement : webElementCreator.getWebElementsFromWebViews()){
				if(isWebElementSufficientlyShown(webElement)){
					currentWebElements.add(webElement);
				}
			}
		}
		return currentWebElements;
	}

	/**
	 * 构造JavaScript执行环境,并返回构造好的JavaScript
	 * Prepares for start of JavaScript execution
	 * 
	 * @return the JavaScript as a String
	 */

	private String prepareForStartOfJavascriptExecution(){
		// 初始化WebElement存储容器
		webElementCreator.prepareForStart();
		// 获取当前版本Android对应的WebChromeClient
		WebChromeClient currentWebChromeClient = getCurrentWebChromeClient();
		// 保存原有的WebChromeClient
		if(currentWebChromeClient != null && !currentWebChromeClient.getClass().isAssignableFrom(RobotiumWebClient.class)){
			originalWebChromeClient = getCurrentWebChromeClient();	
		}
		// 初始化 Robotium定制版本的WebChromeClient
		robotiumWebCLient.enableJavascriptAndSetRobotiumWebClient(viewFetcher.getCurrentViews(WebView.class), originalWebChromeClient);
		// 返回读取到的RobotiumWeb.js中的内容
		return getJavaScriptAsString();
	}
	
	/**
	 * 获取当前的Android版本对应的WebChromeClient
	 * Returns the current WebChromeClient through reflection
	 * 
	 * @return the current WebChromeClient
	 * 
	 */

	private WebChromeClient getCurrentWebChromeClient(){
		WebChromeClient currentWebChromeClient = null;
		// 获取当前最新的WebView
		Object currentWebView = viewFetcher.getFreshestView(viewFetcher.getCurrentViews(WebView.class));
		// 高版本才用反射获取
		if (android.os.Build.VERSION.SDK_INT >= 16) {
			try{
				currentWebView = new Reflect(currentWebView).field("mProvider").out(Object.class);
			}catch(IllegalArgumentException ignored) {}
		}

		try{
			// 反射获取相关对象
			Object mCallbackProxy = new Reflect(currentWebView).field("mCallbackProxy").out(Object.class);
			// 获取属性并转化成WebChromeClient对象
			currentWebChromeClient = new Reflect(mCallbackProxy).field("mWebChromeClient").out(WebChromeClient.class);
		}catch(Exception ignored){}

		return currentWebChromeClient;
	}

	/**
	 * 对指定条件的WebElement输入文本
	 * Enters text into a web element using the given By method
	 * 
	 * @param by the By object e.g. By.id("id");
	 * @param text the text to enter
	 */

	public void enterTextIntoWebElement(final By by, final String text){
		// 按照 Id查找WebElement对象输入
		if(by instanceof By.Id){
			executeJavaScriptFunction("enterTextById(\""+by.getValue()+"\", \""+text+"\");");
		}
		// 按照 Xpath查找WebElement对象输入
		else if(by instanceof By.Xpath){
			executeJavaScriptFunction("enterTextByXpath(\""+by.getValue()+"\", \""+text+"\");");
		}
		// 按照 CssSelector查找WebElement对象输入
		else if(by instanceof By.CssSelector){
			executeJavaScriptFunction("enterTextByCssSelector(\""+by.getValue()+"\", \""+text+"\");");
		}
		// 按照 Name查找WebElement对象输入
		else if(by instanceof By.Name){
			executeJavaScriptFunction("enterTextByName(\""+by.getValue()+"\", \""+text+"\");");
		}
		// 按照 ClassName查找WebElement对象输入
		else if(by instanceof By.ClassName){
			executeJavaScriptFunction("enterTextByClassName(\""+by.getValue()+"\", \""+text+"\");");
		}
		// 按照 Text查找WebElement对象输入
		else if(by instanceof By.Text){
			executeJavaScriptFunction("enterTextByTextContent(\""+by.getValue()+"\", \""+text+"\");");
		}
		// 按照 TagName查找WebElement对象输入
		else if(by instanceof By.TagName){
			executeJavaScriptFunction("enterTextByTagName(\""+by.getValue()+"\", \""+text+"\");");
		}
	}

	/**
	 * 运行JavaScript.按照by类型对相应的 WebElement进行操作
	 * shouldClick 为true标识点击对应的WebElement,否则获取响应的Element信息
	 * 返回true标识执行成，false执行异常
	 * Executes JavaScript determined by the given By object
	 * 
	 * @param by the By object e.g. By.id("id");
	 * @param shouldClick true if click should be performed
	 * @return true if JavaScript function was executed
	 */

	public boolean executeJavaScript(final By by, boolean shouldClick){
		// 拼接按照Id执行的JavaScript脚本
		if(by instanceof By.Id){
			return executeJavaScriptFunction("id(\""+by.getValue()+"\", \"" + String.valueOf(shouldClick) + "\");");
		}
		// 拼接按照Xpath执行的JavaScript脚本
		else if(by instanceof By.Xpath){
			return executeJavaScriptFunction("xpath(\""+by.getValue()+"\", \"" + String.valueOf(shouldClick) + "\");");
		}
		// 拼接按照CssSelector执行的JavaScript脚本
		else if(by instanceof By.CssSelector){
			return executeJavaScriptFunction("cssSelector(\""+by.getValue()+"\", \"" + String.valueOf(shouldClick) + "\");");
		}
		// 拼接按照Name执行的JavaScript脚本
		else if(by instanceof By.Name){
			return executeJavaScriptFunction("name(\""+by.getValue()+"\", \"" + String.valueOf(shouldClick) + "\");");
		}
		// 拼接按照ClassName执行的JavaScript脚本
		else if(by instanceof By.ClassName){
			return executeJavaScriptFunction("className(\""+by.getValue()+"\", \"" + String.valueOf(shouldClick) + "\");");
		}
		// 拼接按照Text执行的JavaScript脚本
		else if(by instanceof By.Text){
			return executeJavaScriptFunction("textContent(\""+by.getValue()+"\", \"" + String.valueOf(shouldClick) + "\");");
		}
		// 拼接按照TagName执行的JavaScript脚本
		else if(by instanceof By.TagName){
			return executeJavaScriptFunction("tagName(\""+by.getValue()+"\", \"" + String.valueOf(shouldClick) + "\");");
		}
		return false;
	}

	/**
	 * 在WebView中执行指定的Javascript.执行成功返回true,否则返回false
	 * Executes the given JavaScript function
	 * 
	 * @param function the function as a String
	 * @return true if JavaScript function was executed
	 */

	private boolean executeJavaScriptFunction(final String function){
		// 获取当前时刻最新的WebView
		final WebView webView = viewFetcher.getFreshestView(viewFetcher.getCurrentViews(WebView.class));
		// 非null检查
		if(webView == null){
			return false;
		}
		// 获取JavaScript资源文件，即RoboTiumWeb.js中的内容
		final String javaScript = prepareForStartOfJavascriptExecution();
		// WebView中加载相关JavaScript
		activityUtils.getCurrentActivity(false).runOnUiThread(new Runnable() {
			public void run() {
				if(webView != null){
					webView.loadUrl("javascript:" + javaScript + function);
				}
			}
		});
		return true;
	}

	/**
	 * 检查当前WebElement是否可见
	 * Returns true if the view is sufficiently shown
	 *
	 * @param view the view to check
	 * @return true if the view is sufficiently shown
	 */

	public final boolean isWebElementSufficientlyShown(WebElement webElement){
		// 获取当前最新的 WebView
		final WebView webView = viewFetcher.getFreshestView(viewFetcher.getCurrentViews(WebView.class));
		// 存储WebView XY坐标信息
		final int[] xyWebView = new int[2];

		if(webView != null && webElement != null){
			// 获取WebView XY坐标信息
			webView.getLocationOnScreen(xyWebView);
			//  WebElement在WebView外，则不可见
			if(xyWebView[1] + webView.getHeight() > webElement.getLocationY())
				return true;
		}
		return false;
	}
	
	/**
	 * 按照大写字母分割字符串，各字符串之间添加空格 ,并转换成小写
	 * Splits a name by upper case.
	 * 
	 * @param name the name to split
	 * @return a String with the split name
	 * 
	 */

	public String splitNameByUpperCase(String name) {
		String [] texts = name.split("(?=\\p{Upper})");
		StringBuilder stringToReturn = new StringBuilder();

		for(String string : texts){

			if(stringToReturn.length() > 0) {
				stringToReturn.append(" " + string.toLowerCase());
			}
			else {
				stringToReturn.append(string.toLowerCase());
			}
		}
		return stringToReturn.toString();
	}

	/**
	 * 加载Robotium.js文件
	 * 并加载样式信息,添加\n换行符
	 * Returns the JavaScript file RobotiumWeb.js as a String
	 *  
	 * @return the JavaScript file RobotiumWeb.js as a {@code String} 
	 */

	private String getJavaScriptAsString() {
		InputStream fis = getClass().getResourceAsStream("RobotiumWeb.js");
		StringBuffer javaScript = new StringBuffer();

		try {
			BufferedReader input =  new BufferedReader(new InputStreamReader(fis));
			String line = null;
			while (( line = input.readLine()) != null){
				javaScript.append(line);
				javaScript.append("\n");
			}
			input.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return javaScript.toString();
	}
}