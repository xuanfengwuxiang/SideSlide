package com.itheima.sideslide.ui;



import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class MyLinearLayout extends LinearLayout {

	private DragLayout draglayout;

	public MyLinearLayout(Context context) {
		super(context);
	}

	public MyLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		if(draglayout != null && draglayout.getStatus() != DragLayout.Status.Close){
			// 不是关闭状态 , 直接拦截, 不往下传递了
			return true;
		}else {
			// 如果当前是关闭状态, 按原来的判断处理
			return super.onInterceptTouchEvent(ev);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(draglayout != null && draglayout.getStatus() != DragLayout.Status.Close){
			// 如果手指抬起, 执行关闭动画
			if(event.getAction() == MotionEvent.ACTION_UP){
				draglayout.close();
			}
			return true;
		} else {
			return super.onTouchEvent(event);
		}
	}

	public void setDragLayout(DragLayout draglayout) {
		this.draglayout = draglayout;
	}
	

}
