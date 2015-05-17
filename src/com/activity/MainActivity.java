package com.activity;

import com.view.AdNoticeAdapter;
import com.view.AdNoticeView;

import android.content.res.Configuration;
import android.os.Bundle;

/**
 * 广告图片
 * @author miaowei
 *
 */
public class MainActivity extends BaseActivity {
	
	private AdNoticeView adview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		
		adview = (AdNoticeView) findViewById(R.id.ad_index_adview);
		
		AdNoticeAdapter adAdapter = new AdNoticeAdapter(this, adview);
		adview.setAdapter(adAdapter);
		
	}
	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		adview.cancelTimer();// 取消定时
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		adview.onConfigurationChanged(newConfig);
	}
}
