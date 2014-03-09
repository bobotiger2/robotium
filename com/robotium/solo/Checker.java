package com.robotium.solo;

import java.util.ArrayList;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * Check类View检查工具类，提供各种信息检查用
 * Contains various check methods. Examples are: isButtonChecked(),
 * isSpinnerTextSelected.
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class Checker {
	// view获取工具类
	private final ViewFetcher viewFetcher;
	// wait等待工具类,用于获取各类 View和判断text等内容是否出现
	private final Waiter waiter;

	/**
	 * 构造函数
	 * Constructs this object.
	 * 
	 * @param viewFetcher the {@code ViewFetcher} instance
     * @param waiter the {@code Waiter} instance
	 */
	
	public Checker(ViewFetcher viewFetcher, Waiter waiter){
		this.viewFetcher = viewFetcher;
		this.waiter = waiter;
	}

	
	/**
	 * 获取指定类型的第index个CompoundButton对象，检查是否被选中了.CompoundButton有选中和未选中2种状态
	 * Checks if a {@link CompoundButton} with a given index is checked.
	 *
	 * @param expectedClass the expected class, e.g. {@code CheckBox.class} or {@code RadioButton.class}
	 * @param index of the {@code CompoundButton} to check. {@code 0} if only one is available
	 * @return {@code true} if {@code CompoundButton} is checked and {@code false} if it is not checked
	 */
	
	public <T extends CompoundButton> boolean isButtonChecked(Class<T> expectedClass, int index)
	{	
		// 调用waiter的方法获取指定条件的view
		return (waiter.waitForAndGetView(index, expectedClass).isChecked());
	}
	
	/**
	 * 获取知道类型的指定text的CompoundButton类型控件，取第一个，检查是否被选中,如果没有找到指定条件的控件，那么也返回false
	 * Checks if a {@link CompoundButton} with a given text is checked.
	 *
	 * @param expectedClass the expected class, e.g. {@code CheckBox.class} or {@code RadioButton.class}
	 * @param text the text that is expected to be checked
	 * @return {@code true} if {@code CompoundButton} is checked and {@code false} if it is not checked
	 */

	public <T extends CompoundButton> boolean isButtonChecked(Class<T> expectedClass, String text)
	{
		// 按照给定条件查找View,可拖动刷新查找
		T button = waiter.waitForText(expectedClass, text, 0, Timeout.getSmallTimeout(), true);
		// 检查是否找到且被选中
		if(button != null && button.isChecked()){
			return true;
		}
		return false;
	}

	/**
	 * 查找指定text的第一个CheckedTextView类型控件，检查是否被选中，选中返回true,未选中返回false.如果未找到也返回false.
	 * Checks if a {@link CheckedTextView} with a given text is checked.
	 *
	 * @param checkedTextView the {@code CheckedTextView} object
	 * @param text the text that is expected to be checked
	 * @return {@code true} if {@code CheckedTextView} is checked and {@code false} if it is not checked
	 */

	public boolean isCheckedTextChecked(String text)
	{
		// 按照指定条件查找View
		CheckedTextView checkedTextView = waiter.waitForText(CheckedTextView.class, text, 0, Timeout.getSmallTimeout(), true);
		// 检查是否找到且被选中
		if(checkedTextView != null && checkedTextView.isChecked()) {
			return true;
		}
		return false;
	}

	
	/**
	 * 查找指定text的所有Spinner类型控件，检查是否有被选中，有选中返回true,未选中返回false.如果未找到也返回false.
	 * Checks if a given text is selected in any {@link Spinner} located on the current screen.
	 * 
	 * @param text the text that is expected to be selected
	 * @return {@code true} if the given text is selected in any {@code Spinner} and false if it is not
	 */
	
	public boolean isSpinnerTextSelected(String text)
	{
		// 刷新一次页面
		waiter.waitForAndGetView(0, Spinner.class);
		// 查找指定text的 Spinner
		ArrayList<Spinner> spinnerList = viewFetcher.getCurrentViews(Spinner.class);
		// 遍历检查其中是否有符合条件的
		for(int i = 0; i < spinnerList.size(); i++){
			if(isSpinnerTextSelected(i, text))
					return true;
		}
		return false;
	}
	
	/**
	 * 查找指定text的第spinnerIndex个Spinner类型控件，检查是否有被选中，有选中返回true,未选中返回false.如果未找到也返回false.
	 * 因该方法内未作null判断，因此有导致nullpoint异常的可能
	 * Checks if a given text is selected in a given {@link Spinner} 
	 * @param spinnerIndex the index of the spinner to check. 0 if only one spinner is available
	 * @param text the text that is expected to be selected
	 * @return true if the given text is selected in the given {@code Spinner} and false if it is not
	 */
	
	public boolean isSpinnerTextSelected(int spinnerIndex, String text)
	{
		// 获取指定的Spinner
		Spinner spinner = waiter.waitForAndGetView(spinnerIndex, Spinner.class);
		// 未检查获取的是否是null,如无Spinner类型，可导致nullpoint异常
		// 获取Spinner当前被选中的text
		TextView textView = (TextView) spinner.getChildAt(0);
		// 检查是否为指定的
		if(textView.getText().equals(text))
			return true;
		else
			return false;
	}
}
