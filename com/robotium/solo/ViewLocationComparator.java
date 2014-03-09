package com.robotium.solo;

import android.view.View;
import java.util.Comparator;

/**
 * View控件比较工具,默认使用y坐标值做比较，即view按照在屏幕中的上面到下面排序
 * Orders {@link View}s by their location on-screen.
 * 
 */

class ViewLocationComparator implements Comparator<View> {
	// 存放第一个view的位置坐标
	private final int a[] = new int[2];
	// 存放第二个view的位置坐标
	private final int b[] = new int[2];
	private final int axis1, axis2;
	// 默认构造函数，高度优先排序
	public ViewLocationComparator() {
		this(true);
	}

	/**
	 * 设置排序规则
	 * yAxisFirst true按照高度优先比较，false 按照 x坐标，从左往右排序
	 * @param yAxisFirst Whether the y-axis should be compared before the x-axis.
	 */

	public ViewLocationComparator(boolean yAxisFirst) {
		this.axis1 = yAxisFirst ? 1 : 0;
		this.axis2 = yAxisFirst ? 0 : 1;
	}
	// 按照构造函数设定的规则，比较2个view的位置
	public int compare(View lhs, View rhs) {
		// 获取第一个view的位置坐标信息
		lhs.getLocationOnScreen(a);
		// 获取第二个view的位置坐标信息
		rhs.getLocationOnScreen(b);
		// 首先坐标不相等，比较首先坐标大小
		if (a[axis1] != b[axis1]) {
			return a[axis1] < b[axis1] ? -1 : 1;
		}
		// 比较第二个坐标大小
		if (a[axis2] < b[axis2]) {
			return -1;
		}
		// 2个坐标相等，则返回0，第1个比第二个大则返回1
		return a[axis2] == b[axis2] ? 0 : 1;
	}
}
