package com.itheima.sideslide;

import java.util.Random;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.itheima.sideslide.ui.DragLayout;
import com.itheima.sideslide.ui.DragLayout.OnDragChangeListener;
import com.itheima.sideslide.ui.MyLinearLayout;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public class MainActivity extends Activity {

	private DragLayout dl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		final View iv_header = findViewById(R.id.iv_header);
		iv_header.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dl.open();				
			}
		});
		
		final ListView lv_left = (ListView) findViewById(R.id.lv_left);
		lv_left.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				((TextView)view).setTextColor(Color.WHITE);
				return view;
			}
		});
		
		ListView lv_main = (ListView) findViewById(R.id.lv_main);
		lv_main.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Cheeses.NAMES));
		
		dl = (DragLayout) findViewById(R.id.dl);
		
		dl.setOnDragChangeListener(new OnDragChangeListener() {
			
			@Override
			public void onOpen() {
//				Utils.showToast(getApplicationContext(), "onOpen");
				lv_left.smoothScrollToPosition(new Random().nextInt(50));
				
			}
			
			@Override
			public void onDraging(float percent) {
//				Utils.showToast(getApplicationContext(), "onDraging: " + percent);
				// 1.0 -> 0.0  
				ViewHelper.setAlpha(iv_header, 1 - percent);
			}
			
			@Override
			public void onClose() {
//				Utils.showToast(getApplicationContext(), "onClose");
//				iv_header.setTranslationX(translationX)
				ObjectAnimator animator = ObjectAnimator.ofFloat(iv_header, "translationX", 15f);
				animator.setInterpolator(new CycleInterpolator(4));
				animator.setDuration(500);
				animator.start();
				
			}
		});
		
		MyLinearLayout ll_main = (MyLinearLayout) findViewById(R.id.ll_main);
		
		ll_main.setDragLayout(dl);
		
	}


}
