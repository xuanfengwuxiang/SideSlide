package com.itheima.sideslide.ui;



import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * 侧滑面板
 * @author xuanfengwuxiang
 *
 */
public class DragLayout extends FrameLayout {

	private ViewDragHelper mHelper;
	
	public static enum Status {
		Close, Open, Draging
	}
	private Status status = Status.Close;
	
	public interface OnDragChangeListener {
		void onClose();
		
		void onOpen();
		
		void onDraging(float percent);
	}
	
	private OnDragChangeListener onDragChangeListener;
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public OnDragChangeListener getOnDragChangeListener() {
		return onDragChangeListener;
	}

	public void setOnDragChangeListener(OnDragChangeListener onDragChangeListener) {
		this.onDragChangeListener = onDragChangeListener;
	}

	public DragLayout(Context context) {
		this(context, null);
	}

	public DragLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		// forParent 父类的容器
		// sensitivity 敏感度, 越大越敏感.1.0f是默认值
		// Callback 事件回调
		// 1. 创建 ViewDragHelper 辅助类 
		mHelper = ViewDragHelper.create(this, 1.0f, callback);

	}
	
	// 3. 接受处理的结果.
	ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
		
		// 1. 返回值, 决定了child是否可以被拖拽
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			// child 被用户拖拽的子View
			// pointerId 多点触摸的手指id
			System.out.println("tryCaptureView: ");
			return true;
		}
		
		// 2. 返回拖拽的范围. 返回一个 >0 的值, 决定了动画的执行时长, 水平方向是否可以被滑开
		@Override
		public int getViewHorizontalDragRange(View child) {
			return mRange;
		};
		
		// 3. 修正子View水平方向的位置. 此时还没有发生真正的移动.
		// 返回值决定了View将会移动到的位置
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			// child 被拖拽的子View
			// left 建议移动到的位置
			// dx 跟旧的位置的差值
//			int oldLeft = mMainContent.getLeft();
//			System.out.println("clamp: " + " left: " + left + " dx: " + dx + " oldLeft: " + oldLeft);
			
			if(child == mMainContent){
				left = fixLeft(left);
			}
			return left;
		}

		// 4. 当控件位置变化时 调用, 可以做 : 伴随动画, 状态的更新, 事件的回调.
		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			// left最新的水平方向的位置
			// dx 刚刚发生的水平变化量
