package com.robotium.solo;

/**
 * 判断条件接口
 * Represents a conditional statement.<br/>
 * Implementations may be used with {@link Solo#waitForCondition(Condition, int)}.
 */
public interface Condition {

	/**
	 * 判定条件满足返回true,不满足返回false
	 * Should do the necessary work needed to check a condition and then return whether this condition is satisfied or not.
	 * @return {@code true} if condition is satisfied and {@code false} if it is not satisfied
	 */
	public boolean isSatisfied();

}
