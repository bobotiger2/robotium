package com.robotium.solo;

import java.util.ArrayList;
import com.robotium.solo.Solo.Config;
import junit.framework.Assert;
import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ScrollView;


/**
 * 滚动条操作工具类
 * Contains scroll methods. Examples are scrollDown(), scrollUpList(),
 * scrollToSide().
 *
 * @author Renas Reda, renas.reda@robotium.com
 *
 */

class Scroller {
	// 向下
	public static final int DOWN = 0;
	// 向下
	public static final int UP = 1;
	// 左右 枚举
	public enum Side {LEFT, RIGHT}
	// 是否可以拖动
	private boolean canScroll = false;
	// Instrument对象
	private final Instrumentation inst;
	// Activity工具类
	private final ActivityUtils activityUtils;
	// View获取工具类
	private final ViewFetcher viewFetcher;
	// 延时工具类
	private final Sleeper sleeper;
	// Robotium属性配置类
	private final Config config;


	/**
	 * 构造函数
	 * Constructs this object.
	 *
	 * @param inst the {@code Instrumentation} instance
	 * @param activityUtils the {@code ActivityUtils} instance
	 * @param viewFetcher the {@code ViewFetcher} instance
	 * @param sleeper the {@code Sleeper} instance
	 */

	public Scroller(Config config, Instrumentation inst, ActivityUtils activityUtils, ViewFetcher viewFetcher, Sleeper sleeper) {
		this.config = config;
		this.inst = inst;
		this.activityUtils = activityUtils;
		this.viewFetcher = viewFetcher;
		this.sleeper = sleeper;
	}


	/**
	 * 按住并且拖动到指定位置
	 * fromX 起始X坐标
	 * toX   终点X坐标
	 * fromY 起始Y坐标
	 * toY   终点Y坐标
	 * stepCount 动作拆分成几步
	 * Simulate touching a specific location and dragging to a new location.
	 *
	 * This method was copied from {@code TouchUtils.java} in the Android Open Source Project, and modified here.
	 *
	 * @param fromX X coordinate of the initial touch, in screen coordinates
	 * @param toX Xcoordinate of the drag destination, in screen coordinates
	 * @param fromY X coordinate of the initial touch, in screen coordinates
	 * @param toY Y coordinate of the drag destination, in screen coordinates
	 * @param stepCount How many move steps to include in the drag
	 */

