package com.robotium.solo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import junit.framework.Assert;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.IntentFilter;
import android.util.Log;
import android.view.KeyEvent;


/**
 * 用于Activity操作的工具类
 * Contains activity related methods. Examples are:
 * getCurrentActivity(), getActivityMonitor(), setActivityOrientation(int orientation).
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class ActivityUtils {
	// Instrument 各种事件发送强大利器
	private final Instrumentation inst;
	// activitymonitor 所有的activity变化都可以监控
	private ActivityMonitor activityMonitor;
	// 普通的 activity
	private Activity activity;
	// 用于做延时的,看了UIAutomator的代码，都用SystemClock.sleep()了，比这个看着优雅
	private final Sleeper sleeper;
	// 日志标签，log 日志输出会带上robotium的标签.标记框架是他们的
	private final String LOG_TAG = "Robotium";
	// 短等待时间100ms
	private final int MINISLEEP = 100;
	// 用于activitymonitor循环抓取当前activity的等待50ms
	private static final int ACTIVITYSYNCTIME = 50;
	// activity堆栈，用于存放所有开启状态的activity,采用WeakReference,避免对GC产生影响
	private Stack<WeakReference<Activity>> activityStack;
	// Activity对象引用变量，使用WeakReference，避免对GC产生影响
	private WeakReference<Activity> weakActivityReference;
	// 堆栈存储activity的名字
	private Stack<String> activitiesStoredInActivityStack;
	// 定时器，用于定时获取最新的activity,定时时间就是上面定义的50ms
	private Timer activitySyncTimer;
	/**
	 * 构造函数
	 * Constructs this object.
	 *
	 * @param inst the {@code Instrumentation} instance.  获取instrument一般都是通过getIntrument()获取的传递给构造函数
	 * @param activity the start {@code Activity}         应用启动的activity,一般是传递mainActivity
	 * @param sleeper the {@code Sleeper} instance        Sleep工具类
	 */

	public ActivityUtils(Instrumentation inst, Activity activity, Sleeper sleeper) {
		this.inst = inst;
		this.activity = activity;
		this.sleeper = sleeper;
		createStackAndPushStartActivity();
		activitySyncTimer = new Timer();
		activitiesStoredInActivityStack = new Stack<String>();
		// 开启 activity监控
		setupActivityMonitor();
		setupActivityStackListener();
	}



	/**
	 * 创建一个堆栈，用于存放创建的activity.因为 activity创建了新的老的就在后面了，所以使用堆栈的先进后出功能
	 * 
	 * Creates a new activity stack and pushes the start activity. 
	 */

	private void createStackAndPushStartActivity(){
		// 初始化一个堆栈
		activityStack = new Stack<WeakReference<Activity>>();
		// 如果构造函数传入的activity不为null，那么假如堆栈最为当前最新的activity 
		if (activity != null){
			WeakReference<Activity> weakReference = new WeakReference<Activity>(activity);
			activity = null;
			activityStack.push(weakReference);
		}
	}

	/**
	 * 返回所有处于打开或运行状态的activity,一般代码编写返回一个 List<Activity>较好
	 * Returns a {@code List} of all the opened/active activities.
	 * 
	 * @return a {@code List} of all the opened/active activities
	 */

	public ArrayList<Activity> getAllOpenedActivities()
	{
		// 构造一个 List 用于返回  activity数组
		ArrayList<Activity> activities = new ArrayList<Activity>();
		// 遍历activityStack堆栈中的所有activity 加如到List中
		Iterator<WeakReference<Activity>> activityStackIterator = activityStack.iterator();
		// 判断是否可以继续遍历
		while(activityStackIterator.hasNext()){
			// 获取当前activity,堆栈指针指向下个activity对象
			Activity  activity = activityStackIterator.next().get();
			// 判断activity对象非空，才加入，可能由于gc导致对象已经被回收，导致null异常
			if(activity!=null)
				activities.add(activity);
		}
		// 返回所有的当前存活activity
		return activities;
	}

	/**
	 * 通过instrument构造一个activityMonitor用于监控activity的创建
	 * This is were the activityMonitor is set up. The monitor will keep check
	 * for the currently active activity.
	 */

	private void setupActivityMonitor() {

		try {
			// 为了addMonitor方法需要，创建一个null对象
			IntentFilter filter = null;
			// 获取一个activityMonitor
			activityMonitor = inst.addMonitor(filter, null, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 通过定时任务不断刷新获取当前最新创建的activity,定时每50ms运行一次，因此存在一定的概率获取的不是最新的activity
	 * This is were the activityStack listener is set up. The listener will keep track of the
	 * opened activities and their positions.
	 */

	private void setupActivityStackListener() {
		// 创建一个定时任务
		TimerTask activitySyncTimerTask = new TimerTask() {
			@Override
			public void run() {
				// 检查activitymonitor是否已创建,避免null异常
				if (activityMonitor != null){
					// 获取当前最新的activity
					Activity activity = activityMonitor.getLastActivity();
					// 检查获取对象是否为null
					if (activity != null){
						// 如果该activity已经存储在activity堆栈中，则不进行重复添加
						if(!activitiesStoredInActivityStack.isEmpty() && activitiesStoredInActivityStack.peek().equals(activity.toString())){
							return;
						}	
						// 移除可能存在同名对象，避免堆栈加入脏数据
						if (activitiesStoredInActivityStack.remove(activity.toString())){
							removeActivityFromStack(activity);
						}
						// 确保activity还处于存活状态，并加入堆栈
						if (!activity.isFinishing()){
							addActivityToStack(activity);
						}
					}
				}
			}
		};
		// 开启定时任务，每50ms执行一次
		activitySyncTimer.schedule(activitySyncTimerTask, 0, ACTIVITYSYNCTIME);
	}

	/**
	 * 从activity堆栈中移除一个activity
	 * Removes a given activity from the activity stack
	 * 
	 * @param activity the activity to remove
	 */

	private void removeActivityFromStack(Activity activity){
		// 遍历整个堆栈
		Iterator<WeakReference<Activity>> activityStackIterator = activityStack.iterator();
		while(activityStackIterator.hasNext()){
			// 获取当前位置的activity
			Activity activityFromWeakReference = activityStackIterator.next().get();
			// 如果发现当前堆栈中存在 null对象，则移除之
			if(activityFromWeakReference == null){
				activityStackIterator.remove();
			}
			// 找对了对应的activity,则移除之
			if(activity!=null && activityFromWeakReference!=null && activityFromWeakReference.equals(activity)){
				activityStackIterator.remove();
			}
		}
	}

	/**
	 * 获取ActivityMonitor对象，一般这个也没啥用
	 * Returns the ActivityMonitor used by Robotium.
	 * 
	 * @return the ActivityMonitor used by Robotium
	 */

	public ActivityMonitor getActivityMonitor(){
		return activityMonitor;
	}

	/**
	 * 设置屏幕方向，横或者纵
	 * Sets the Orientation (Landscape/Portrait) for the current activity.
	 * 
	 * @param orientation An orientation constant such as {@link android.content.pm.ActivityInfo#SCREEN_ORIENTATION_LANDSCAPE} or {@link android.content.pm.ActivityInfo#SCREEN_ORIENTATION_PORTRAIT}
	 */

	public void setActivityOrientation(int orientation)
	{
		Activity activity = getCurrentActivity();
		activity.setRequestedOrientation(orientation);	
	}

	/**
	 * 获取当前的activity，true标识需要等待500ms,false标识不需要等待500ms
	 * Returns the current {@code Activity}, after sleeping a default pause length.
	 *
	 * @param shouldSleepFirst whether to sleep a default pause first
	 * @return the current {@code Activity}
	 */

	public Activity getCurrentActivity(boolean shouldSleepFirst) {
		return getCurrentActivity(shouldSleepFirst, true);
	}

	/**
	 * 获取当前activity,并且等待500ms
	 * Returns the current {@code Activity}, after sleeping a default pause length.
	 *
	 * @return the current {@code Activity}
	 */

	public Activity getCurrentActivity() {
		return getCurrentActivity(true, true);
	}

	/**
	 * 把activity加入堆栈中
	 * Adds an activity to the stack
	 * 
	 * @param activity the activity to add
	 */

	private void addActivityToStack(Activity activity){
		// activity名加入堆栈
		activitiesStoredInActivityStack.add(activity.toString());
		weakActivityReference = new WeakReference<Activity>(activity);
		activity = null;
		// activity弱引用对象加入堆栈
		activityStack.push(weakActivityReference);
	}

	/**
	 * 一直等待，知道出现抓取到一个存活的activity，未找到存活activity则不断迭代循环，有概率导致无限死循环
	 * 可自行修改添加一个超时时间，避免引发无法循环
	 * Waits for an activity to be started if one is not provided
	 * by the constructor.
	 */

	private final void waitForActivityIfNotAvailable(){
		// 如果当前堆栈中的activity为空,当初始化时传入的activity为null，可导致该状态
		if(activityStack.isEmpty() || activityStack.peek().get() == null){
			// 不断尝试获取当前activity,直到获取到一个存活的activity
			if (activityMonitor != null) {
				Activity activity = activityMonitor.getLastActivity();
				// 此处可能导致无限循环
				// activityMonitor初始化是为得到当前activity.应用又没有新打开页面，调用该方法就死循环了
				// 传入一个null的activity对象，在 初始化之后，没打开新的 activity就不断null,死循环了
				while (activity == null){
					// 等待300ms
					sleeper.sleepMini();
					// 获取当前activity
					activity = activityMonitor.getLastActivity();
				}
				// 非空对象加入堆栈
				addActivityToStack(activity);
			}
			else{
				// 等待300ms
				sleeper.sleepMini();
				// 初始化activityMonitor
				setupActivityMonitor();
				// 继续获取最新的activity
				waitForActivityIfNotAvailable();
			}
		}
	}

	/**
	 * 获取当前最新的activity,shouldSleepFirst为true,那么等待500ms后在获取,
	 * waitForActivity为true那么尝试获取最新的activity,为false则不尝试获取最新的，直接从activity堆栈中获取栈顶的activity返回
	 * 
	 * Returns the current {@code Activity}.
	 *
	 * @param shouldSleepFirst whether to sleep a default pause first
	 * @param waitForActivity whether to wait for the activity
	 * @return the current {@code Activity}
	 */

	public Activity getCurrentActivity(boolean shouldSleepFirst, boolean waitForActivity) {
		// 是否需要等待
		if(shouldSleepFirst){
			sleeper.sleep();
		}
		// 是否需要获取最新的 
		if(waitForActivity){
			waitForActivityIfNotAvailable();
		}
		// 获取堆栈中的栈顶activity
		if(!activityStack.isEmpty()){
			activity=activityStack.peek().get();
		}
		return activity;
	}

	/**
	 * 检查 activity堆栈是否为空
	 * Check if activity stack is empty.
	 * 
	 * @return true if activity stack is empty
	 */
	
	public boolean isActivityStackEmpty() {
		return activityStack.isEmpty();
	}

	/**
	 * 通过不断触发返回按钮尝试回到指定名字的activity
	 * Returns to the given {@link Activity}.
	 *
	 * @param name the name of the {@code Activity} to return to, e.g. {@code "MyActivity"}
	 */

	public void goBackToActivity(String name)
	{
		// 获取所有存活的activity
		ArrayList<Activity> activitiesOpened = getAllOpenedActivities();
		boolean found = false;	
		// 遍历所有存活的activity,如果不存在指定的activity,则为false,找到为 true
		for(int i = 0; i < activitiesOpened.size(); i++){
			if(activitiesOpened.get(i).getClass().getSimpleName().equals(name)){
				found = true;
				break;
			}
		}
		// 如果找对需要返回的activity在activity堆栈中.那么尝试货到该activity
		if(found){
			// 判断当前activity是否为需要返回的，不是是不断发送返回指令，直到找到
			while(!getCurrentActivity().getClass().getSimpleName().equals(name))
			{
				try{
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
					// instrument 触发该指令可能导致的exception
				}catch(SecurityException ignored){}	
			}
		}
		// 没有找到则打印先关日志.并且抛错
		else{
			for (int i = 0; i < activitiesOpened.size(); i++){
				Log.d(LOG_TAG, "Activity priorly opened: "+ activitiesOpened.get(i).getClass().getSimpleName());
			}
			Assert.fail("No Activity named: '" + name + "' has been priorly opened");
		}
	}

	/**
	 * 在当前activity中按照id查询String
	 * Returns a localized string.
	 * 
	 * @param resId the resource ID for the string
	 * @return the localized string
	 */

	public String getString(int resId)
	{
		Activity activity = getCurrentActivity(false);
		return activity.getString(resId);
	}

	/**
	 * solo生命周期结束，释放相关资源
	 * Finalizes the solo object.
	 */  

	@Override
	public void finalize() throws Throwable {
		// 停止activity监控定时任务
		activitySyncTimer.cancel();
		try {
			// 清理activityMonitor对象
			// Remove the monitor added during startup
			if (activityMonitor != null) {
				inst.removeMonitor(activityMonitor);
				activityMonitor = null;
			}
		} catch (Exception ignored) {}
		super.finalize();
	}

	/**
	 * 关闭所有存活的activity
	 * All activites that have been opened are finished.
	 */

	public void finishOpenedActivities(){
		// 停止activity监听定时任务
		// Stops the activityStack listener
		activitySyncTimer.cancel();
		// 获取所有存活的activity
		ArrayList<Activity> activitiesOpened = getAllOpenedActivities();
		// 结束所有存活的activity
		// Finish all opened activities
		for (int i = activitiesOpened.size()-1; i >= 0; i--) {
			sleeper.sleep(MINISLEEP);
			finishActivity(activitiesOpened.get(i));
		}
		// 释放对象
		activitiesOpened = null;
		sleeper.sleep(MINISLEEP);
		// Finish the initial activity, pressing Back for good measure
		finishActivity(getCurrentActivity(true, false));
		this.activity = null;
		sleeper.sleepMini();
		// 点击2次back按钮退出程序
		try {
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
			sleeper.sleep(MINISLEEP);
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
		} catch (Throwable ignored) {
			// Guard against lack of INJECT_EVENT permission
		}
		// 清空堆栈信息
		clearActivityStack();
	}

	/**
	 *清空堆栈信息
	 * Clears the activity stack.
	 */

	private void clearActivityStack(){
		activityStack.clear();
		activitiesStoredInActivityStack.clear();
	}

	/**
	 * 调用activity的清理方法结束activity生命周期
	 * Finishes an activity.
	 * 
	 * @param activity the activity to finish
	 */

	private void finishActivity(Activity activity){
		if(activity != null) {
			try{
				activity.finish();
			}catch(Throwable e){
				e.printStackTrace();
			}
		}
	}

}