//			System.out.println("onViewPositionChanged: " + " left:" + left + " dx: " + dx);
			
			if(changedView == mLeftContent){
				// 如果移动的是左面板
				// 1. 放回原来的位置
				mLeftContent.layout(0, 0, 0 + mWidth, 0 + mHeight);
				// 2. 把左面板发生的变化量dx转递给主面板
				int newLeft = mMainContent.getLeft() + dx;
				
				// 修正左边值.
				newLeft = fixLeft(newLeft);
				mMainContent.layout(newLeft, 0, newLeft + mWidth, 0 + mHeight);
			}
			
			dispatchDragEvent();
			
			// 为了兼容低版本, 手动重绘界面所有内容.
			invalidate();
		}

		
		//5. 决定了松手之后要做的事情, 结束的动画
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			super.onViewReleased(releasedChild, xvel, yvel);
			System.out.println("onViewReleased: xvel: " + xvel);
			// releasedChild 被释放的孩子
			// xvel 水平方向的速度 向右为+,  向左为-
			if(xvel == 0 && mMainContent.getLeft() > mRange * 0.5f){
				open();
			} else if (xvel > 0) {
				open();
			} else {
				close();
			}
			
		}

		@Override
		public void onViewDragStateChanged(int state) {
			super.onViewDragStateChanged(state);
		}
		
	};
	
	/**
	 * 分发拖拽事件, 伴随动画,更新状态.
	 */
	protected void dispatchDragEvent() {
		// 0.0 -> 1.0  
		float percent = mMainContent.getLeft() * 1.0f / mRange;
		System.out.println("percent: " + percent);
		
		if(onDragChangeListener != null){
			onDragChangeListener.onDraging(percent);
		}
		
		// 更新状态
		Status lastStatus = status;
		status = updateStatus(percent);
		if(lastStatus != status && onDragChangeListener != null){
			if(status == Status.Close){
				onDragChangeListener.onClose();
			}else if (status == Status.Open) {
				onDragChangeListener.onOpen();
			}
		}
		
		
		// 执行动画
		animViews(percent);
	}


	/**
	 * 更新状态
	 * @param percent 当前动画执行的百分比
	 * @return
	 */
	private Status updateStatus(float percent) {
		if(percent == 0){
			return Status.Close;
		}else if (percent == 1) {
			return Status.Open;
		}
		return Status.Draging;
	}

	private void animViews(float percent) {
		//		- 左面板: 缩放动画, 平移动画, 透明度动画
				// 缩放动画 0.0 -> 1.0  >>> 0.0 -> 0.5 >>>0.5 -> 1.0
				// percent * 0.5 + 0.5
		//		mLeftContent.setScaleX(percent * 0.5f + 0.5f);
		//		mLeftContent.setScaleY(percent * 0.5f + 0.5f);
				ViewHelper.setScaleX(mLeftContent, evaluate(percent, 0.5f, 1.0f));
				ViewHelper.setScaleY(mLeftContent, evaluate(percent, 0.5f, 1.0f));

				
		//		平移动画 -mWidth / 2.0f -> 0
				ViewHelper.setTranslationX(mLeftContent, evaluate(percent,  -mWidth / 2.0f, 0));
				
				// 透明度动画 0.2f -> 1.0
				ViewHelper.setAlpha(mLeftContent, evaluate(percent,  0.2f, 1.0f));
				
		//		- 主面板: 缩放动画 1.0 -> 0.8
				ViewHelper.setScaleX(mMainContent, evaluate(percent, 1.0f, 0.8f));
				ViewHelper.setScaleY(mMainContent, evaluate(percent, 1.0f, 0.8f));
				
		//		- 背  景: 亮度变化
				getBackground().setColorFilter((Integer)evaluateColor(percent, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
	}
	
    public Object evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int)((startA + (int)(fraction * (endA - startA))) << 24) |
                (int)((startR + (int)(fraction * (endR - startR))) << 16) |
                (int)((startG + (int)(fraction * (endG - startG))) << 8) |
                (int)((startB + (int)(fraction * (endB - startB))));
    }
	
    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }
	
	/**
	 * 修正位置
	 * @param left
	 * @return
	 */
	private int fixLeft(int left) {
		if(left < 0){
			return 0;
		}else if (left > mRange) {
			return mRange;
		}
		return left;
	}

	/**
	 * 关闭面板
	 */
	public void close() {
		close(true);
	}
	
	public void close(boolean isSmooth){
		int finalLeft = 0;
		if(isSmooth){
			// 走平滑动画
			// 1. 触发一个平滑动画.
			if(mHelper.smoothSlideViewTo(mMainContent, finalLeft, 0)){
				// 如果当前位置不是指定的最终位置. 返回true
				// 需要重绘界面, 一定要传 子View 所在的容器
				ViewCompat.postInvalidateOnAnimation(this);
			}
			
		}else {
			System.out.println("open");
			mMainContent.layout(finalLeft, 0, finalLeft + mWidth, 0 + mHeight);
		}
	}
	

	/**
	 * 打开面板
	 */
	public void open() {
		open(true);
	}
	
	public void open(boolean isSmooth){
		int finalLeft = mRange;
		if(isSmooth){
			// 走平滑动画
			// 1. 触发一个平滑动画.
			if(mHelper.smoothSlideViewTo(mMainContent, finalLeft, 0)){
				// 如果当前位置不是指定的最终位置. 返回true
				// 需要重绘界面, 一定要传 子View 所在的容器
				ViewCompat.postInvalidateOnAnimation(this);
			}
			
		}else {
			System.out.println("open");
			mMainContent.layout(finalLeft, 0, finalLeft + mWidth, 0 + mHeight);
		}
	}
	
	//2. 维持动画的继续, 高频率调用.
	@Override
	public void computeScroll() {
		super.computeScroll();
		
		if(mHelper.continueSettling(true)){
			// 如果当前位置还没有移动到最终位置. 返回true.需要继续重绘界面
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	private ViewGroup mLeftContent;
	private ViewGroup mMainContent;
	private int mHeight;
	private int mWidth;
	private int mRange;
	
	// 2. 转交触摸事件
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// 由 ViewDragHelper 判断触摸事件是否该拦截
		return mHelper.shouldInterceptTouchEvent(ev);
	};
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 由 ViewDragHelper 处理事件
		
		try {
			mHelper.processTouchEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		// 当控件尺寸变化的时候调用
		mHeight = getMeasuredHeight();
		mWidth = getMeasuredWidth();
		
		// 计算拖拽的范围
		mRange = (int) (mWidth * 0.6f);
		
		System.out.println("mWidth: " + mWidth + " mHeight: " + mHeight + " mRange: " + mRange);
		
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// 代码的健壮性.
		// 孩子至少俩
		if(getChildCount() < 2){
			throw new IllegalStateException("Your viewgroup must have 2 children. 子View至少有两个!");
		} 
		// 孩子必须是ViewGroup的子类
		if(!(getChildAt(0) instanceof ViewGroup) || !(getChildAt(1) instanceof ViewGroup)){
			throw new IllegalArgumentException("Child must be an instance of ViewGroup . 孩子必须是ViewGroup的子类");
		}
		
		// Github
		mLeftContent = (ViewGroup) getChildAt(0);
		mMainContent = (ViewGroup) getChildAt(1);
	}

}
