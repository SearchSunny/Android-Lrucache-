package com.bean;
/**
 * 图片广告
 * @author miaowei
 *
 */
public class ImageAD {

	/**
	 * 广告图片URL
	 */
	private String adUrl;
	/**
	 * 广告类型(1/任务 跳转至抢单页面 2/活动 跳转至浏览器页面),根据类型跳转至对应界面
	 */
	private String adAction;
	
	/**
	 * 广告名称
	 */
	private String adName;

	public String getAdUrl() {
		return adUrl;
	}

	public void setAdUrl(String adUrl) {
		this.adUrl = adUrl;
	}

	public String getAdAction() {
		return adAction;
	}

	public void setAdAction(String adAction) {
		this.adAction = adAction;
	}

	public String getAdName() {
		return adName;
	}

	public void setAdName(String adName) {
		this.adName = adName;
	}
	
	
	
	
}
