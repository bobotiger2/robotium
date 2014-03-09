package com.robotium.solo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * View获取操作工具类，提供大量操作获取view的方法
 * Contains view methods. Examples are getViews(),
 * getCurrentTextViews(), getCurrentImageViews().
 *
 * @author Renas Reda, renas.reda@robotium.com
 *
 */

class ViewFetcher {
	// activity工具类
	private final ActivityUtils activityUtils;
	// 存储windowManager管理类的名字
	private String windowManagerString;

	/**
	 * 构造函数，初始化ViewFetcher对象
	 * Constructs this object.
	 *
	 * @param activityUtils the {@code ActivityUtils} instance
	 *
	 */

	public ViewFetcher(ActivityUtils activityUtils) {
		this.activityUtils = activityUtils;
		// 检查系统版本，初始化WindowManager对象的属性名
		setWindowManagerString();
	}


	/**
	 * 获取view类的mParent属性
	 * Returns the absolute top parent {@code View} in for a given {@code View}.
	 *
	 * @param view the {@code View} whose top parent is requested
	 * @return the top parent {@code View}
	 */

	public View getTopParent(View view) {
		// 如果获取的mParent非空，而且mParent是View的实例，那么继续迭代直到为空或者非View的实例
		if (view.getParent() != null
				&& view.getParent() instanceof android.view.View) {
			return getTopParent((View) view.getParent());
		} else {
			return view;
		}
	}


	/**
	 * 返回列表或者滚动条的mParent属性,即View的宿主容器
	 * 一般为AbsListView ScrollView WebView
	 * 主要为了确定控件类型
	 * Returns the scroll or list parent view
	 *
	 * @param view the view who's parent should be returned
	 * @return the parent scroll view, list view or null
	 */

	public View getScrollOrListParent(View view) {
		// view不是继承自 AbsListView ScrollView WebView 则继续迭代
	    if (!(view instanceof android.widget.AbsListView) && !(view instanceof android.widget.ScrollView) && !(view instanceof WebView)) {
	        try{
	            return getScrollOrListParent((View) view.getParent());
	        }catch(Exception e){
	            return null;
	        }
	    } else {
	        return view;
	    }
	}

	/**
	 * 获取当前界面上的所有非装饰类View对象
	 * onlySufficientlyVisible 为true则过滤所有的不可见对象，为false则不可见对象也返回
	 * Returns views from the shown DecorViews.
	 *
	 * @param onlySufficientlyVisible if only sufficiently visible views should be returned
	 * @return all the views contained in the DecorViews
	 */

	public ArrayList<View> getAllViews(boolean onlySufficientlyVisible) {
		// 获取当前界面对应的mViews属性
		final View[] views = getWindowDecorViews();
		// 构造 View数组，一般用List<View>
		final ArrayList<View> allViews = new ArrayList<View>();
		// views数组中过滤掉DecorView
		final View[] nonDecorViews = getNonDecorViews(views);
		View view = null;
		// 获取所有非DecorView包含的View对象
		if(nonDecorViews != null){
			for(int i = 0; i < nonDecorViews.length; i++){
				view = nonDecorViews[i];
				try {
					// 遍历获取所有的 View
					addChildren(allViews, (ViewGroup)view, onlySufficientlyVisible);
				} catch (Exception ignored) {}
				if(view != null) allViews.add(view);
			}
		}
		// 获取所有的DecorView包含的View
		if (views != null && views.length > 0) {
			// 获取最近选中的View
			view = getRecentDecorView(views);
			try {
				// 遍历获取所有的View
				addChildren(allViews, (ViewGroup)view, onlySufficientlyVisible);
			} catch (Exception ignored) {}

			if(view != null) allViews.add(view);
		}

		return allViews;
	}

	/**
	 * 过滤出views中的 DecorView对象
	 * Returns the most recent DecorView
	 *
	 * @param views the views to check
	 * @return the most recent DecorView
	 */

