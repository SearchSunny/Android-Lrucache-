package com.view;

import java.util.Timer;

import com.activity.R;
import com.view.ViewFlow.ViewSwitchListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;


/**
 * 自定义广告View
 * @author miaowei
 *
 */
@SuppressLint({ "NewApi", "CutPasteId" })
public class AdNoticeView extends LinearLayout{
	
	private LinearLayout layout;
	private Context ctx;
	private ViewFlow noticeviewFlow;
	private AdNoticeAdapter adapter;
	private CircleFlowIndicator noticeindic ;
	
	private int adItemPotion=0;

	public AdNoticeView(Context context) {
		super(context);
		this.ctx = context;
		initView();
	}
	public AdNoticeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx = context;
		initView();
	}

	private void initView() {
		
		layout = (LinearLayout) ((LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.adnoticeview, this);
		 noticeviewFlow = (ViewFlow)layout.findViewById(R.id.noticeviewflow);  
		 noticeindic = (CircleFlowIndicator) layout.findViewById(R.id.noticeviewflowindic);
		 noticeviewFlow.setOnViewSwitchListener(new ViewSwitchListener() {
		
			public void onSwitched(View view, int position) {
				//if(position>=0)
					adItemPotion=position;
			}
		});
		 timer = new Timer();
			timer.schedule(new MyTask(), 10000, 5000);

	}
	/**
	 * 停掉timer
	 * 
	 * 必须要调用的。
	 */
	public void cancelTimer(){
		if(timer!=null)
			timer.cancel();
		timer=null;
	}
	/**
	 * 设置adapter
	 * @param adapter
	 */
	public void setAdapter(AdNoticeAdapter adapter){
		this.adapter = adapter;
		 noticeviewFlow.setAdapter(adapter);
		 noticeviewFlow.setFlowIndicator(noticeindic); 
	}
	/* If your min SDK version is < 8 you need to trigger the onConfigurationChanged in ViewFlow manually, like this */	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		noticeviewFlow.onConfigurationChanged(newConfig);		
	}
	
	private Timer timer;
	class MyTask extends java.util.TimerTask {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Message message = new Message();
			message.what = 1023;
			adHandler.sendMessage(message);
		}
	}
	Handler adHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(noticeviewFlow.getChildCount()>0){
				if(adItemPotion<(adapter.getCount()-1))
					adItemPotion++;
				else{
					adItemPotion=0;
				}
				noticeviewFlow.setSelection(adItemPotion);
				
			}
				
			Log.d("adcount", "adcount:"+noticeviewFlow.getChildCount()+"|"+adItemPotion);
			
		}
		
	};


}
