package com.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.activity.R;
import com.bean.ImageAD;
import com.utils.ADAsyncImageLoaderLruCache;
import com.utils.ADAsyncImageLoaderLruCache.ImageCallback;

/**
 * 广告适配
 * @author miaowei
 *
 */
public class AdNoticeAdapter extends BaseAdapter {

	/**
	 * 异步加载下载图片Lrucache方式
	 */
	private ADAsyncImageLoaderLruCache adasyncImageLoader;
	private LayoutInflater mInflater;
	private Activity ctx;
	private ArrayList<ImageAD> adLists;
	private AdNoticeView adView;

	private String [] urls = new String [4];
	
	public AdNoticeAdapter(Activity ctx, AdNoticeView adView) {
		this.ctx = ctx;
		mInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		urls[0] = "http://192.168.85.29:6555/roadcamera-web/upload/a5f5fe54s4fd54f5efef545.jpg";
		urls[1] = "http://192.168.85.29:6555/roadcamera-web/upload/200881211331729_2.jpg";
		urls[2] = "http://192.168.85.29:6555/roadcamera-web/upload/7405378_094014008000_2.jpg";
		urls[3] = "http://192.168.85.29:6555/roadcamera-web/upload/df56s4f6sf64sdf61sf65s4.jpg";
		adLists = new ArrayList<ImageAD>();
		adasyncImageLoader = new ADAsyncImageLoaderLruCache(ctx);
		this.adView = adView;
		for (int i = 0; i < urls.length; i++) {
			ImageAD ad = new ImageAD();
			ad.setAdUrl(urls[i]);
			ad.setAdName("adName"+i);
			adLists.add(ad);
		}
		//getAds();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return adLists.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		AdViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new AdViewHolder();
			convertView = mInflater.inflate(R.layout.index_ad_item, null);
			viewHolder.adimg = (ImageView) convertView
					.findViewById(R.id.index_ad_item_img);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (AdViewHolder) convertView.getTag();
		}

		if (adLists.size() == 0) {
			return null;
		}
		final ImageAD ad = adLists.get(position);

		//viewHolder.adtext.setText(ad.getAdText());
		viewHolder.adimg.setTag(ad.getAdUrl());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		//String imgID = ad.getAdId();
		
		Bitmap cachedImage = adasyncImageLoader.loadDrawable(ad.getAdUrl(),ad.getAdName(), new ImageCallback() {

					public void imageLoaded(Bitmap imageDrawable,
							String imageUrl) {
						
					   ImageView imageViewByTag = (ImageView) adView
								.findViewWithTag(imageUrl);
						Log.i("adimag",
								"Drawable cachedImage = asyncImageLoader.loadDrawable( )-->");
						// 防止图片url获取不到图片是，占位图片不见了的情况
						if (imageViewByTag != null && imageDrawable != null) { 
							imageViewByTag.setImageBitmap(imageDrawable);
						}
					}
				});
		
		
		if (cachedImage == null) {
			viewHolder.adimg.setImageResource(R.drawable.user_top_bg);
		} else {
			viewHolder.adimg.setImageBitmap(cachedImage);
		}

