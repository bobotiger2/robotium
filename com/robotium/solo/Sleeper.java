package com.robotium.solo;
/**
 * 延时等待工具类
 *
 */
class Sleeper {
	// 常量500ms
	private final int PAUSE = 500;
	// 常量300ms
	private final int MINIPAUSE = 300;

	/**
	 * 延时500ms
	 * Sleeps the current thread for a default pause length.
	 */

	public void sleep() {
        sleep(PAUSE);
	}


	/**
	 * 延时300ms
	 * Sleeps the current thread for a default mini pause length.
	 */

	public void sleepMini() {
        sleep(MINIPAUSE);
	}


	/**
	 * 延时指定数值的ms
	 * Sleeps the current thread for <code>time</code> milliseconds.
	 *
	 * @param time the length of the sleep in milliseconds
	 */

	public void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException ignored) {}
	}

}