	 public final View getRecentDecorView(View[] views) {
		 if(views == null)
			 return null;
		 
		 final View[] decorViews = new View[views.length];
		 int i = 0;
		 View view;

		 for (int j = 0; j < views.length; j++) {
			 view = views[j];
			 // 获取 DecorView对象
			 if (view != null && view.getClass().getName()
					 .equals("com.android.internal.policy.impl.PhoneWindow$DecorView")) {
				 decorViews[i] = view;
				 i++;
			 }
		 }
		 return getRecentContainer(decorViews);
	 }

	/**
	 * 获取当前的焦点View
	 * Returns the most recent view container
	 *
	 * @param views the views to check
	 * @return the most recent view container
	 */

	 private final View getRecentContainer(View[] views) {
		 View container = null;
		 long drawingTime = 0;
		 View view;

		 for(int i = 0; i < views.length; i++){
			 view = views[i];
			 // 按照控件是否选中和绘制时间判断是否最新的
			 if (view != null && view.isShown() && view.hasWindowFocus() && view.getDrawingTime() > drawingTime) {
				 // 更改临时变量值
				 container = view;
				 // 更改临时变量值
				 drawingTime = view.getDrawingTime();
			 }
		 }
		 return container;
	 }

	 /**
	  * 过滤所有的非装饰类View
	  * Returns all views that are non DecorViews
	  *
	  * @param views the views to check
	  * @return the non DecorViews
	  */

	 private final View[] getNonDecorViews(View[] views) {
		 View[] decorViews = null;

		 if(views != null) {
			 decorViews = new View[views.length];

			 int i = 0;
			 View view;

			 for (int j = 0; j < views.length; j++) {
				 view = views[j];
				 // 类名不是DecorView的则加入返回数组
				 if (view != null && !(view.getClass().getName()
						 .equals("com.android.internal.policy.impl.PhoneWindow$DecorView"))) {
					 decorViews[i] = view;
					 i++;
				 }
			 }
		 }
		 return decorViews;
	 }



	/**
	 * 获取给定View中的所有包含的View包含parent
	 * parent 为空则默认返回当前界面所有的View
	 * onlySufficientlyVisible 为true则返回所有可被Clicker点击的View,为false则不进行过滤全部返回
	 * Extracts all {@code View}s located in the currently active {@code Activity}, recursively.
	 *
	 * @param parent the {@code View} whose children should be returned, or {@code null} for all
	 * @param onlySufficientlyVisible if only sufficiently visible views should be returned
	 * @return all {@code View}s located in the currently active {@code Activity}, never {@code null}
	 */

	public ArrayList<View> getViews(View parent, boolean onlySufficientlyVisible) {
		final ArrayList<View> views = new ArrayList<View>();
		final View parentToUse;
		// 传入的view为空，则按照当前界面操作
		if (parent == null){
			return getAllViews(onlySufficientlyVisible);
		}else{
			parentToUse = parent;
			// 先把自己添加了
			views.add(parentToUse);
			// 如果传入是ViewGroup类型的，那么遍历所有的View
			if (parentToUse instanceof ViewGroup) {
				addChildren(views, (ViewGroup) parentToUse, onlySufficientlyVisible);
			}
		}
		return views;
	}

	/**
	 * 遍历ViewGroup中的所有View
	 * onlySufficientlyVisible 为true则返回所有的使用 Clicker可以点击的view,为false则返回所有遍历到的View
	 * Adds all children of {@code viewGroup} (recursively) into {@code views}.
	 *
	 * @param views an {@code ArrayList} of {@code View}s
	 * @param viewGroup the {@code ViewGroup} to extract children from
	 * @param onlySufficientlyVisible if only sufficiently visible views should be returned
	 */

