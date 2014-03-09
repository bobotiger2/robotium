package com.robotium.solo;

import junit.framework.Assert;
import android.app.Activity;
import android.app.ActivityManager;

/**
 * 测试的断言方法，提供断言支持，主要包装junit提供的
 * Contains assert methods examples are assertActivity() and assertLowMemory().
 * 
 * @author Renas Reda, renas.reda@robotium.com
 *
 */

class Asserter {
	// activity操作工具类
	private final ActivityUtils activityUtils;
	// 等待工具类
	private final Waiter waiter;

	/**
	 * 对象构造初始化
	 * Constructs this object.
	 *
	 * @param activityUtils the {@code ActivityUtils} instance.
	 * @param waiter the {@code Waiter} instance.
	 */

	public Asserter(ActivityUtils activityUtils, Waiter waiter) {
		this.activityUtils = activityUtils;
		this.waiter = waiter;
	}

	/**
	 * 断言判断当前activity是否是想要的
	 * message 当前activity名与name名字不一致则给出断言提示
	 * name    期望的activity名字
	 * Asserts that an expected {@link Activity} is currently active one.
	 *
	 * @param message the message that should be displayed if the assert fails
	 * @param name the name of the {@code Activity} that is expected to be active e.g. {@code "MyActivity"}
	 */

	public void assertCurrentActivity(String message, String name) {
		// 使用wait工具等待期望的activity出现,直接获取activity堆栈的栈顶activity,默认超时10s
		boolean foundActivity = waiter.waitForActivity(name);
		// 如果期望的activity未找到，则用断言提示相关异常
		if(!foundActivity)
			Assert.assertEquals(message, name, activityUtils.getCurrentActivity().getClass().getSimpleName());		
	}

	/**
	 * 按照Class类断言当前activity是否是期望的activity
	 * message 如果不是期望的，断言的提示信息
	 * expectedClass 期望的activity类
	 * Asserts that an expected {@link Activity} is currently active one.
	 *
	 * @param message the message that should be displayed if the assert fails
	 * @param expectedClass the {@code Class} object that is expected to be active e.g. {@code MyActivity.class}
	 */

	public void assertCurrentActivity(String message, Class<? extends Activity> expectedClass) {
		// null检查
		if(expectedClass == null){
			Assert.fail("The specified Activity is null!");
		}
		// 检查期望的class对应的activity是否出现,直接获取activity堆栈的栈顶activity,默认超时10s
		boolean foundActivity = waiter.waitForActivity(expectedClass);
		// 未找到，断言给出错误提示
		if(!foundActivity) {
			Assert.assertEquals(message, expectedClass.getName(), activityUtils.getCurrentActivity().getClass().getName());
		}
	}

	/**
	 * 断言当前activity是否与输入的activity名字一致
	 * message 不一致的提示信息
	 * name    期望的activity名字
	 * isNewInstance 为true则等待最新出现的activity,为false则直接获取activity堆栈的栈顶activity做比较
	 * Asserts that an expected {@link Activity} is currently active one, with the possibility to
	 * verify that the expected {@code Activity} is a new instance of the {@code Activity}.
	 * 
	 * @param message the message that should be displayed if the assert fails
	 * @param name the name of the {@code Activity} that is expected to be active e.g. {@code "MyActivity"}
	 * @param isNewInstance {@code true} if the expected {@code Activity} is a new instance of the {@code Activity}
	 */

	public void assertCurrentActivity(String message, String name, boolean isNewInstance) {
		// 检查当前activity的名字是否是期望的
		assertCurrentActivity(message, name);
		// 检查当前activity的类对象是否是存活的
		assertCurrentActivity(message, activityUtils.getCurrentActivity().getClass(),
				isNewInstance);
	}

	/**
	 * 检查class类是否为当前的activity
	 * message 当不是期望的activity时提示异常信息
	 * expectedClass 期望的activity类
	 * isNewInstance 为true则等待最新出现的activity,为false则直接获取activity堆栈的栈顶activity做比较
	 * Asserts that an expected {@link Activity} is currently active one, with the possibility to
	 * verify that the expected {@code Activity} is a new instance of the {@code Activity}.
	 * 
	 * @param message the message that should be displayed if the assert fails
	 * @param expectedClass the {@code Class} object that is expected to be active e.g. {@code MyActivity.class}
	 * @param isNewInstance {@code true} if the expected {@code Activity} is a new instance of the {@code Activity}
	 */

	public void assertCurrentActivity(String message, Class<? extends Activity> expectedClass,
			boolean isNewInstance) {
		boolean found = false;
		// 先判断当前类是否是期望的
		assertCurrentActivity(message, expectedClass);
		// 获取activity堆栈的栈顶activity
		Activity activity = activityUtils.getCurrentActivity(false);
		// 判断当前打开的所有的activity中是否存在期望的
		for (int i = 0; i < activityUtils.getAllOpenedActivities().size() - 1; i++) {
			String instanceString = activityUtils.getAllOpenedActivities().get(i).toString();
			if (instanceString.equals(activity.toString()))
				found = true;
		}
		// 断言判断是否出现
		Assert.assertNotSame(message, isNewInstance, found);
	}

	/**
	 * 检查当前是否内存过低
	 * Asserts that the available memory is not considered low by the system.
	 */

	public void assertMemoryNotLow() {
		// 构建内存信息对象
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		// 获取当前activity对象获取内存信息
		((ActivityManager)activityUtils.getCurrentActivity().getSystemService("activity")).getMemoryInfo(mi);
		// 通过lowMemory状态判断是否内存过低
		Assert.assertFalse("Low memory available: " + mi.availMem + " bytes!", mi.lowMemory);
	}

}
