package com.robotium.solo;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import com.robotium.solo.Solo.Config;
import com.robotium.solo.Solo.Config.ScreenshotFileType;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

/**
 * 截屏操作工具类
 * Contains screenshot methods like: takeScreenshot(final View, final String name), startScreenshotSequence(final String name, final int quality, final int frameDelay, final int maxFrames), 
 * stopScreenshotSequence().
 * 
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class ScreenshotTaker {
	// 配置文件，配置Robotium的各种属性
	private final Config config;
	// activity工具类
	private final ActivityUtils activityUtils;
	// 日志标记，标识该操作是Robotium的
	private final String LOG_TAG = "Robotium";
	// 连续截图线程
	private ScreenshotSequenceThread screenshotSequenceThread = null;
	// 图片存储处理线程
	private HandlerThread screenShotSaverThread = null;
	// 图片保存工具类
	private ScreenShotSaver screenShotSaver = null;
	// view查找工具类
	private final ViewFetcher viewFetcher;
	// 延时等待工具类
	private final Sleeper sleeper;


	/**
	 * 构造函数
	 * Constructs this object.
	 * 
	 * @param config the {@code Config} instance
	 * @param activityUtils the {@code ActivityUtils} instance
	 * @param viewFetcher the {@code ViewFetcher} instance
	 * @param sleeper the {@code Sleeper} instance
	 * 
	 */
	ScreenshotTaker(Config config, ActivityUtils activityUtils, ViewFetcher viewFetcher, Sleeper sleeper) {
		this.config = config;
		this.activityUtils = activityUtils;
		this.viewFetcher = viewFetcher;
		this.sleeper = sleeper;
	}

	/**
	 * 截图操作,要去有写SDcard权限，截图文件会存储到sdcard,如要修改储存路径，那么可以通过修改Config的screenshotSavePath属性编辑
	 * 默认路径为/sdcard/Robotium-Screenshots/
	 * name     截图保存文件名
	 * quality  截图质量0-100
	 * Takes a screenshot and saves it in the {@link Config} objects save path.  
	 * Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.
	 * 
	 * @param view the view to take screenshot of   这个参数已经没有了，就没必要加这个注释了
	 * @param name the name to give the screenshot image
	 * @param quality the compression rate. From 0 (compress for lowest size) to 100 (compress for maximum quality).
	 */
	public void takeScreenshot(final String name, final int quality) {
		// 获取DecorView
		View decorView = getScreenshotView();
		// 无法获取DecorView,直接退出
		if(decorView == null) 
			return;
		// 初始化图片存储需要的一些事情
		initScreenShotSaver();
		// 构造截图线程
		ScreenshotRunnable runnable = new ScreenshotRunnable(decorView, name, quality);
		// 执行截图线程
		activityUtils.getCurrentActivity(false).runOnUiThread(runnable);
	}

	/**
	 * 连接截图
	 * name    		截图保存的图片名.会追加_0---maxFrames-1
	 * quality 		截图质量0-100
	 * frameDelay   每次截图时间间隔
	 * maxFrames    截图数量
	 * Takes a screenshot sequence and saves the images with the name prefix in the {@link Config} objects save path.  
	 *
	 * The name prefix is appended with "_" + sequence_number for each image in the sequence,
	 * where numbering starts at 0.  
	 *
	 * Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in the 
	 * AndroidManifest.xml of the application under test.
	 *
	 * Taking a screenshot will take on the order of 40-100 milliseconds of time on the 
	 * main UI thread.  Therefore it is possible to mess up the timing of tests if
	 * the frameDelay value is set too small.
	 *
	 * At present multiple simultaneous screenshot sequences are not supported.  
	 * This method will throw an exception if stopScreenshotSequence() has not been
	 * called to finish any prior sequences.
	 *
	 * @param name the name prefix to give the screenshot
	 * @param quality the compression rate. From 0 (compress for lowest size) to 100 (compress for maximum quality)
	 * @param frameDelay the time in milliseconds to wait between each frame
	 * @param maxFrames the maximum number of frames that will comprise this sequence
	 *
	 */
	public void startScreenshotSequence(final String name, final int quality, final int frameDelay, final int maxFrames) {
		// 初始化截图保存相关
		initScreenShotSaver();
		// 禁止同时执行多个连续截图，当有连续截图在执行时抛出异常
		if(screenshotSequenceThread != null) {
			throw new RuntimeException("only one screenshot sequence is supported at a time");
		}
		// 构造一个连续截图线程
		screenshotSequenceThread = new ScreenshotSequenceThread(name, quality, frameDelay, maxFrames);
		// 开始连续截图
		screenshotSequenceThread.start();
	}

	/**
	 * 停止连续截图
	 * Causes a screenshot sequence to end.
	 * 
	 * If this method is not called to end a sequence and a prior sequence is still in 
	 * progress, startScreenshotSequence() will throw an exception.
	 */
	public void stopScreenshotSequence() {
		// 当连续截图线程非空时，停止连续截图
		if(screenshotSequenceThread != null) {
			// 停止连续截图
			screenshotSequenceThread.interrupt();
			// 释放线程对象
			screenshotSequenceThread = null;
		}
	}

	/**
	 * 获取当前的界面显示view,并做一些Robotium定制化的操作
	 * Gets the proper view to use for a screenshot.  
	 */
	private View getScreenshotView() {
		// 获取当前的显示界面view
		View decorView = viewFetcher.getRecentDecorView(viewFetcher.getWindowDecorViews());
		// 设置超时时间
		final long endTime = SystemClock.uptimeMillis() + Timeout.getSmallTimeout();
		// 如果无法获取decorView,则继续查找
		while (decorView == null) {	
			// 检查是否已经超时
			final boolean timedOut = SystemClock.uptimeMillis() > endTime;
			// 已经超时直接退出
			if (timedOut){
				return null;
			}
			// 等待300ms
			sleeper.sleepMini();
			// 重试获取当前的decorView
			decorView = viewFetcher.getRecentDecorView(viewFetcher.getWindowDecorViews());
		}
		// 用Rotium的Render替换原生的Render
		wrapAllGLViews(decorView);

		return decorView;
	}

	/**
	 * 修改 View的Render,用Robotium自定义的替换
	 * Extract and wrap the all OpenGL ES Renderer.
	 */
	private void wrapAllGLViews(View decorView) {
		// 获取当前decorView中的GLSurfaceView类型的view
		ArrayList<GLSurfaceView> currentViews = viewFetcher.getCurrentViews(GLSurfaceView.class, decorView);
		// 锁住当前线程，避免并发引发问题
		final CountDownLatch latch = new CountDownLatch(currentViews.size());
		// 编译所有view进行替换render
		for (GLSurfaceView glView : currentViews) {
			// 反射获取属性
			Object renderContainer = new Reflect(glView).field("mGLThread")
					.type(GLSurfaceView.class).out(Object.class);
			// 获取原始的renderer
			Renderer renderer = new Reflect(renderContainer).field("mRenderer").out(Renderer.class);
			// 如果获取失败，则尝试直接获取glView的属性
			if (renderer == null) {
				renderer = new Reflect(glView).field("mRenderer").out(Renderer.class);
				renderContainer = glView;
			}  
			// 如果无法获取，则跳过当前，处理下一个
			if (renderer == null) {
				//计数器减一
				latch.countDown();
				// 跳转到下个循环
				continue;
			}
			// 按照render类型进行操作,如果已经是Robotium修改过的render,那么重置下相关属性即可
			if (renderer instanceof GLRenderWrapper) {
				// 类型转成Robotium的
				GLRenderWrapper wrapper = (GLRenderWrapper) renderer;
				// 设置截图模式
				wrapper.setTakeScreenshot();
				// 设置并发控制计数器
				wrapper.setLatch(latch);
				// 如果还不是robotium修改过的，那么就重新构造一个，并且替换原有属性
			} else {
				// 构造一个robotium修改过的Render
				GLRenderWrapper wrapper = new GLRenderWrapper(glView, renderer, latch);
				// 通过反射修改属性为定制的render
				new Reflect(renderContainer).field("mRenderer").in(wrapper);
			}
		}
		// 等待操作完成
		try {
			latch.await();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * 获取WebView的图形内容
	 * Returns a bitmap of a given WebView.
	 *  
	 * @param webView the webView to save a bitmap from
	 * @return a bitmap of the given web view
	 * 
	 */

	private Bitmap getBitmapOfWebView(final WebView webView){
		// 获取WebView图形内容
		Picture picture = webView.capturePicture();
		// 构造Bitmap对象
		Bitmap b = Bitmap.createBitmap( picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888);
		// 构造Canvas
		Canvas c = new Canvas(b);
		// 把图片绘制到canvas.就是把内容搞到Bitmap中，即b中
		picture.draw(c);
		return b;
	}

	/**
	 * 获取View的BitMap格式文件内容
	 * Returns a bitmap of a given View.
	 * 
	 * @param view the view to save a bitmap from
	 * @return a bitmap of the given view
	 * 
	 */

	private Bitmap getBitmapOfView(final View view){
		// 初始化缓冲，清空原有内容
		view.destroyDrawingCache();
		view.buildDrawingCache(false);
		// 获取Bitmap内容
		Bitmap orig = view.getDrawingCache();
		Bitmap.Config config = null;
		// 如果获取内容为null,直接返回null
		if(orig == null) {
			return null;
		}
		// 获取配置信息
		config = orig.getConfig();
		// 如果图片类型无法获取，则默认使用ARGB_8888
		if(config == null) {
			config = Bitmap.Config.ARGB_8888;
		}
		// 构造BitMap内容
		Bitmap b = orig.copy(config, false);
		// 清空绘图缓存
		view.destroyDrawingCache();
		return b; 
	}

	/**
	 * 按照传入文件名，构造完整文件名
	 * Returns a proper filename depending on if name is given or not.
	 * 
	 * @param name the given name
	 * @return a proper filename depedning on if a name is given or not
	 * 
	 */

	private String getFileName(final String name){
		// 构造日期格式
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy-hhmmss");
		String fileName = null;
		// 如果未传入名字，那么默认构造一个
		if(name == null){
			// 按照配置构造图片类型jpg png
			if(config.screenshotFileType == ScreenshotFileType.JPEG){
				fileName = sdf.format( new Date()).toString()+ ".jpg";
			}
			else{
				fileName = sdf.format( new Date()).toString()+ ".png";	
			}
		}
		// 如已传入文件名字，那么拼接文件类型后缀
		else {
			// 按照配置构造图片类型jpg png
			if(config.screenshotFileType == ScreenshotFileType.JPEG){
				fileName = name + ".jpg";
			}
			else {
				fileName = name + ".png";	
			}
		}
		return fileName;
	}

	/**
	 * 初始化图片存储相关资源
	 * This method initializes the aysnc screenshot saving logic
	 */
	private void initScreenShotSaver() {
		// 如果当前存储线程未初始化，则进行初始化
		if(screenShotSaverThread == null || screenShotSaver == null) {
			// 初始化一个处理线程
			screenShotSaverThread = new HandlerThread("ScreenShotSaver");
			// 开始运行线程
			screenShotSaverThread.start();
			// 初始化一个存储类
			screenShotSaver = new ScreenShotSaver(screenShotSaverThread);
		}
	}

	/** 
	 * 连续截图线程
	 * _name       截图保存名,会拼接上顺序0--_maxFrames-1
	 * _quality    截图质量0-100
	 * _frameDelay 截图间隔时间，单位 ms
	 * _maxFrames  截图数量
	 * This is the thread which causes a screenshot sequence to happen
	 * in parallel with testing.
	 */
	private class ScreenshotSequenceThread extends Thread {
		// 开始点设置为0
		private int seqno = 0;
		// 保存的文件名
		private String name;
		// 图片质量0-100
		private int quality;
		// 截图延时，单位 ms
		private int frameDelay;
		// 需要截图的数量
		private int maxFrames;

		private boolean keepRunning = true;
		// 构造函数
		public ScreenshotSequenceThread(String _name, int _quality, int _frameDelay, int _maxFrames) {
			name = _name;
			quality = _quality; 
			frameDelay = _frameDelay;
			maxFrames = _maxFrames;
		}

		public void run() {
			// 截图数量未达到指定值，继续截图
			while(seqno < maxFrames) {
				// 线程结束或业务已经完成则退出循环
				if(!keepRunning || Thread.interrupted()) break;
				// 截图
				doScreenshot();
				// 计算器+1
				seqno++;
				try {
					// 等待指定的时间
					Thread.sleep(frameDelay);
				} catch (InterruptedException e) {
				}
			}
			// 释放线程对象
			screenshotSequenceThread = null;
		}
		// 截图
		public void doScreenshot() {
			// 获取当前的屏幕DecorView
			View v = getScreenshotView();
			// 如果无法获取decorView 终止当前线程
			if(v == null) keepRunning = false;
			// 拼接文件名
			String final_name = name+"_"+seqno;
			// 初始化截图线程
			ScreenshotRunnable r = new ScreenshotRunnable(v, final_name, quality);
			// 记录日志
			Log.d(LOG_TAG, "taking screenshot "+final_name);
			// 启动截图线程
			activityUtils.getCurrentActivity(false).runOnUiThread(r);
		}
		// 停掉当前线程
		public void interrupt() {
			// 标记为设置为false,停止截图
			keepRunning = false;
			super.interrupt();
		}
	}

	/**
	 * 抓取当前屏幕并发送给对应图片处理器进行相关图片处理和保存
	 * Here we have a Runnable which is responsible for taking the actual screenshot,
	 * and then posting the bitmap to a Handler which will save it.
	 *
	 * This Runnable is run on the UI thread.
	 */
	private class ScreenshotRunnable implements Runnable {
		// decorView
		private View view;
		// 文件名
		private String name;
		// 图片质量
		private int quality;
		// 构造函数
		public ScreenshotRunnable(final View _view, final String _name, final int _quality) {
			view = _view;
			name = _name;
			quality = _quality;
		}

		public void run() {
			// 如果decorView可以获取到，则截图
			if(view !=null){
				Bitmap  b;
				// 按照 View类型进行图片内容获取操作
				if(view instanceof WebView){
					b = getBitmapOfWebView((WebView) view);
				}
				else{
					b = getBitmapOfView(view);
				}
				// 如果可以获取到图片内容，则保存图片
				if(b != null)
					screenShotSaver.saveBitmap(b, name, quality);
				// 无法获取图片内容，打印相关日志
				else 
					Log.d(LOG_TAG, "NULL BITMAP!!");
			}
		}
	}

	/**
	 * 保存图片，通过异步线程完成
	 * This class is a Handler which deals with saving the screenshots on a separate thread.
	 *
	 * The screenshot logic by necessity has to run on the ui thread.  However, in practice
	 * it seems that saving a screenshot (with quality 100) takes approx twice as long
	 * as taking it in the first place. 
	 *
	 * Saving the screenshots in a separate thread like this will thus make the screenshot
	 * process approx 3x faster as far as the main thread is concerned.
	 *
	 */
	private class ScreenShotSaver extends Handler {
		// 构造函数
		public ScreenShotSaver(HandlerThread thread) {
			super(thread.getLooper());
		}

		/**
		 * 保存图片,通过消息推送
		 * bitmap  要保存的图片
		 * name    图片名
		 * quality 图片质量0-100
		 * This method posts a Bitmap with meta-data to the Handler queue.
		 *
		 * @param bitmap the bitmap to save
		 * @param name the name of the file
		 * @param quality the compression rate. From 0 (compress for lowest size) to 100 (compress for maximum quality).
		 */
		public void saveBitmap(Bitmap bitmap, String name, int quality) {
			// 初始化构造一个消息
			Message message = this.obtainMessage();
			// 初始化消息属性
			message.arg1 = quality;
			message.obj = bitmap;
			message.getData().putString("name", name);
			// 发送消息，等待处理器处理
			this.sendMessage(message);
		}

		/**
		 * 处理收到的消息
		 * Here we process the Handler queue and save the bitmaps.
		 *
		 * @param message A Message containing the bitmap to save, and some metadata.
		 */
		public void handleMessage(Message message) {
			// 获取图片名
			String name = message.getData().getString("name");
			// 获取图片质量
			int quality = message.arg1;
			// 获取图片内容
			Bitmap b = (Bitmap)message.obj;
			// 处理图片内容
			if(b != null) {
				// 保存图片到指定文件
				saveFile(name, b, quality);
				// 释放图片缓存
				b.recycle();
			}
			// 如果图片无内容，则打印日志信息
			else {
				Log.d(LOG_TAG, "NULL BITMAP!!");
			}
		}

		/**
		 * 保存结果文件
		 * Saves a file.
		 * 
		 * @param name the name of the file
		 * @param b the bitmap to save
		 * @param quality the compression rate. From 0 (compress for lowest size) to 100 (compress for maximum quality).
		 * 
		 */
		private void saveFile(String name, Bitmap b, int quality){
			// 写文件对象
			FileOutputStream fos = null;
			// 构造完整文件名
			String fileName = getFileName(name);
			// 获取系统设置的目录
			File directory = new File(config.screenshotSavePath);
			// 创建目录
			directory.mkdir();
			// 获取文件对象
			File fileToSave = new File(directory,fileName);
			try {
				// 获取文件流写对象
				fos = new FileOutputStream(fileToSave);
				if(config.screenshotFileType == ScreenshotFileType.JPEG){
					// 图片内容按照指定格式压缩，并写入指定文件，如出现异常，打印异常日志
					if (b.compress(Bitmap.CompressFormat.JPEG, quality, fos) == false){
						Log.d(LOG_TAG, "Compress/Write failed");
					}
				}
				else{
					// 图片内容按照指定格式压缩，并写入指定文件，如出现异常，打印异常日志
					if (b.compress(Bitmap.CompressFormat.PNG, quality, fos) == false){
						Log.d(LOG_TAG, "Compress/Write failed");
					}
				}
				// 关闭写文件流
				fos.flush();
				fos.close();
			} catch (Exception e) {
				// 日常记录logcat日志，并打印异常堆栈
				Log.d(LOG_TAG, "Can't save the screenshot! Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.");
				e.printStackTrace();
			}
		}
	}
}
