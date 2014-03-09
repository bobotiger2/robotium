package com.robotium.solo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import android.view.View;
import android.widget.TextView;

/**
 * Robotium操作工具类
 * Contains utility methods. Examples are: removeInvisibleViews(Iterable<T> viewList),
 * filterViews(Class<T> classToFilterBy, Iterable<?> viewList), sortViewsByLocationOnScreen(List<? extends View> views).
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

public class RobotiumUtils {


	/**
	 * 移除给定列表中的分可见View,并返回剩余的
	 * Removes invisible Views.
	 * 
	 * @param viewList an Iterable with Views that is being checked for invisible Views
	 * @return a filtered Iterable with no invisible Views
	 */

	public static <T extends View> ArrayList<T> removeInvisibleViews(Iterable<T> viewList) {
		ArrayList<T> tmpViewList = new ArrayList<T>();
		for (T view : viewList) {
			// 可见的view加入返回List
			if (view != null && view.isShown()) {
				tmpViewList.add(view);
			}
		}
		return tmpViewList;
	}

	/**
	 * 查找给定列表中的指定类型View,并返回
	 * classToFilterBy 给定的View  class
	 * Filters Views based on the given class type.
	 * 
	 * @param classToFilterBy the class to filter
	 * @param viewList the Iterable to filter from
	 * @return an ArrayList with filtered views
	 */

	public static <T> ArrayList<T> filterViews(Class<T> classToFilterBy, Iterable<?> viewList) {
		ArrayList<T> filteredViews = new ArrayList<T>();
		for (Object view : viewList) {
			// 如果是指定的class类型，加入返回列表
			if (view != null && classToFilterBy.isAssignableFrom(view.getClass())) {
				filteredViews.add(classToFilterBy.cast(view));
			}
		}
		viewList = null;
		return filteredViews;
	}

	/**
	 * 查找给定列表中的指定类型Views,并返回
	 * classSet[] 指定的一组class
	 * Filters all Views not within the given set.
	 *
	 * @param classSet contains all classes that are ok to pass the filter
	 * @param viewList the Iterable to filter form
	 * @return an ArrayList with filtered views
	 */

	public static ArrayList<View> filterViewsToSet(Class<View> classSet[], Iterable<View> viewList) {
		ArrayList<View> filteredViews = new ArrayList<View>();
		for (View view : viewList) {
			if (view == null)
				continue;
			// 属于指定类型的view加入返回列表
			for (Class<View> filter : classSet) {
				if (filter.isAssignableFrom(view.getClass())) {
					filteredViews.add(view);
					break;
				}
			}
		}
		return filteredViews;
	}

	/**
	 * 按照控件屏幕位置从上往下排序
	 * Orders Views by their location on-screen.
	 * 
	 * @param views The views to sort.
	 * @see ViewLocationComparator
	 */

	public static void sortViewsByLocationOnScreen(List<? extends View> views) {
		Collections.sort(views, new ViewLocationComparator());
	}

	/**
	 * 按照控件在屏幕上的展示信息排序，从上往下，或从左往右
	 * views       需要排序的views
	 * yAxisFirst  为true则从上往下排序，为false则从左往右排序
	 * Orders Views by their location on-screen.
	 * 
	 * @param views The views to sort.
	 * @param yAxisFirst Whether the y-axis should be compared before the x-axis.
	 * @see ViewLocationComparator
	 */

	public static void sortViewsByLocationOnScreen(List<? extends View> views, boolean yAxisFirst) {
		Collections.sort(views, new ViewLocationComparator(yAxisFirst));
	}

	/**
	 * 校验view的文本内容，错误提示信息和帮助提醒信息是否与给定的regex匹配,返回uniqueTextViews中的view总数
	 * regex           给定的正则
	 * view   		         需要校验的view
	 * uniqueTextViews 已存在的view列表,如果匹配则加入此列表
	 * Checks if a View matches a certain string and returns the amount of total matches.
	 * 
	 * @param regex the regex to match
	 * @param view the view to check
	 * @param uniqueTextViews set of views that have matched
	 * @return number of total matches
	 */

	public static int getNumberOfMatches(String regex, TextView view, Set<TextView> uniqueTextViews){
		// 如果传入view为null,那么直接返回总数量
		if(view == null) {
			return uniqueTextViews.size();
		}
		// 按照输入的regex构造正则对象
		Pattern pattern = null;
		try{
			pattern = Pattern.compile(regex);
		}catch(PatternSyntaxException e){
			pattern = Pattern.compile(regex, Pattern.LITERAL);
		}
		// 获取view 的 text并按照正则匹配
		Matcher matcher = pattern.matcher(view.getText().toString());
		//如果配置，把 view加入uniqueTextViews
		if (matcher.find()){
			uniqueTextViews.add(view);
		}
		// 如果view设置了错误提示信息.那么错误提示信息也作为检查条件,如果错误信息匹配了输入的regex,
		// 那么加入uniqueTextViews.因uniqueTextViews为Set类型，所以不会存在重复view.重复add不生效
		if (view.getError() != null){
			matcher = pattern.matcher(view.getError().toString());
			if (matcher.find()){
				uniqueTextViews.add(view);
			}
		}	
		// 检查view 的提示信息是否和给定的regex匹配，如果配置也当做符合的view
		if (view.getText().toString().equals("") && view.getHint() != null){
			matcher = pattern.matcher(view.getHint().toString());
			if (matcher.find()){
				uniqueTextViews.add(view);
			}
		}	
		// 返回uniqueTextViews总数
		return uniqueTextViews.size();		
	}

	/**
	 * 按照给定的text过滤views,返回配置的views,text被当做正则表达式解析
	 * Filters a collection of Views and returns a list that contains only Views
	 * with text that matches a specified regular expression.
	 * 
	 * @param views The collection of views to scan.
	 * @param regex The text pattern to search for.
	 * @return A list of views whose text matches the given regex.
	 */

	public static <T extends TextView> List<T> filterViewsByText(Iterable<T> views, String regex) {
		return filterViewsByText(views, Pattern.compile(regex));
	}

	/**
	 * 按照给定的text规则正则表达式过滤views,返回配置的views
	 * views  传入的views
	 * regex  正则表达式
	 * Filters a collection of Views and returns a list that contains only Views
	 * with text that matches a specified regular expression.
	 * 
	 * @param views The collection of views to scan.
	 * @param regex The text pattern to search for.
	 * @return A list of views whose text matches the given regex.
	 */

	public static <T extends TextView> List<T> filterViewsByText(Iterable<T> views, Pattern regex) {
		final ArrayList<T> filteredViews = new ArrayList<T>();
		// 遍历view
		for (T view : views) {
			// 与正则匹配的加入返回列表
			if (view != null && regex.matcher(view.getText()).matches()) {
				filteredViews.add(view);
			}
		}
		return filteredViews;
	}
}