	private void addChildren(ArrayList<View> views, ViewGroup viewGroup, boolean onlySufficientlyVisible) {
		if(viewGroup != null){
			// 遍历ViewGroup
			for (int i = 0; i < viewGroup.getChildCount(); i++) {
				final View child = viewGroup.getChildAt(i);
				// 添加所有Clicker可点击对象
				if(onlySufficientlyVisible && isViewSufficientlyShown(child))
					views.add(child);
				// 不关注view是否可以通过Clicker点击，全部获取
				else if(!onlySufficientlyVisible)
					views.add(child);
				// 如果包含ViewGroup进行迭代遍历
				if (child instanceof ViewGroup) {
					addChildren(views, (ViewGroup) child, onlySufficientlyVisible);
				}
			}
		}
	}

	/**
	 * 如果View是可见的，那么返回true,否则返回false
	 * 滑动容器或者列表容易以可见面积大于等于控件面积的1/2做判断
	 * 因 Click方法是点击View的正中心位置，因此该位置不可见会导致无法点击
	 * Returns true if the view is sufficiently shown
	 *
	 * @param view the view to check
	 * @return true if the view is sufficiently shown
	 */

	public final boolean isViewSufficientlyShown(View view){
		// 存储View的xy坐标
		final int[] xyView = new int[2];
		// 存储View容器的xy坐标
		final int[] xyParent = new int[2];

		if(view == null)
			return false;
		// 获取View的高度，按照高度判断是否可见
		final float viewHeight = view.getHeight();
		// 获取 View的父容器
		final View parent = getScrollOrListParent(view);
		// 获取 view的XY坐标
		view.getLocationOnScreen(xyView);
		// 如果无宿主容器，那么坐标是0
		if(parent == null){
			xyParent[1] = 0;
		}
		// 有宿主容器，则获取宿主容器xy坐标
		else{
			parent.getLocationOnScreen(xyParent);
		}
		// 如果view在容器中可见内容小于容易总面积的一般，那么判定为不可见，分为高度的上限和下限判断
		if(xyView[1] + (viewHeight/2.0f) > getScrollListWindowHeight(view))
			return false;

		else if(xyView[1] + (viewHeight/2.0f) < xyParent[1])
			return false;

		return true;
	}

	/**
	 * 获取可滑动容器或者列表容器的高度坐标
	 * Returns the height of the scroll or list view parent
	 * @param view the view who's parents height should be returned
	 * @return the height of the scroll or list view parent
	 */

	@SuppressWarnings("deprecation")
	public float getScrollListWindowHeight(View view) {
		final int[] xyParent = new int[2];
		// 获取容器的宿主容器
		View parent = getScrollOrListParent(view);
		final float windowHeight;
		// 如果无宿主容器，那么直接获取当前Activity的高度
		if(parent == null){
			windowHeight = activityUtils.getCurrentActivity(false).getWindowManager()
			.getDefaultDisplay().getHeight();
		}
		// 否则高度为宿主容器+当前容器的高度
		else{
			parent.getLocationOnScreen(xyParent);
			windowHeight = xyParent[1] + parent.getHeight();
		}
		// 释放对象
		parent = null;
		return windowHeight;
	}


	/**
	 * 按照给定的过滤类型获取所有改类型的View
	 * classToFilterBy 过滤类
	 * Returns an {@code ArrayList} of {@code View}s of the specified {@code Class} located in the current
	 * {@code Activity}.
	 *
	 * @param classToFilterBy return all instances of this class, e.g. {@code Button.class} or {@code GridView.class}
	 * @return an {@code ArrayList} of {@code View}s of the specified {@code Class} located in the current {@code Activity}
	 */

	public <T extends View> ArrayList<T> getCurrentViews(Class<T> classToFilterBy) {
		return getCurrentViews(classToFilterBy, null);
	}

	/**
	 * 按照给定类型的class,返回View中对应的View
	 * Returns an {@code ArrayList} of {@code View}s of the specified {@code Class} located under the specified {@code parent}.
	 *
	 * @param classToFilterBy return all instances of this class, e.g. {@code Button.class} or {@code GridView.class}
	 * @param parent the parent {@code View} for where to start the traversal
	 * @return an {@code ArrayList} of {@code View}s of the specified {@code Class} located under the specified {@code parent}
	 */

