package org.stagex.danmaku.activity;

import java.io.File;

import org.keke.player.R;
import org.stagex.danmaku.util.GlobalValue;
import org.stagex.danmaku.util.Network;
import org.stagex.danmaku.util.SystemUtility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import cn.waps.AppConnect;
import cn.waps.UpdatePointsNotifier;

import org.stagex.danmaku.util.QuitPopAd;
import org.stagex.danmaku.util.AppWall;

import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.UMSsoHandler;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;

public class HomeActivity extends Activity implements UpdatePointsNotifier {
	private static final String LOGTAG = "HomeActivity";

	private LinearLayout button_local;
	private LinearLayout button_live;
	private LinearLayout button_userdef;
	private LinearLayout button_setup;

	private SharedPreferences sharedPreferences;
	private Editor editor;

	// FIXME 如果数据库变化了，根据版本号，需要在这里添加判别代码
	private int DBversion = GlobalValue.dataBaseVerion; /* 特别注意，这里要与数据库SQLiteHelperOrm.java中的版本号一致 */
	private boolean DBChanged = false;

	private String displayPointsText;
	private String currencyName = "积分";
	final Handler mHandler = new Handler();

	// 加上menu
	private static final int SUPPORT_ID = Menu.FIRST + 1;
	private static final int SETUP_ID = Menu.FIRST + 2;
	private static final int APP_ID = Menu.FIRST + 3;
	
	private UMSocialService mController;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		sharedPreferences = getSharedPreferences("keke_player", MODE_PRIVATE);
		editor = sharedPreferences.edit();

		// 判别数据库是否变化了，仅在升级或安装后，变化一次
		if (DBversion > sharedPreferences.getInt("DBversion", 0)) {
			Log.d(LOGTAG, "===>数据库版本过旧，即将更新！");
			// 版本号比之前记录的版本号高，说明变化了
			editor.putInt("DBversion", DBversion);
			editor.putBoolean("DBChanged", true);
			// 2013-07-28， 配合backup用户自定义的收藏列表
			editor.putBoolean("needSelfDevFavbkp", true);
			editor.commit();
		}

