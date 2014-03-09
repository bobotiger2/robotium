package com.robotium.solo;

import java.util.List;
import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.os.Message;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;

/**
 * Robotium WebView操作工具类,扩展WebChromeClient
 * Robotium需要操作WebView,因此需要劫持WebView的 JS执行，重写onJsPrompt,获取WebView中相关元素
 * WebChromeClient used to get information on web elements by injections of JavaScript. 
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class RobotiumWebClient extends WebChromeClient{
	// 用于构造WebElement的工具类
	WebElementCreator webElementCreator;
	// Instrument,用于发送各种事件
	private Instrumentation inst;
	// robotium扩展的client
	private WebChromeClient robotiumWebClient;
	// 原生的client
	private WebChromeClient originalWebChromeClient = null;


	/**
	 * 构造函数
	 * Constructs this object.
	 *
	 * @param instrumentation the {@code Instrumentation} instance
	 * @param webElementCreator the {@code WebElementCreator} instance
	 */

	public RobotiumWebClient(Instrumentation inst, WebElementCreator webElementCreator){
		this.inst = inst;
		this.webElementCreator = webElementCreator;
		robotiumWebClient = this;
	}

	/**
	 * 设置WebView可执行javaScript,各种WebView操作都要靠JavaScript完成.
	 * Enables JavaScript in the given {@code WebViews} objects.
	 * 
	 * @param webViews the {@code WebView} objects to enable JavaScript in
	 */

	public void enableJavascriptAndSetRobotiumWebClient(List<WebView> webViews, WebChromeClient originalWebChromeClient){
		// 保留原有的ChromeClient.用作需要原生调用时使用
		this.originalWebChromeClient = originalWebChromeClient;

		for(final WebView webView : webViews){

			if(webView != null){ 
				inst.runOnMainSync(new Runnable() {
					public void run() {
						// 设置可执行js
						webView.getSettings().setJavaScriptEnabled(true);
						// 设置使用 Robotium定制的WebClient
						webView.setWebChromeClient(robotiumWebClient);

					}
				});
			}
		}
	}

	/**
	 * 重写js执行处理函数,robotium使用的通过js的prompt也解析所有元素信息,因此重写改方法
	 * Overrides onJsPrompt in order to create {@code WebElement} objects based on the web elements attributes prompted by the injections of JavaScript
	 */

	@Override
	public boolean onJsPrompt(WebView view, String url, String message,	String defaultValue, JsPromptResult r) {
		// 对于robotium执行的js进行特殊处理,解析js执行返回信息，并构造相关的WebElement信息
		if(message != null && (message.contains(";,") || message.contains("robotium-finished"))){
			// 执行完成则设置解析完毕
			if(message.equals("robotium-finished")){
				webElementCreator.setFinished(true);
			}
			else{
				webElementCreator.createWebElementAndAddInList(message, view);
			}
			// 直接确认掉，避免影响页面
			r.confirm();
			return true;
		}
		// 非robotium运行的js,使用默认逻辑处理
		else {
			if(originalWebChromeClient != null) {
				return originalWebChromeClient.onJsPrompt(view, url, message, defaultValue, r); 
			}
			return true;
		}

	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public Bitmap getDefaultVideoPoster() {
		if (originalWebChromeClient != null) {
			return originalWebChromeClient.getDefaultVideoPoster();
		} 
		return null;
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public View getVideoLoadingProgressView() {
		if (originalWebChromeClient != null) {
			return originalWebChromeClient.getVideoLoadingProgressView();
		} 
		return null;
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public void getVisitedHistory(ValueCallback<String[]> callback) {
		if (originalWebChromeClient != null) {
			originalWebChromeClient.getVisitedHistory(callback);
		} 
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public void onCloseWindow(WebView window) {
		if (originalWebChromeClient != null) {
			originalWebChromeClient.onCloseWindow(window);
		} 
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public void onConsoleMessage(String message, int lineNumber, String sourceID) {
		if (originalWebChromeClient != null) {
			originalWebChromeClient.onConsoleMessage(message, lineNumber, sourceID);
		}
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public boolean onConsoleMessage(ConsoleMessage consoleMessage) {       
		if (originalWebChromeClient != null) {
			return originalWebChromeClient.onConsoleMessage(consoleMessage);
		} 
		return true;
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
		if (originalWebChromeClient != null) {
			return originalWebChromeClient.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
		} 
		return true;
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota,
			long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater) {
		if (originalWebChromeClient != null) {
			originalWebChromeClient.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
		} 
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public void onGeolocationPermissionsHidePrompt() {
		if (originalWebChromeClient != null) {
			originalWebChromeClient.onGeolocationPermissionsHidePrompt();
		} 
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
		if (originalWebChromeClient != null) {
			originalWebChromeClient.onGeolocationPermissionsShowPrompt(origin, callback);
		} 
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public void onHideCustomView() {
		if (originalWebChromeClient != null) {
			originalWebChromeClient.onHideCustomView();
		} 
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
		if (originalWebChromeClient != null) {
			return originalWebChromeClient.onJsAlert(view, url, message, result);
		} 
		return true;
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
		if (originalWebChromeClient.onJsBeforeUnload(view, url, message, result)) {
			return originalWebChromeClient.onJsBeforeUnload(view, url, message, result);
		}
		return true;
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
		if (originalWebChromeClient != null) {
			return originalWebChromeClient.onJsConfirm(view, url, message, result);
		} 
		return true;
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public boolean onJsTimeout() {
		if (originalWebChromeClient != null) {
			return originalWebChromeClient.onJsTimeout();
		} 
		return true;
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		if (originalWebChromeClient != null) {            
			originalWebChromeClient.onProgressChanged(view, newProgress);
		} 
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
		if (originalWebChromeClient != null) {
			originalWebChromeClient.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
		} 
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public void onReceivedIcon(WebView view, Bitmap icon) {
		if (originalWebChromeClient != null) {
			originalWebChromeClient.onReceivedIcon(view, icon);
		} 
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public void onReceivedTitle(WebView view, String title) {
		if (originalWebChromeClient != null) {
			originalWebChromeClient.onReceivedTitle(view, title);
		} 
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
		if (originalWebChromeClient != null) {
			originalWebChromeClient.onReceivedTouchIconUrl(view, url, precomposed);
		} 
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public void onRequestFocus(WebView view) {
		if (originalWebChromeClient != null) {
			originalWebChromeClient.onRequestFocus(view);
		}
	}
	/**
	 * 重写方法，调用原生的
	 */
	@Override
	public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
		if (originalWebChromeClient != null) {
			originalWebChromeClient.onShowCustomView(view, callback);
		} 
	}
}
