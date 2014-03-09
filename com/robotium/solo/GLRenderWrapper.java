package com.robotium.solo;

import java.nio.IntBuffer;
import java.util.concurrent.CountDownLatch;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.view.View;

/**
 * 构造定制化的renderer做页面渲染,用作截屏使用
 * Used to wrap and replace the renderer to gain access to the gl context.  
 * 
 * @author Per-Erik Bergman, bergman@uncle.se
 * 
 */

class GLRenderWrapper implements Renderer {
	// 变量缓存渲染器,用于存储系统原有的渲染器
	private Renderer renderer;
	// 宽度
	private int width;
	// 高度
	private int height;
	// 图形操作接口
	private final GLSurfaceView view;
	// 原子计数器，同步线程操作
	private CountDownLatch latch;
	// 设置是否要截屏
	private boolean takeScreenshot = true;
	// 获取GL版本
	private int glVersion;

	/**
	 * 构造函数
	 * Constructs this object.
	 * 
	 * @param view the current glSurfaceView
	 * @param renderer the renderer to wrap
	 * @param latch the count down latch
	 */
	
	public GLRenderWrapper(GLSurfaceView view,
			Renderer renderer, CountDownLatch latch) {
		this.view = view;
		this.renderer = renderer;
		this.latch = latch;
		// 设置宽度
		this.width = view.getWidth();
		// 设置高度
		this.height = view.getHeight();
		// 通过反射获取GL版本信息
		Integer out = new Reflect(view).field("mEGLContextClientVersion")
				.out(Integer.class);
		// 可以获取GL版本，那么设置对应的版本，否则设置版本为-1,设置截图操作为false
		if ( out != null ) {
			this.glVersion = out.intValue();
		} else {
			this.glVersion = -1;
			this.takeScreenshot = false;
		}
	}

	@Override
	/* 不修改，调用默认实现
	 * (non-Javadoc)
	 * @see android.opengl.GLSurfaceView.Renderer#onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
	 */
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		renderer.onSurfaceCreated(gl, config);
	}

	@Override
	/* 获取相关的高度和宽度，调用继续使用原有的
	 * (non-Javadoc)
	 * @see android.opengl.GLSurfaceView.Renderer#onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int)
	 */
	
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		renderer.onSurfaceChanged(gl, width, height);
	}

	@Override
	/* 修改绘图内容渲染完成后保存渲染的界面内容
	 * (non-Javadoc)
	 * @see android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.microedition.khronos.opengles.GL10)
	 */
	
	public void onDrawFrame(GL10 gl) {
		// 调用原有的绘图渲染
		renderer.onDrawFrame(gl);
		// 如果设置了截图
		if (takeScreenshot) {
			// 图片缓存变量
			Bitmap screenshot = null;
			// 按照 GL版本，调用对应的图片处理方法
			if (glVersion >= 2) {
				screenshot = savePixels(0, 0, width, height);
			} else {
				screenshot = savePixels(0, 0, width, height, gl);
			}
			// 处理图片，把图片内容返回给对应view对象
			new Reflect(view).field("mDrawingCache").type(View.class)
					.in(screenshot);
			// 释放锁对象
			latch.countDown();
			// 标志为未截图
			takeScreenshot = false;
		}
	}

	/**
	 * 设置是否需要截图
	 * Tell the wrapper to take a screen shot 
	 */
	
	public void setTakeScreenshot() {
		takeScreenshot = true;
	}

	/**
	 * 设置计数器
	 * Set the count down latch 
	 */

	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}

	/**
	 * 获取图像保存为bitmap
	 * Extract the bitmap from OpenGL 
	 * 
	 * @param x the start column
	 * @param y the start line
	 * @param w the width of the bitmap
	 * @param h the height of the bitmap
	 */
	
	private Bitmap savePixels(int x, int y, int w, int h) {
		// 存储图片内容
		int b[] = new int[w * (y + h)];
		int bt[] = new int[w * h];
		IntBuffer ib = IntBuffer.wrap(b);
		ib.position(0);
		// 处理图片
		GLES20.glReadPixels(x, 0, w, y + h, GLES20.GL_RGBA,
				GLES20.GL_UNSIGNED_BYTE, ib);
		// 处理成指定的高度和宽度
		for (int i = 0, k = 0; i < h; i++, k++) {
			// remember, that OpenGL bitmap is incompatible with Android bitmap
			// and so, some correction need.
			for (int j = 0; j < w; j++) {
				int pix = b[i * w + j];
				int pb = (pix >> 16) & 0xff;
				int pr = (pix << 16) & 0x00ff0000;
				int pix1 = (pix & 0xff00ff00) | pr | pb;
				bt[(h - k - 1) * w + j] = pix1;
			}
		}
		// 处理成BitMap
		Bitmap sb = Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
		return sb;
	}

	/**
	 * 处理图片
	 * Extract the bitmap from OpenGL 
	 * 
	 * @param x the start column
	 * @param y the start line
	 * @param w the width of the bitmap
	 * @param h the height of the bitmap
	 * @param gl the current GL reference
	 */
	
	private static Bitmap savePixels(int x, int y, int w, int h, GL10 gl) {
		// 缓存像素内容
		int b[] = new int[w * (y + h)];
		int bt[] = new int[w * h];
		IntBuffer ib = IntBuffer.wrap(b);
		ib.position(0);
		// 处理图片
		gl.glReadPixels(x, 0, w, y + h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);
		// 处理成指定的高度和宽度
		for (int i = 0, k = 0; i < h; i++, k++) {
			// remember, that OpenGL bitmap is incompatible with Android bitmap
			// and so, some correction need.
			for (int j = 0; j < w; j++) {
				int pix = b[i * w + j];
				int pb = (pix >> 16) & 0xff;
				int pr = (pix << 16) & 0x00ff0000;
				int pix1 = (pix & 0xff00ff00) | pr | pb;
				bt[(h - k - 1) * w + j] = pix1;
			}
		}
		//    处理成Bitmap
		Bitmap sb = Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
		return sb;
	}

}
