package com.robotium.solo;

import junit.framework.Assert;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.view.View;
import android.widget.TextView;


/**
 * 按照指定条件获取View或者其他的一些信息
 * Contains various get methods. Examples are: getView(int id),
 * getView(Class<T> classToFilterBy, int index).
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class Getter {
	// Instrument,用于发送事件
	private final Instrumentation instrumentation;
	// activity工具类
	private final ActivityUtils activityUtils;
	// View等待工具类
	private final Waiter waiter;
	// 1s
	private final int TIMEOUT = 1000;

	/**
	 * 构造函数
	 * Constructs this object.
	 * 
	 * @param inst the {@code Instrumentation} instance
	 * @param viewFetcher the {@code ViewFetcher} instance
	 * @param waiter the {@code Waiter} instance
	 */

	public Getter(Instrumentation instrumentation, ActivityUtils activityUtils, Waiter waiter){
		this.instrumentation = instrumentation;
		this.activityUtils = activityUtils;
		this.waiter = waiter;
	}


	/**
	 * 获取指定类型的第index个View，找不到返回null
	 * Returns a {@code View} with a certain index, from the list of current {@code View}s of the specified type.
	 *
	 * @param classToFilterBy which {@code View}s to choose from
	 * @param index choose among all instances of this type, e.g. {@code Button.class} or {@code EditText.class}
	 * @return a {@code View} with a certain index, from the list of current {@code View}s of the specified type
	 */

	public <T extends View> T getView(Class<T> classToFilterBy, int index) {
		//获取指定class类型的第index个View,默认超时10s,10s内未找到返回null 
		return waiter.waitForAndGetView(index, classToFilterBy);
	}

	/**
	 * 获取指定class类型和text的第1个view,找不到提示异常.可设置是否只查找可见的
	 * onlyVisible true 只找可见的,false 查找所有的 
	 * Returns a {@code View} that shows a given text, from the list of current {@code View}s of the specified type.
	 *
	 * @param classToFilterBy which {@code View}s to choose from
	 * @param text the text that the view shows
	 * @param onlyVisible {@code true} if only visible texts on the screen should be returned
	 * @return a {@code View} showing a given text, from the list of current {@code View}s of the specified type
	 */

	public <T extends TextView> T getView(Class<T> classToFilterBy, String text, boolean onlyVisible) {
		// 获取指定class类型和text的第1个view,默认短超时
		T viewToReturn = (T) waiter.waitForText(classToFilterBy, text, 0, Timeout.getSmallTimeout(), false, onlyVisible, false);
		// 未找到提示异常
		if(viewToReturn == null)
			Assert.fail(classToFilterBy.getSimpleName() + " with text: '" + text + "' is not found!");

		return viewToReturn;
	}

	/**
	 * 按照指定资源id，获取当前activity中的 String
	 * Returns a localized string
	 * 
	 * @param id the resource ID for the string
	 * @return the localized string
	 */

	public String getString(int id)
	{
		// 获取当前activity
		Activity activity = activityUtils.getCurrentActivity(false);
		// 返回id对应对的string
		return activity.getString(id);
	}

	/**
	 * 按照指定资源id，获取当前activity的String.
     *
	 * Returns a localized string
	 * 
	 * @param id the resource ID for the string
	 * @return the localized string
	 */

	public String getString(String id)
	{
		// 将String类型的标识解析成对应的Int型Id再从当前activity查找对应的String
		// 获取 Context
		Context targetContext = instrumentation.getTargetContext(); 
		// 获取应用名
		String packageName = targetContext.getPackageName(); 
		// 按照String类型id查询对应的int id,现在当前应用中查，找不到，整个android中查
		int viewId = targetContext.getResources().getIdentifier(id, "string", packageName);
		if(viewId == 0){
			viewId = targetContext.getResources().getIdentifier(id, "string", "android");
		}
		// 按照指定资源id，获取当前activity中的 String
		return getString(viewId);		
	}
	
	/**
	 * 获取指定id的第index个 View，如设置的index小于1，那么返回当前activity中id为 0的view.
	 * 可设置超时时间,如果超时时间设置为0,则默认改成10s,设置为负值则直接返回null
	 *  
	 * Returns a {@code View} with a given id.
	 * 
	 * @param id the R.id of the {@code View} to be returned
	 * @param index the index of the {@link View}. {@code 0} if only one is available
	 * @param timeout the timeout in milliseconds
	 * @return a {@code View} with a given id
	 */

	public View getView(int id, int index, int timeout){
		// 获取当前activity
		final Activity activity = activityUtils.getCurrentActivity(false);
		View viewToReturn = null;
		// 传入index 小于1，默认返回当前activity中的id为0的view
		if(index < 1){
			index = 0;
			viewToReturn = activity.findViewById(id);
		}
		// 如果找到了，则返回，这块代码可以嵌入上面的if块中，较好理解
		if (viewToReturn != null) {
			return viewToReturn;
		}
		// 获取指定id，指定数量的view出现，可设置超时时间,如果超时时间设置为0,则默认改成10s,设置为负值则直接返回null 
		return waiter.waitForView(id, index, timeout);
	}

	/**
	 * 获取指定id的第index个 View，如设置的index小于1，那么返回当前activity中id为 0的view.默认超时10s
	 * Returns a {@code View} with a given id.
	 * 
	 * @param id the R.id of the {@code View} to be returned
	 * @param index the index of the {@link View}. {@code 0} if only one is available
	 * @return a {@code View} with a given id
	 */

	public View getView(int id, int index){
		return getView(id, index, 0);
	}

	/**
	 * 获取指定id的第index个 View，如设置的index小于1，那么返回当前activity中id为 0的view.默认超时1s
	 * 将String类型的标识解析成对应的Int型Id再从当前activity查找对应的View
	 * 
	 * Returns a {@code View} with a given id.
	 * 
	 * @param id the id of the {@link View} to return
	 * @param index the index of the {@link View}. {@code 0} if only one is available
	 * @return a {@code View} with a given id
	 */

	public View getView(String id, int index){
		// 将String类型的标识解析成对应的Int型Id 
		View viewToReturn = null;
		// 获取应用上下文
		Context targetContext = instrumentation.getTargetContext(); 
		// 获取应用名
		String packageName = targetContext.getPackageName(); 
		// 按照String类型id查询对应的int id,现在当前应用中查
		int viewId = targetContext.getResources().getIdentifier(id, "id", packageName);
		// 查询对应的view
		if(viewId != 0){
			viewToReturn = getView(viewId, index, TIMEOUT); 
		}
		// 如果未找到，将id解析成android对应的继续查找
		if(viewToReturn == null){
			int androidViewId = targetContext.getResources().getIdentifier(id, "id", "android");
			// 如果可以获取对应的id 继续查找
			if(androidViewId != 0){
				viewToReturn = getView(androidViewId, index, TIMEOUT);
			}
		}
		// 找到则直接返回
		if(viewToReturn != null){
			return viewToReturn;
		}
		// 未找到则设置id 为0继续查找
		return getView(viewId, index); 
	}
}