	public void drag(float fromX, float toX, float fromY, float toY,
			int stepCount) {
		// 获取当前系统时间，构造MontionEvent使用
		long downTime = SystemClock.uptimeMillis();
		// 获取当前系统时间，构造MontionEvent使用
		long eventTime = SystemClock.uptimeMillis();
		float y = fromY;
		float x = fromX;
		// 计算每次增加Y坐标量
		float yStep = (toY - fromY) / stepCount;
		// 计算每次增加X坐标量
		float xStep = (toX - fromX) / stepCount;
		// 构造MotionEvent,先按住
		MotionEvent event = MotionEvent.obtain(downTime, eventTime,MotionEvent.ACTION_DOWN, fromX, fromY, 0);
		try {
			// 通过Instrument发送按住事件
			inst.sendPointerSync(event);
			// 抓取可能出现的异常
		} catch (SecurityException ignored) {}
		// 按照设置的步数，发送Move事件
		for (int i = 0; i < stepCount; ++i) {
			y += yStep;
			x += xStep;
			eventTime = SystemClock.uptimeMillis();
			// 构造 MOVE事件
			event = MotionEvent.obtain(downTime, eventTime,MotionEvent.ACTION_MOVE, x, y, 0);
			try {
				// 通过Instrument发送按住事件
				inst.sendPointerSync(event);
				// 抓取可能出现的异常
			} catch (SecurityException ignored) {}
		}
		// 获取系统当前时间
		eventTime = SystemClock.uptimeMillis();
		// 构造松开事件
		event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP,toX, toY, 0);
		try {
			inst.sendPointerSync(event);
		} catch (SecurityException ignored) {}
	}


	/**
	 * 按照设定的方法拖动滚动条,已经处于顶部的，调用拖动到顶部无效
	 * view  带滚动条的View控件
	 * 
	 * Scrolls a ScrollView.
	 * direction 拖动方向 0 滚动条向上拉动,1滚动条向下拉动
	 * @param direction the direction to be scrolled
	 * @return {@code true} if scrolling occurred, false if it did not
	 */

	private boolean scrollScrollView(final ScrollView view, int direction){
		// null 检查，比较传入null参数引发异常
		if(view == null){
			return false;
		}
		// 获取控件的高度
		int height = view.getHeight();
		// 高度减小一个像素
		height--;
		int scrollTo = -1;
		// 向上拉动，设置成滚动条的高度,拉到顶部
		if (direction == DOWN) {
			scrollTo = height;
		}
		// 向下拉动，设置成负值,拉到底部
		else if (direction == UP) {
			scrollTo = -height;
		}
		// 获取当前滚动的高度位置
		int originalY = view.getScrollY();
		final int scrollAmount = scrollTo;
		inst.runOnMainSync(new Runnable(){
			public void run(){
				view.scrollBy(0, scrollAmount);
			}
		});
		// 滚动条坐标未变化，标识本次拖动动作失败.已经处于顶端了，触发无效果
		if (originalY == view.getScrollY()) {
			return false;
		}
		else{
			return true;
		}
	}

	/**
	 * 滚动条滑到底部或者顶部，已经处于顶部，调用该方法拖动到顶部将引发死循环
	 * Scrolls a ScrollView to top or bottom.
	 *
	 * @param direction the direction to be scrolled
	 */

	private void scrollScrollViewAllTheWay(final ScrollView view, final int direction) {
		while(scrollScrollView(view, direction));
	}

	/**
	 * 拖动到顶部或者底部,0拖动到顶部,1拖动到底部
	 * Scrolls up or down.
	 *
	 * @param direction the direction in which to scroll
	 * @return {@code true} if more scrolling can be done
	 */

	public boolean scroll(int direction) {
		return scroll(direction, false);
	}

	/**
	 * 拖动到顶部
	 * Scrolls down.
	 *
	 * @return {@code true} if more scrolling can be done
	 */

	public boolean scrollDown() {
		// 如果配置设置了禁止拖动，那么将不拖动控件
		if(!config.shouldScroll) {
			return false;
		}
		// 拖动到顶部
		return scroll(Scroller.DOWN);
	}

	/**
	 * 拖动当前页面的可拖动控件
	 * direction  0拖动到顶部,1拖动到底部
	 * Scrolls up and down.
	 *
	 * @param direction the direction in which to scroll
	 * @param allTheWay <code>true</code> if the view should be scrolled to the beginning or end,
	 *                  <code>false</code> to scroll one page up or down.
	 * @return {@code true} if more scrolling can be done
	 */

	public boolean scroll(int direction, boolean allTheWay) {
		// 获取所有的Clicker可操作Views
		final ArrayList<View> viewList = RobotiumUtils.
				removeInvisibleViews(viewFetcher.getAllViews(true));
		// 获取所有可以拖动操作的views
		@SuppressWarnings("unchecked")
		ArrayList<View> views = RobotiumUtils.filterViewsToSet(new Class[] { ListView.class,
				ScrollView.class, GridView.class, WebView.class}, viewList);
		// 获取所有可视view中的最新的，即当前用户选中的可拖动控件
		View view = viewFetcher.getFreshestView(views);
		// 如果无可拖动控件，则返回
		if (view == null)
		{
			return false;
		}
		// 是一个列表控件，则使用列表控件方法操作
		if (view instanceof AbsListView) {
			return scrollList((AbsListView)view, direction, allTheWay);
		}
		// 如果是一个可拖动控件，则按照可拖动控件方法操作
		if (view instanceof ScrollView) {
			if (allTheWay) {
				scrollScrollViewAllTheWay((ScrollView) view, direction);
				return false;
			} else {
				return scrollScrollView((ScrollView)view, direction);
			}
		}
		// 如果是一个WebView控件，则按照WebView方法操作
		if(view instanceof WebView){
			return scrollWebView((WebView)view, direction, allTheWay);
		}
		// 非上述控件类型，返回false
		return false;
	}
	
	/**
	 * WebView 控件 拖动操作.
	 * Scrolls a WebView.
	 * 
	 * webView   传入的WebView
	 * direction 操作方向，0拖动到顶部，1拖动到底部
	 * allTheWay  true标识拖动到底部或顶部，false标识不拖动
	 * 事件发送成功返回true 失败返回false
	 * @param webView the WebView to scroll
	 * @param direction the direction to scroll
	 * @param allTheWay {@code true} to scroll the view all the way up or down, {@code false} to scroll one page up or down                          or down.
	 * @return {@code true} if more scrolling can be done
	 */
	
	public boolean scrollWebView(final WebView webView, int direction, final boolean allTheWay){

		if (direction == DOWN) {
			// 调用Instrument发送拖动事件
			inst.runOnMainSync(new Runnable(){
				public void run(){
					// 拖动到底部
					canScroll =  webView.pageDown(allTheWay);
				}
			});
		}
		if(direction == UP){
			// 调用Instrument发送拖动事件
			inst.runOnMainSync(new Runnable(){
				public void run(){
					// 拖动到底部
					canScroll =  webView.pageUp(allTheWay);
				}
			});
		}
		// 返回事件发送是否成功
		return canScroll;
	}

	/**
	 * 拖动一个列表
	 * Scrolls a list.
	 * absListView AbsListView类型的，即列表类控件
	 * direction   拖动方向0最顶部，1最底部
	 * @param absListView the list to be scrolled
	 * @param direction the direction to be scrolled
	 * @param allTheWay {@code true} to scroll the view all the way up or down, {@code false} to scroll one page up or down
	 * @return {@code true} if more scrolling can be done
	 */

	public <T extends AbsListView> boolean scrollList(T absListView, int direction, boolean allTheWay) {
		// 非null校验
		if(absListView == null){
			return false;
		}
		// 拖动到底部
		if (direction == DOWN) {
			// 如果是直接拖动到底部的模式
			if (allTheWay) {
				// 拖动到最大号的行,因总数据数，会大于可视行数，因此调用此方法，永久返回false
				scrollListToLine(absListView, absListView.getCount()-1);
				return false;
			}
			// 当总行数比可见行数大时.拖动到可见行数底部，返回false.
			if (absListView.getLastVisiblePosition() >= absListView.getCount()-1) {
				scrollListToLine(absListView, absListView.getLastVisiblePosition());
				return false;
			}
			// 当不是一行时，拖动到最下面的行
			if(absListView.getFirstVisiblePosition() != absListView.getLastVisiblePosition())
				scrollListToLine(absListView, absListView.getLastVisiblePosition());

			else
				// 当可见的只有一行时，拖动到下面一行
				scrollListToLine(absListView, absListView.getFirstVisiblePosition()+1);
			// 拖动到顶部
		} else if (direction == UP) {
			// 可见行数少于1行时，直接划到第0行
			if (allTheWay || absListView.getFirstVisiblePosition() < 2) {
				scrollListToLine(absListView, 0);
				return false;
			}
			// 计算显示的行数.没必要设置成final,又不是子类中使用
			final int lines = absListView.getLastVisiblePosition() - absListView.getFirstVisiblePosition();
			// 计算未显示的剩余行数全部显示多余的行
			int lineToScrollTo = absListView.getFirstVisiblePosition() - lines;
			// 如果正好可以显示行数与当前底部位置一致,则移动到当前位置
			if(lineToScrollTo == absListView.getLastVisiblePosition())
				lineToScrollTo--;
			// 如果计算位置为负值，那么直接滑到顶部
			if(lineToScrollTo < 0)
				lineToScrollTo = 0;

			scrollListToLine(absListView, lineToScrollTo);
		}
		sleeper.sleep();
		return true;
	}


	/**
	 * 拖动列表内容到指定的行
	 * line 对应的行号
	 * Scroll the list to a given line
	 *
	 * @param view the {@link AbsListView} to scroll
	 * @param line the line to scroll to
	 */

	public <T extends AbsListView> void scrollListToLine(final T view, final int line){
		// 非null校验
		if(view == null)
			Assert.fail("AbsListView is null!");

		final int lineToMoveTo;
		// 如果是gridview类型的，带标题，因此行数+1
		if(view instanceof GridView)
			lineToMoveTo = line+1;
		else
			lineToMoveTo = line;
		// 发送拖动事件
		inst.runOnMainSync(new Runnable(){
			public void run(){
				view.setSelection(lineToMoveTo);
			}
		});
	}


	/**
	 * 横向拖动,拖动默认拆分成40步操作
	 * side           指定拖动方向
	 * scrollPosition 拖动百分比0-1.
	 * Scrolls horizontally.
	 *
	 * @param side the side to which to scroll; {@link Side#RIGHT} or {@link Side#LEFT}
	 * @param scrollPosition the position to scroll to, from 0 to 1 where 1 is all the way. Example is: 0.55.
	 */

	@SuppressWarnings("deprecation")
	public void scrollToSide(Side side, float scrollPosition) {
		// 获取屏幕高度
		int screenHeight = activityUtils.getCurrentActivity().getWindowManager().getDefaultDisplay()
				.getHeight();
		// 获取屏幕宽度
		int screenWidth = activityUtils.getCurrentActivity(false).getWindowManager().getDefaultDisplay()
				.getWidth();
		// 按照宽度计算总距离
		float x = screenWidth * scrollPosition;
		// 拖动选择屏幕正中间
		float y = screenHeight / 2.0f;
		//往左拖动
		if (side == Side.LEFT)
			drag(0, x, y, y, 40);
		// 往右拖动
		else if (side == Side.RIGHT)
			drag(x, 0, y, y, 40);
	}

	/**
	 * 对给定控件进行向左或向右拖动操作.默认拖动距离拆分成40步
	 * view  需要拖动操作的控件
	 * side  拖动方向
	 * scrollPosition 拖动距离，按照屏幕宽度百分比计算，值为0-1
	 * Scrolls view horizontally.
	 *
	 * @param view the view to scroll
	 * @param side the side to which to scroll; {@link Side#RIGHT} or {@link Side#LEFT}
	 * @param scrollPosition the position to scroll to, from 0 to 1 where 1 is all the way. Example is: 0.55.
	 */

	public void scrollViewToSide(View view, Side side, float scrollPosition) {
		// 临时变量，存储控件在手机屏幕中的相对坐标
		int[] corners = new int[2];
		// 获取相对坐标
		view.getLocationOnScreen(corners);
		// 获取高度相对坐标
		int viewHeight = view.getHeight();
		// 获取宽度相对坐标
		int viewWidth = view.getWidth();
		// 计算拖动开始x坐标
		float x = corners[0] + viewWidth * scrollPosition;
		// 计算拖动开始y坐标
		float y = corners[1] + viewHeight / 2.0f;
		// 往左拖动
		if (side == Side.LEFT)
			drag(corners[0], x, y, y, 40);
		// 往右拖动
		else if (side == Side.RIGHT)
			drag(x, corners[0], y, y, 40);
	}

}