		viewHolder.adimg.setScaleType(ScaleType.FIT_XY);

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				
				Toast.makeText(ctx, "点击第 "+position+" 张", Toast.LENGTH_SHORT).show();
				/*if (ad.getAdAction().equals("01")) {
					//goToNotice(ad.getAdMsgId());
				} else if (ad.getAdAction().equals("02")) {
					//goToApp(ad.getAdAppid());
				} else if (ad.getAdAction().equals("03")) {
					goToBrowse(ad.getAdUrl());
				} else if (ad.getAdAction().equals("04")) {
					//goToAdvise();
				}*/
			}
		});
		return convertView;
	}

	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();
		Log.d("ad_data_change", "ad_data_change");
	}

	class AdViewHolder {
		/**
		 * 广告图片
		 */
		ImageView adimg;
	}

	void getAds() {
		AsyncTask<String, Integer, String> task = new AsyncTask<String, Integer, String>() {
			@Override
			protected void onPreExecute() {
				
			}

			@Override
			protected String doInBackground(String... params) {
				String adUrl = params[0];
				String result = "";

				Log.d("ad_data_change", "ad_data_change_back:" + result);
				return result;
			}

			@Override
			protected void onPostExecute(String result) {
				/*Log.d("ad_data_change", "ad_data_change_post:" + result);
				if (!result.equals(ctx.getResources().getString(
						R.string.conntect_time_out))) {
					try {
						JSONObject JsonResult = new JSONObject(result);
						System.out.println("jsonResult:" + JsonResult);
						setListItems(JsonResult);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
					}
				} else {
					Log.d("ad_data_change", "ad_data_change:" + result);
				}*/
				super.onPostExecute(result);
			}
		};

		//task.execute(productNo, location);
	}

	/**
	 * 添加广告列表
	 * @param jsonObject
	 * @throws JSONException
	 */
	public void setListItems(JSONObject jsonObject) throws JSONException {
		Log.d("ad_data_change",
				"ad_data_change_setListItems:"
						+ jsonObject.getString("ERRORCODE"));
		if (jsonObject.getString("ERRORCODE").equals("000000")) {
			if (!jsonObject.getString("RECORDAMOUNT").equals("0")) {
				JSONArray app_array = jsonObject.getJSONArray("ADLIST");

				for (int i = 0; i < app_array.length(); i++) {

					ImageAD ad = new ImageAD();
					
					
					adLists.add(ad);

				}

			}
			notifyDataSetChanged();
			Log.d("ad_data_change", "ad_data_change");
		} else {
			Log.d("ad_data_change",
					"ad_data_change:" + jsonObject.getString("ERRORCODE"));
		}
	}

	/**
	 * 进入公告
	 * 
	 * @param noticeid
	 */
	/*void goToNotice(String noticeid) {
		Intent intent = new Intent(ctx, More_Notice_DetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("id", noticeid);
		intent.putExtras(bundle);
		ctx.startActivity(intent);
	}*/

	/**
	 * 进入应用
	 * 
	 * @param appid
	 */
	/*void goToApp(String appid) {
		ApplicationVar var = ((ApplicationVar) ctx.getApplication());
		AppListRecordV2 apps = null;// �޸��˲��Ի����µ���������������Ӧ����
		for (AppListRecordV2 app : var.getApplist()) {
			if (app.getAppID().equals(appid)) {
				apps = app;
				break;
			}
		}
		if (apps != null) {
			String fileType = apps.getFILETYPE();
			if (("0".equals(fileType) && Util.isAppInstalled(
					apps.getAppPackageName(), ctx))
					|| ("1".equals(fileType) && Util.isUnZipPathExists(apps
							.getName()))) {
				Log.d("app_state", "app_state:" + apps.getPackageState());
				if (apps.getPackageState().equals("1")) {
					String appKey = "";
					try {
						if (Util.isAppInstalled(apps.getAppPackageName(), ctx))
							appKey = ctx.getPackageManager().getPackageInfo(
									apps.getAppPackageName(),
									PackageManager.GET_SIGNATURES).signatures[0]
									.toCharsString();
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (fileType.equals("0")) {// apk
						if ("".equals(appKey)
								|| (!"".equals(appKey) && apps.getAPPKEYSTORE()
										.equals(Util.getMD5(appKey.getBytes())))) {
							if (checkIsLogin()) {
								String appVision = "";
								try {
									appVision = ctx
											.getPackageManager()
											.getPackageInfo(
													apps.getAppPackageName(),
													PackageManager.GET_SIGNATURES).versionName
											.toString();
								} catch (NameNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								if (apps.getNewestapkver().equals(appVision)) {
									startApp(apps);
								} else {
									if (apps.getValidapkver().contains(
											appVision)) {
										showDialogAppUpadate(apps);
									} else {
										showDialogAppOptionUpadate(apps);
									}
								}

							} else {
								startLoginActivity();
							}
						} else {
							showDialogAppKeyError(apps);
						}
					} else {// html5
						if (checkIsLogin()) {
							HtmlInfoBean htmlBean = Util.getHtmlVersion(apps
									.getName());
							if (htmlBean.version.equals(apps.getVersion())) {
								if (Util.isVerifySuccess(htmlBean)) {
									startApp(apps);
								} else {
									CustomToast toast = new CustomToast(ctx,
											apps.getName() + "�ļ��ܵ��𻵣���ж����װ��ʹ��!");
									toast.show();
								}
							} else {
								if (!htmlBean.version.equals("0.0.0")
										&& apps.getValidapkver() != null
										&& apps.getValidapkver().contains(
												htmlBean.version)) {
									showDialogAppUpadate(apps);// ��ѡ����
								} else {
									showDialogAppOptionUpadate(apps);// ǿ�Ƹ���
								}
							}
						} else {
							startLoginActivity();
						}
					}
				} else if (apps.getPackageState().equals("5")) {
					showDialogAppPause(apps);
				}
			} else {
				// startAppDetail(apps, false);
				CustomToast toast = new CustomToast(ctx, "����δ��װ��ӦӦ�ã��������أ�");
				toast.show();
			}
		}

	}*/

	/**
	 * 进入浏览器 
	 */
	void goToBrowse(String Url) {
		// Intent intent = new Intent();
		// intent.setAction("android.intent.action.VIEW");
		// Uri content_uri_browsers = Uri.parse(Url);
		// intent.setData(content_uri_browsers);
		// //
		// intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");
		// ctx.startActivity(intent);

		Intent viewIntent = new Intent("android.intent.action.VIEW",
				Uri.parse(Url));
		ctx.startActivity(viewIntent);
	}

	/**
	 *进入意见反馈界面
	 */
	/*void goToAdvise() {
		// Intent intent=new Intent(ctx,More_My_Advise.class);
		Intent intent = new Intent(ctx, More_MainActivity.class);
		ctx.startActivity(intent);
	}*/

	/**
	 *进入应用
	 * 
	 * @param apps
	 */
	private void startApp(AppListRecordV2 apps) {/*
		String fileType = apps.getFILETYPE();
		ApplicationVar var = (ApplicationVar) ctx.getApplication();
		if ("0".equals(fileType)) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			ComponentName componentName = new ComponentName(
					apps.getAppPackageName(), apps.getAppPackageName()
							+ ".MainActivity");
			Bundle mBundle = new Bundle();
			mBundle.putString("APPID", apps.getAppID());// ѹ�����
			mBundle.putString("PRODUCTNO", var.getProductno());// ѹ�����
			mBundle.putString("LOCATION", var.getLocation());// ѹ�����
			mBundle.putString("REALNAMESTATUS", var.getRealNameStatus());// ���ݸ߼�ʵ����֤״̬��Ӧ��
			mBundle.putString("SIG", "");// ѹ�����
			mBundle.putString("SESSIONKEY", var.getSessionkey());
			intent.putExtras(mBundle);
			intent.setComponent(componentName);
			ctx.startActivity(intent);
		} else {
			Intent htmlIntent = new Intent(ctx, DroidHtml5.class);
			Bundle htmlBundle = new Bundle();
			htmlBundle.putString("STARTHTMLURL", apps.getStartHtmlUrl());
			htmlBundle.putString(DroidHtml5.EXTRAS_NAME_APP_NAME,
					apps.getName());
			htmlBundle.putString("APPID", apps.getAppID());// ѹ�����
			htmlBundle.putString("PRODUCTNO", var.getProductno());// ѹ�����
			htmlBundle.putString("LOCATION", var.getLocation());// ѹ�����
			htmlBundle.putString("REALNAMESTATUS", var.getRealNameStatus());// ���ݸ߼�ʵ����֤״̬��Ӧ��
			htmlBundle.putString("SIG", "");// ѹ�����
			htmlBundle.putString("SESSIONKEY", var.getSessionkey());
			htmlBundle.putBoolean("DEVELOPE_IN_ASSETS", false);
			htmlIntent.putExtras(htmlBundle);
			ctx.startActivity(htmlIntent);
		}
	*/}

	/**
	 * 进入应用详情
	 */
	// private void startAppDetail(AppListRecordV2 apps, boolean update) {
	// Intent intent = new Intent(ctx, App_NotInstall_Detail_Activity.class);
	// Bundle bundle = new Bundle();
	// bundle.putInt("app_id", Integer.parseInt(apps.getAppID()));
	// bundle.putString("app_name", apps.getName());
	// bundle.putString("app_provider", apps.getProvider());
	// bundle.putString("app_icon_url", apps.getIconUrl());
	// bundle.putString("where", "notinstall");// ���������
	// bundle.putBoolean("update_flag", update);
	// bundle.putString("packagename", apps.getAppPackageName());
	// bundle.putString("app_icon_name",
	// "app_icon_" + apps.getAppID() + apps.getIconTime() + ".png");
	// intent.putExtras(bundle);
	// ctx.startActivity(intent);
	// }
}