		// 判断CPU类型，如果低于ARMV6，则不让其运行
//		if (SystemUtility.getArmArchitecture() <= 6) {
		// TODO 2013-09-08 不再支持非NEON类型的手机CPU
		if (SystemUtility.getArmFeatures() < 0) {
			// 如果已经是硬解码模式，则无需设置
			boolean isHardDec = sharedPreferences
					.getBoolean("isHardDec", false);
			if (isHardDec) {
				// do nothing
			} else
				new AlertDialog.Builder(HomeActivity.this)
						.setIcon(R.drawable.ic_dialog_alert)
						.setTitle("警告")
						.setMessage(
								"抱歉！您的手机CPU缺少NEON支持\n\n请到设置中选择【硬解码】模式，且只能使用硬解码")
						// .setMessage("抱歉！软件解码库暂时不支持您的CPU")
						.setPositiveButton("设置",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// do nothing - it will close on
										// its own
										Intent intent = new Intent();
										// 跳转至设置界面
										intent.setClass(HomeActivity.this,
												SetupActivity.class);
										startActivity(intent);
									}
								}).show();
		}

		Network network = new Network(this);
		// 判断是否打开了网络
		if (network.isOpenNetwork()) {
			// 如果连接的是移动网络，对用户作出警告
			if (network.isMobileNetwork())
				new AlertDialog.Builder(HomeActivity.this)
						.setIcon(R.drawable.ic_dialog_alert)
						.setTitle("警告")
						.setMessage(
								"您正在使用2G/3G网络，由此产生的流量费用由网络运营商收取！\n\n是否切换至Wi-Fi网络？")
						.setPositiveButton("是",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// do nothing - it will close on its own
										Intent intent = null;
										try {
											intent = new Intent(
											// android.provider.Settings.ACTION_WIRELESS_SETTINGS);
											// 直接跳转到WIFI网络设置
													android.provider.Settings.ACTION_WIFI_SETTINGS);
											startActivity(intent);
										} catch (Exception e) {
											Log.w(LOGTAG,
													"open network settings failed, please check...");
											e.printStackTrace();
										}
									}
								})
						.setNegativeButton("否",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								}).show();
		} else {
			// 如果没有网络连接
			new AlertDialog.Builder(HomeActivity.this)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle("没有可用的网络")
					.setMessage("推荐您只在Wi-Fi模式下观看直播电视节目！\n\n是否对Wi-Fi网络进行设置？")
					.setPositiveButton("是",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = null;
									try {
										intent = new Intent(
										// android.provider.Settings.ACTION_WIRELESS_SETTINGS);
										// 直接跳转到WIFI网络设置
												android.provider.Settings.ACTION_WIFI_SETTINGS);
										startActivity(intent);
									} catch (Exception e) {
										Log.w(LOGTAG,
												"open network settings failed, please check...");
										e.printStackTrace();
									}
								}
							})
					.setNegativeButton("否",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							}).show();
		}

		// 启动广告
		AppConnect.getInstance(this);
		// TODO 2013-09-06
		// 初始化自定义广告数据
		AppConnect.getInstance(this).initAdInfo();
		// 初始化插屏广告数据
		AppConnect.getInstance(this).initPopAd(this);
		// end

		//========================================================
		// 2013-08-06
		// 由于新版1.3.1之后加入了积分要求在线配置的功能，所以可能会有调节功能
		// 如某段时间搞活动，要求的积分较少，过一段时间，可能要上调
		// 主要根据收益情况调整（这部分工作放到HomeActivity中去做）
		// 在线获取需要的积分参数，以便随时可以控制积分值
		String noAdPoint=AppConnect.getInstance(HomeActivity.this).getConfig("noAdPoint", "88888");
		if (sharedPreferences.getInt("pointTotal", 0) < Integer.parseInt(noAdPoint)) {
			editor.putBoolean("noAd", false);
			editor.commit();
			Log.d(LOGTAG, "===> reset Ad mode, has ad agatin");
		}
		
		// 2013-08-08
		// 为了不重复在source界面出现广告积分活动，每次启动程序只会运行一次广告活动
		// 同时，广告活动只会在有广告出现是才显示
		editor.putBoolean("showActivity", true);
		editor.commit();
		//========================================================
		
		// 创建应用程序工作目录
		File dir = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/kekePlayer");
		if (dir.exists()) {
			/* do nothing */
		} else {
			dir.mkdirs();
		}

		findViews();
		setListensers();
		
		//========================================================
		// 友盟社会化组件
		mController = UMServiceFactory.getUMSocialService("com.umeng.share",RequestType.SOCIAL);
		// 设置分享内容
		mController.setShareContent("可可电视收录全国500+电视台！可到安卓市场、小米商店等搜索“可可电视”，高清、流畅、便捷的手机电视直播体验等你来鉴定！");
		// 设置分享图片, 参数2为图片的地址
		mController.setShareMedia(new UMImage(this, "http://tv.togic.com:8080/ShowTimeService/images/182.png"));
		// 参数1为当前Activity， 参数2为用户点击分享内容时跳转到的目标地址
		mController.getConfig().supportQQPlatform(this, "http://app.xiaomi.com/detail/39492");
		mController.getConfig().setSsoHandler(new QZoneSsoHandler(this));
		//设置新浪SSO handler
		mController.getConfig().setSsoHandler(new SinaSsoHandler());
		//设置腾讯微博SSO handler
		mController.getConfig().setSsoHandler(new TencentWBSsoHandler());
		//========================================================
	}

	private void findViews() {
		button_local = (LinearLayout) findViewById(R.id.go_local);
		button_live = (LinearLayout) findViewById(R.id.go_live);
		button_userdef = (LinearLayout) findViewById(R.id.go_userdef);
		button_setup = (LinearLayout) findViewById(R.id.go_setup);
	}

	// Listen for button clicks
	private void setListensers() {
		button_local.setOnClickListener(goListener);
		button_live.setOnClickListener(goListener);
		button_userdef.setOnClickListener(goListener);
		button_setup.setOnClickListener(goListener);
	}

	private Button.OnClickListener goListener = new Button.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.go_local:
				// 标记为本地媒体
				// editor.putBoolean("isLiveMedia", false);
				// 暂时去掉本地媒体播放，所以这里仍然设置为直播电视
				editor.putBoolean("isLiveMedia", true);
				editor.commit();
				startLocalMedia();
				break;
			case R.id.go_live:
				// 标记为直播电视媒体
				editor.putBoolean("isLiveMedia", true);
				editor.commit();
				startLiveMedia();
				break;
			case R.id.go_userdef:
				startUserLoadMedia();
				break;
			case R.id.go_setup:
				startSetupMedia();
				break;
			default:
				Log.d(LOGTAG, "not supported btn id");
			}
		}
	};

	/**
	 * 本地媒体界面
	 */
	private void startLocalMedia() {
		Intent intent = new Intent();
		// intent.setClass(HomeActivity.this, FileBrowserActivity.class);
		// 启动新的媒体扫描的activity
		// intent.setClass(HomeActivity.this, MainActivity.class);
		intent.setClass(HomeActivity.this, FavouriteActivity.class);
		startActivity(intent);
	};

	/**
	 * 直播电视视频界面
	 */
	private void startLiveMedia() {
		Intent intent = new Intent();
		intent.setClass(HomeActivity.this, ChannelTabActivity.class);
		startActivity(intent);
	};

	/**
	 * 用户自定义网络视频播放界面
	 */
	private void startUserLoadMedia() {
		Intent intent = new Intent();
		intent.setClass(HomeActivity.this, UserLoadActivity.class);
		startActivity(intent);
	}

	/**
	 * 用户设置界面
	 */
	private void startSetupMedia() {
		Intent intent = new Intent();
		intent.setClass(HomeActivity.this, SetupActivity.class);
		startActivity(intent);
	}

	/**
	 * 在主界面按下返回键，提示用户是否退出应用
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 按下键盘上返回按钮
		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			new AlertDialog.Builder(this)
//					.setIcon(R.drawable.ic_lock_power_off)
//					.setTitle(R.string.prompt)
//					.setMessage(R.string.quit_desc)
//					.setNegativeButton("帮助可可",
//							new DialogInterface.OnClickListener() {
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which) {
//									// TODO 2013-09-08
//									// 改为支持可可的控件入口
//									Intent intent = new Intent(HomeActivity.this, SupportKK.class);
//									startActivity(intent);
//								}
//							})
//					.setPositiveButton(R.string.confirm,
//							new DialogInterface.OnClickListener() {
//								public void onClick(DialogInterface dialog,
//										int whichButton) {
//									finish();
//								}
//							}).show();
			// TODO 2013-09-06 调用退屏广告
			QuitPopAd.getInstance().show(this);
			
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 关闭广告
		AppConnect.getInstance(this).close();
		// System.exit(0);
		// 或者下面这种方式
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	protected void onResume() {
		// 从服务器端获取当前用户的虚拟货币.
		// 返回结果在回调函数getUpdatePoints(...)中处理
		AppConnect.getInstance(this).getPoints(this);
		super.onResume();
	}

	/**
	 * AppConnect.getPoints()方法的实现，必须实现
	 * 
	 * @param currencyName
	 *            虚拟货币名称.
	 * @param pointTotal
	 *            虚拟货币余额.
	 */
	public void getUpdatePoints(String currencyName, int pointTotal) {
		this.currencyName = currencyName;
		displayPointsText = currencyName + ": " + pointTotal;
		// 保存积分值
		editor.putInt("pointTotal", pointTotal);
		editor.commit();
		Log.d(LOGTAG, "===>" + displayPointsText);
	}

	/**
	 * AppConnect.getPoints() 方法的实现，必须实现
	 * 
	 * @param error
	 *            请求失败的错误信息
	 */
	public void getUpdatePointsFailed(String error) {
		displayPointsText = error;
		Log.e(LOGTAG, "===>" + displayPointsText);
	}
	
	// =================================================

	public boolean onCreateOptionsMenu(Menu menu) {
		/*
		 * 第一个参数是groupId，如果不需要可以设置为Menu.NONE
		 * 第二个参数就是item的ID，我们可以通过menu.findItem(id)来获取具体的item
		 * 第三个参数是item的顺序，一般可采用Menu.NONE，具体看本文最后MenuInflater的部分
		 * 第四个参数是显示的内容，可以是String，或者是引用Strings.xml的ID
		 */
		menu.add(Menu.NONE, SUPPORT_ID, Menu.NONE, "帮助可可");
//		menu.add(Menu.NONE, SETUP_ID, Menu.NONE, "设置");
		menu.add(Menu.NONE, SETUP_ID, Menu.NONE, "一键分享");
		menu.add(Menu.NONE, APP_ID, Menu.NONE, "热门应用");

		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) { // 获取Id
		case SUPPORT_ID:
			Intent intent1 = new Intent(HomeActivity.this, SupportKK.class);
			startActivity(intent1);
			break;
		case SETUP_ID:
//			Intent intent2 = new Intent(HomeActivity.this, SetupActivity.class);
//			startActivity(intent2);
			// 打开平台选择面板，参数2为打开分享面板时是否强制登录,false为不强制登录
	        mController.openShare(HomeActivity.this, false);
			break;
		case APP_ID:	
			//获取全部自定义广告数据
			Intent appWallIntent = new Intent(this, AppWall.class);
			this.startActivity(appWallIntent);
			break;
		default:
		}
		return super.onOptionsItemSelected(item);
	}
	// =================================================
	
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    /**使用SSO授权必须添加如下代码 */
	    UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode) ;
	    if(ssoHandler != null){
	       ssoHandler.authorizeCallBack(requestCode, resultCode, data);
	    }
	}
}