	public <T extends View> ArrayList<T> getCurrentViews(Class<T> classToFilterBy, View parent) {
		ArrayList<T> filteredViews = new ArrayList<T>();
		List<View> allViews = getViews(parent, true);
		for(View view : allViews){
			// 按照class类型做过滤,并做类型转换
			if (view != null && classToFilterBy.isAssignableFrom(view.getClass())) {
				filteredViews.add(classToFilterBy.cast(view));
			}
		}
		// 释放对象
		allViews = null;
		return filteredViews;
	}

	
	/**
	 * 返回给定views中的最新可见View
	 * Tries to guess which view is the most likely to be interesting. Returns
	 * the most recently drawn view, which presumably will be the one that the
	 * user was most recently interacting with.
	 *
	 * @param views A list of potentially interesting views, likely a collection
	 *            of views from a set of types, such as [{@link Button},
	 *            {@link TextView}] or [{@link ScrollView}, {@link ListView}]
	 * @param index the index of the view
	 * @return most recently drawn view, or null if no views were passed 
	 */

	public final <T extends View> T getFreshestView(ArrayList<T> views){
		// 临时变量存储xy坐标
		final int[] locationOnScreen = new int[2];
		T viewToReturn = null;
		long drawingTime = 0;
		if(views == null){
			return null;
		}
		for(T view : views){
			// 获取xy坐标
			view.getLocationOnScreen(locationOnScreen);

			if (locationOnScreen[0] < 0 ) 
				continue;
			// 遍历找出最新的
			if(view.getDrawingTime() > drawingTime && view.getHeight() > 0){
				drawingTime = view.getDrawingTime();
				viewToReturn = view;
			}
		}
		views = null;
		return viewToReturn;
	}
	// WindowManager对象，提供大量的app界面view对象获取方法
	private static Class<?> windowManager;
	static{
		try {
			String windowManagerClassName;
			// 按照Android版本，判断对应的类名
			if (android.os.Build.VERSION.SDK_INT >= 17) {
				windowManagerClassName = "android.view.WindowManagerGlobal";
			} else {
				windowManagerClassName = "android.view.WindowManagerImpl"; 
			}
			// 通过反射获取类对象
 			windowManager = Class.forName(windowManagerClassName);

		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取当前界面的所有装饰器类
	 * Returns the WindorDecorViews shown on the screen.
	 * 
	 * @return the WindorDecorViews shown on the screen
	 */

	@SuppressWarnings("unchecked")
	public View[] getWindowDecorViews()
	{

		Field viewsField;
		Field instanceField;
		try {
			// 反射获取mViews属性
			viewsField = windowManager.getDeclaredField("mViews");
			// 反射获取WindowMager属性
			instanceField = windowManager.getDeclaredField(windowManagerString);
			// 修改属性声明，改成可访问
			viewsField.setAccessible(true);
			// 修改属性声明，改成可访问
			instanceField.setAccessible(true);
			// 反射获取windowManager对象
			Object instance = instanceField.get(null);
			View[] result;
			if (android.os.Build.VERSION.SDK_INT >= 19) {
				// 获取mViews属性内容,即View[]
				result = ((ArrayList<View>) viewsField.get(instance)).toArray(new View[0]);
			} else {
				// 获取mViews属性内容,即View[]
				result = (View[]) viewsField.get(instance);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 判断当前Android版本对应的WindowManager对象字段名
	 * Sets the window manager string.
	 */
	private void setWindowManagerString(){

		if (android.os.Build.VERSION.SDK_INT >= 17) {
			windowManagerString = "sDefaultWindowManager";
			
		} else if(android.os.Build.VERSION.SDK_INT >= 13) {
			windowManagerString = "sWindowManager";

		} else {
			windowManagerString = "mWindowManager";
		}
	}


}