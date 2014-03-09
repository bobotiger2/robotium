package com.robotium.solo;

import android.widget.EditText;
import android.widget.Spinner;
import junit.framework.Assert;
import android.app.Instrumentation;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

/**
 * 按操作工具类
 * Contains press methods. Examples are pressMenuItem(),
 * pressSpinnerItem().
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class Presser{
	// 点击操作工具类
	private final Clicker clicker;
	// Instrument 用于发送事件
	private final Instrumentation inst;
	// 等待工具类
	private final Sleeper sleeper;
	// View等待工具类
	private final Waiter waiter;
	// 弹框处理工具类
	private final DialogUtils dialogUtils;
	// View获取工具类
	private final ViewFetcher viewFetcher;


	/**
	 * 构造函数
	 * Constructs this object.
	 *
	 * @param viewFetcher the {@code ViewFetcher} instance
	 * @param clicker the {@code Clicker} instance
	 * @param inst the {@code Instrumentation} instance
	 * @param sleeper the {@code Sleeper} instance
	 * @param waiter the {@code Waiter} instance
	 * @param dialogUtils the {@code DialogUtils} instance
	 */

	public Presser(ViewFetcher viewFetcher, Clicker clicker, Instrumentation inst, Sleeper sleeper, Waiter waiter, DialogUtils dialogUtils) {
		this.viewFetcher = viewFetcher;
		this.clicker = clicker;
		this.inst = inst;
		this.sleeper = sleeper;
		this.waiter = waiter;
		this.dialogUtils = dialogUtils;
	}


	/**
	 * 点击Menu中的第n个Item,Item从左往右从上到下，按顺序排列,默认每行包含3个Item
	 * Presses a {@link android.view.MenuItem} with a given index. Index {@code 0} is the first item in the
	 * first row, Index {@code 3} is the first item in the second row and
	 * index {@code 5} is the first item in the third row.
	 *
	 * @param index the index of the {@code MenuItem} to be pressed
	 */

	public void pressMenuItem(int index){
		// 设置每行3个 Item
		pressMenuItem(index, 3);
	}

	/**
	 * 点击Menu中的第n个Item,Item从左往右从上到下，按顺序排列,
	 * index	              需要点击的第n个Item,从1开始
	 * itemsPerRow   每一行包含的Item数量
	 * Presses a {@link android.view.MenuItem} with a given index. Supports three rows with a given amount
	 * of items. If itemsPerRow equals 5 then index 0 is the first item in the first row, 
	 * index 5 is the first item in the second row and index 10 is the first item in the third row.
	 * 
	 * @param index the index of the {@code MenuItem} to be pressed
	 * @param itemsPerRow the amount of menu items there are per row.   
	 */

	public void pressMenuItem(int index, int itemsPerRow) {	
		//  Item缓存，存储4行，每行的开头序号
		int[] row = new int[4];
		// 初始化Item id 信息
		for(int i = 1; i <=3; i++)
			row[i] = itemsPerRow*i;
		// 等待500ms
		sleeper.sleep();
		try{
			// 点击Menu按钮
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
			// 等待Menu出现
			dialogUtils.waitForDialogToOpen(Timeout.getSmallTimeout(), true);
			// 点击2次上方向键.Item位置回到第一个
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
		}catch(SecurityException e){
			Assert.fail("Can not press the menu!");
		}
		// 如果指定Item在第一行,则在第一行移动，往右移动，移动到指定的Item
		if (index < row[1]) {
			for (int i = 0; i < index; i++) {
				sleeper.sleepMini();
				inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
			}
		// 在第二行
		} else if (index >= row[1] && index < row[2]) {
			// 下移到下一行，即第二行
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);	
			// 移动到指定的Item
			for (int i = row[1]; i < index; i++) {
				sleeper.sleepMini();
				inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
			}
			// 在第三行,或者之后的行
		} else if (index >= row[2]) {
			// 移动到第三行
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);	
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);	
			// 移动到指定的Item
			for (int i = row[2]; i < index; i++) {
				sleeper.sleepMini();
				inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
			}
		}

		try{
			// 点击确认
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
		}catch (SecurityException ignored) {}
	}
	
	/**
	 * 点击软件盘当前按键的下一个按键
	 * Presses the soft keyboard next button. 
	 */

	public void pressSoftKeyboardNextButton(){
		// 获取一个EditText.只有EditText才有软键盘
		final EditText freshestEditText = viewFetcher.getFreshestView(viewFetcher.getCurrentViews(EditText.class));
		// 可以获取EditText
		if(freshestEditText != null){
			inst.runOnMainSync(new Runnable()
			{
				public void run()
				{
					// 点击当前按钮位置的下一个按钮
					freshestEditText.onEditorAction(EditorInfo.IME_ACTION_NEXT); 
				}
			});
		}
	}

	/**
	 * 点击第spinnerIndex个 Spinner的第itemIndex个Item
	 * spinnerIndex     指定的Spinner顺序
	 * itemIndex        指定的Item顺序,如果是正值，那么往下移动，负值往上移动
	 * Presses on a {@link android.widget.Spinner} (drop-down menu) item.
	 *
	 * @param spinnerIndex the index of the {@code Spinner} menu to be used
	 * @param itemIndex the index of the {@code Spinner} item to be pressed relative to the currently selected item.
	 * A Negative number moves up on the {@code Spinner}, positive moves down
	 */

	public void pressSpinnerItem(int spinnerIndex, int itemIndex)
	{	
		// 点击下来列表
		clicker.clickOnScreen(waiter.waitForAndGetView(spinnerIndex, Spinner.class));
		// 等待下拉列表出现
		dialogUtils.waitForDialogToOpen(Timeout.getSmallTimeout(), true);

		try{
			// 发送事件，初始化位置,最下面
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
		}catch(SecurityException ignored){}
		// 如果指定的itemIndex为负值，那么往上移动
		boolean countingUp = true;
		if(itemIndex < 0){
			countingUp = false;
			itemIndex *= -1;
		}
		// 按照指定的顺序，移动 Item到对应的位置
		for(int i = 0; i < itemIndex; i++)
		{
			sleeper.sleepMini();
			// 向下
			if(countingUp){
				try{
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
				}catch(SecurityException ignored){}
			// 向下
			}else{
				try{
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
				}catch(SecurityException ignored){}
			}
		}
		// 点击确认按钮
		try{
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
		}catch(SecurityException ignored){}
	}
}
