package org.stagex.danmaku.activity;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keke.player.R;
import org.stagex.danmaku.adapter.ChannelAdapter;
import org.stagex.danmaku.adapter.ChannelDefFavAdapter;
import org.stagex.danmaku.adapter.ChannelInfo;
import org.stagex.danmaku.util.AppWall;
import org.stagex.danmaku.util.BackupData;
import org.stagex.danmaku.util.SourceName;
import org.stagex.danmaku.util.saveFavName;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TabHost.OnTabChangeListener;
import cn.waps.AppConnect;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.nmbb.oplayer.scanner.ChannelListBusiness;
import com.nmbb.oplayer.scanner.DbHelper;
import com.nmbb.oplayer.scanner.POChannelList;
import com.nmbb.oplayer.scanner.POUserDefChannel;
import com.nmbb.oplayer.scanner.SQLiteHelperOrm;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.UMSsoHandler;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;

@SuppressLint("NewApi")
public class FavouriteActivity extends TabActivity implements
		OnTabChangeListener {

	private static final String LOGTAG = "FavouriteActivity";
	private ListView fav_list;
	private ListView selffav_list;

	private List<POChannelList> fav_infos = null;
	private ChannelDefFavAdapter mSourceAdapter;
	private List<POUserDefChannel> infos;
	
	/* 频道收藏的数据库 */
	private DbHelper<POChannelList> mDbHelper;
	private DbHelper<POUserDefChannel> mSelfDbHelper;
	private Map<String, Object> mDbWhere = new HashMap<String, Object>(2);

	/* 顶部标题栏的控件 */
	private TextView button_back;
	private ImageView button_home;
	private ImageView button_delete;
	
	// 更新收藏频道的数目
	private SharedPreferences sharedPreferences;
	private Editor editor;
	private int fav_num = 0;

	private TabHost myTabhost;
	TextView view0, view1, view2;
	private Boolean selfView = false;

	private WebView mWebView;
	
	private UMSocialService mController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tab_fav);

		/* 获得收藏类别的list */
		fav_list = (ListView) findViewById(R.id.fav_list);
		// 防止滑动黑屏
		fav_list.setCacheColorHint(Color.TRANSPARENT);

		/* 频道收藏的数据库 */
		mDbHelper = new DbHelper<POChannelList>();
		/* 自定义收藏的数据库 */
		mSelfDbHelper = new DbHelper<POUserDefChannel>();

		/* 顶部标题栏的控件 */
		button_back = (TextView) findViewById(R.id.back_btn);
		button_home = (ImageView) findViewById(R.id.home_btn);
		button_delete = (ImageView) findViewById(R.id.delete_btn);
		
		mWebView = (WebView) findViewById(R.id.wv);
		
		/* 设置监听 */
		setListensers();

		// 更新收藏频道的数目
		sharedPreferences = getSharedPreferences("keke_player", MODE_PRIVATE);
		editor = sharedPreferences.edit();

		/* ========================================================= */
		/* 2013-07-28
		 *  软件第一次启动之后，会创建一个标志位needSelfDevFavbkp（该标志为在用户
		 *  卸载软件时会消失）
		 * 如果标志位为false，则说明是覆盖安装，也就是说，数据库的数据还在，那么就
		 * 不需要将自定义的收藏频道从SD卡的工作目录下backup出来（如果是首次使用，
		 * 那么肯定之前也没自定义过，也就肯定不许要restore）
		 * 如果为true，那么需要检测SD工作目录下是否有selfDefineTVList文件，如果有的
		 * 话，将其中的数据读出来，加入到数据库中去。
		 * （另外一种情况，如果软件的数据库版本升级了，官方的收藏可以先不管，用户
		 * 自定义的数据库将会清空，这种情况下也应该要backup数据, 而这个标志位的清空
		 * 就由主界面启动时置位为true）
		 */
		if (sharedPreferences.getBoolean("needSelfDevFavbkp", false)) {
			Log.d(LOGTAG, "===> needSelfDevFavbkp");
//			new BackupData(FavouriteActivity.this).execute("restoreDatabase");
			new BackupData().execute("restoreDatabase");
			editor.putBoolean("needSelfDevFavbkp", false);
			editor.commit();
		}
		/* ========================================================= */
		
		// ===============================================================
		if (sharedPreferences.getBoolean("no_fav_help", false) == false) {
			new AlertDialog.Builder(FavouriteActivity.this)
					.setIcon(R.drawable.ic_dialog_alert)
					.setTitle("温馨提示")
					.setMessage(
							"长按频道名称可以实现收藏或取消收藏")
					.setPositiveButton("不再提醒",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 不再收藏
									editor.putBoolean("no_fav_help", true);
									editor.commit();
								}
							})
					.setNegativeButton("知道了",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							}).show();
		}
		// ===============================================================
		myTabhost = this.getTabHost();
		myTabhost.setup();

//		myTabhost.setBackgroundDrawable(this.getResources().getDrawable(
//				R.drawable.bg_home));
		
		/* 设置每一个台类别的Tab */
		RelativeLayout tab0 = (RelativeLayout) LayoutInflater.from(this)
				.inflate(R.layout.tab_fav_ctx, null);
		view0 = (TextView) tab0.findViewById(R.id.tab_label);
		view0.setText("官方收藏");
		myTabhost.addTab(myTabhost.newTabSpec("One")// make a new Tab
				.setIndicator(tab0)
				// set the Title and Icon
				.setContent(R.id.fav_list));
		// set the layout

		RelativeLayout tab1 = (RelativeLayout) LayoutInflater.from(this)
				.inflate(R.layout.tab_fav_ctx, null);
		view1 = (TextView) tab1.findViewById(R.id.tab_label);
		view1.setText("自定义收藏");
		myTabhost.addTab(myTabhost.newTabSpec("Two")// make a new Tab
				.setIndicator(tab1)
				// set the Title and Icon
				.setContent(R.id.selffav_list));
		// set the layout

		RelativeLayout tab2 = (RelativeLayout) LayoutInflater.from(this)
				.inflate(R.layout.tab_fav_ctx, null);
		view2 = (TextView) tab2.findViewById(R.id.tab_label);
		view2.setText("帮助");
		myTabhost.addTab(myTabhost.newTabSpec("Three")// make a new Tab
				.setIndicator(tab2)
				// set the Title and Icon
				.setContent(R.id.selffav_list));
		// set the layout
		
		/* 设置Tab的监听事件 */
		myTabhost.setOnTabChangedListener(this);
		
		/* 获得各个台类别的list */
		fav_list = (ListView) findViewById(R.id.fav_list);
		// 防止滑动黑屏
		fav_list.setCacheColorHint(Color.TRANSPARENT);

		selffav_list = (ListView) findViewById(R.id.selffav_list);
		// 防止滑动黑屏
		fav_list.setCacheColorHint(Color.TRANSPARENT);
		
		// 默认显示第一个标签
		view0.setTextSize(20);
		view1.setTextSize(15);
		view2.setTextSize(15);
		// ===============================================================
		
		setFavView();
//		setSelfFavView();
		
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

	@Override
	public void onTabChanged(String tagString) {
		// TODO Auto-generated method stub
		if (tagString.equals("One")) {
			selfView = false;
			mWebView.setVisibility(View.GONE);
			fav_list.setVisibility(View.VISIBLE);
			view0.setTextSize(20);
			view1.setTextSize(15);
			view2.setTextSize(15);
		}
		if (tagString.equals("Two")) {
			selfView = true;
			mWebView.setVisibility(View.GONE);
			selffav_list.setVisibility(View.VISIBLE);
			view0.setTextSize(15);
			view1.setTextSize(20);
			view2.setTextSize(15);

			setSelfFavView();
		}
		if (tagString.equals("Three")) {
			selfView = false;
			view0.setTextSize(15);
			view1.setTextSize(15);
			view2.setTextSize(20);
			readHtmlFormAssets();
		}
	}
	
	/*
	 * 设置其他未分类台源的channel list
	 */
	private void setFavView() {
		fav_infos = ChannelListBusiness.getAllFavChannels();
		ChannelAdapter adapter = new ChannelAdapter(this, fav_infos);
		fav_list.setAdapter(adapter);
		fav_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				POChannelList info = (POChannelList) fav_list
						.getItemAtPosition(arg2);
				// Log.d("ChannelInfo",
				// "name = " + info.getName() + "[" + info.getUrl() + "]");

				// startLiveMedia(info.getUrl(), info.getName());
//				showAllSource(info.getAllUrl(), info.name, info.program_path,
//						info.save);
				startLiveMedia(info.getAllUrl(), info.name, info.save, "收藏频道", info.program_path);
			}
		});

		// 增加长按频道收藏功能
		fav_list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				POChannelList info = (POChannelList) fav_list
						.getItemAtPosition(arg2);
				ClearFavMsg(arg1, info);
				return true;
			}
		});

		fav_list.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void setSelfFavView() {
		/* 获取所有的自定义收藏频道 */
		infos = ChannelListBusiness.getAllDefFavChannels();

		mSourceAdapter = new ChannelDefFavAdapter(this, infos, false);
		selffav_list.setAdapter(mSourceAdapter);
		// 设置监听事件
		selffav_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				POUserDefChannel info = (POUserDefChannel) selffav_list
						.getItemAtPosition(arg2);

				startSelfLiveMedia(info.getAllUrl(), info.name, true);
			}
		});
		// 增加长按频道删除收藏的个性频道
		selffav_list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				POUserDefChannel info = (POUserDefChannel) selffav_list
						.getItemAtPosition(arg2);
				showFavMsg(arg1, info);
				return true;
			}
		});
	}
	
	/**
	 * 启动播放器界面（收藏频道）
	 * 
	 * @param liveUrl
	 * @param name
	 * @param pos
	 */
	private void startLiveMedia(ArrayList<String> liveUrls, String name,
			Boolean channel_star, String sortName, String mPprograPath) {
		Intent intent = new Intent(FavouriteActivity.this,
				PlayerActivity.class);
		intent.putExtra("selected", 0);
		intent.putExtra("favSort", true);
		intent.putExtra("playlist", liveUrls);
		intent.putExtra("title", name);
		intent.putExtra("channelStar", channel_star);
		intent.putExtra("sortString", sortName);
		// FIXME 2013-09-28 增加了播放界面的分类切台，需要分类序号
		intent.putExtra("channelSort", "7");
		intent.putExtra("source", "线路" + Integer.toString(1) + "："
				+ SourceName.whichName(liveUrls.get(0)));
		intent.putExtra("prograPath", mPprograPath);

		startActivity(intent);
	}
	
//	/**
//	 * 显示所有的台源
//	 */
//	private void showAllSource(ArrayList<String> all_url, String name,
//			String path, Boolean isStar) {
//		// 如果该节目只有一个候选源地址，那么直接进入播放界面
//		if (all_url.size() == 1) {
//			Intent intent = new Intent(FavouriteActivity.this,
//					PlayerActivity.class);
//			ArrayList<String> playlist = new ArrayList<String>();
//			playlist.add(all_url.get(0));
//			intent.putExtra("selected", 0);
//			intent.putExtra("playlist", playlist);
//			intent.putExtra("title", name);
//			intent.putExtra("channelStar", isStar);
//			startActivity(intent);
//		} else {
//			// 否则进入候选源界面
//			Intent intent = new Intent(FavouriteActivity.this,
//					ChannelSourceActivity.class);
//			intent.putExtra("all_url", all_url);
//			intent.putExtra("channel_name", name);
//			intent.putExtra("program_path", path);
//			intent.putExtra("channelStar", isStar);
//			startActivity(intent);
//		}
//	}

	// 打开自定义的网络媒体
	private void startSelfLiveMedia(ArrayList<String> all_url, String name, Boolean isStar) {
//		// 如果该节目只有一个候选源地址，那么直接进入播放界面
//		if (all_url.size() == 1) {
//			Intent intent = new Intent(FavouriteActivity.this,
//					PlayerActivity.class);
//			ArrayList<String> playlist = new ArrayList<String>();
//			playlist.add(all_url.get(0));
//			intent.putExtra("selected", 0);
//			intent.putExtra("playlist", playlist);
//			intent.putExtra("title", name);
//			intent.putExtra("isSelfTV", true);
//			intent.putExtra("channelStar", isStar);
//			startActivity(intent);
//		} else {
			// 否则进入候选源界面
			Intent intent = new Intent(FavouriteActivity.this,
					PlayerActivity.class);
			intent.putExtra("selected", 0);
			intent.putExtra("playlist", all_url);
			intent.putExtra("title", name);
			// 标识是自定义的收藏频道
			intent.putExtra("isSelfFavTV", true);
			intent.putExtra("channelStar", isStar);
			intent.putExtra("sortString", "自定义收藏");
			// FIXME 2013-09-28 增加了播放界面的分类切台，需要分类序号
			intent.putExtra("channelSort", "9");
			intent.putExtra("source", "线路" + Integer.toString(1) + "："
					+ SourceName.whichName(all_url.get(0)));
			
			startActivity(intent);
//		}
	}
	
	/**
	 * 提示是否取消官方收藏
	 */
	private void ClearFavMsg(View view, POChannelList info) {
		final POChannelList saveInfo = info;

		fav_num = sharedPreferences.getInt("fav_num", 0);
		Log.d(LOGTAG, "===>current fav_num = " + fav_num);

		new AlertDialog.Builder(FavouriteActivity.this)
				.setIcon(R.drawable.ic_dialog_alert).setTitle("温馨提示")
				.setMessage("确定取消该直播频道的收藏吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 收藏数目减1
						editor.putInt("fav_num", fav_num - 1);
						editor.commit();
						// TODO 增加加入数据库操作
						clearDatabase(saveInfo);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
	}

	/**
	 * 提示是否删除收藏的个性频道
	 */
	private void showFavMsg(View view, POUserDefChannel info) {

		final POUserDefChannel saveInfo = info;

		new AlertDialog.Builder(FavouriteActivity.this)
				.setIcon(R.drawable.ic_dialog_alert).setTitle("温馨提示")
				.setMessage("确定删除该自定义频道吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO 从数据库中删除一条数据
						mSelfDbHelper.remove(saveInfo);
						// 重新加载list
						if (infos != null) {
							infos.clear();
							setSelfFavView();
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
	}
	
	/**
	 * 提示是否取消所有官方收藏
	 */
	private void clearAllFavMsg() {
		new AlertDialog.Builder(FavouriteActivity.this)
				.setIcon(R.drawable.ic_dialog_alert)
				.setTitle("警告")
				.setMessage("确定取消所有【官方】收藏频道吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO 取消所有收藏频道
						List<POChannelList> allFavChannel = ChannelListBusiness
								.getAllFavChannels();
						int size = allFavChannel.size();
						POChannelList favChannel;
						for (int i = 0; i < size; i++) {
							favChannel = allFavChannel.get(i);
							favChannel.save = false;
							mDbHelper.update(favChannel);
						}
						// 重新加載listView
						reloadFavList();

						// 收藏数目置为0
						editor.putInt("fav_num", 0);
						editor.commit();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
	}

	/**
	 * 取消官方收藏
	 * 
	 * @throws FileNotFoundException
	 */
	private void clearDatabase(POChannelList channelList) {
		channelList.save = false;
		// update
		Log.i(LOGTAG, "==============>" + channelList.name + "###"
				+ channelList.poId + "###" + channelList.save);
		mDbHelper.update(channelList);

		// 重新加載listView
		reloadFavList();
	}

	// 重新加載listView
	private void reloadFavList() {
		fav_infos.clear();
		setFavView();
	}

	/**
	 * 提示是否取消所有自定义收藏
	 */
	private void clearAllSelfFavMsg() {
		new AlertDialog.Builder(FavouriteActivity.this)
				.setIcon(R.drawable.ic_dialog_alert)
				.setTitle("警告")
				.setMessage("确定取消所有【自定义】收藏频道吗？\n注意：删除后将不可恢复！")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO 取消所有自定义收藏频道
						SQLiteHelperOrm db = new SQLiteHelperOrm();
						// FIXME 此种方式，效率很高，很快，直接删除所有行数据
						db.getWritableDatabase().delete("UserDefChannel", null, null);
						if (db != null)
							db.close();
						
						// 重新加载list
						if (infos != null) {
							infos.clear();
							setSelfFavView();
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
	}
	
	// Listen for button clicks
	private void setListensers() {
		button_home.setOnClickListener(goListener);
		button_delete.setOnClickListener(goListener);
		button_back.setOnClickListener(goListener);
	}

	// 按键监听
	private Button.OnClickListener goListener = new Button.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.back_btn:
				// 回到上一个界面(Activity)
				
				new BackupData().execute("backupDatabase");
				
				finish();
				break;
			case R.id.home_btn:
				// 回到上一个界面(Activity)
				
				new BackupData().execute("backupDatabase");
				
				finish();
				break;
			case R.id.delete_btn:
				// 删除所有的收藏的频道
				// FIXME 需要区分，删除官方收藏还是自定义的收藏
				// 官方收藏
				if (!selfView)
					clearAllFavMsg();
				// 自定义收藏
				else
					clearAllSelfFavMsg();
				// TODO
				break;
			default:
				Log.d(LOGTAG, "not supported btn id");
			}
		}
	};
	
	/**
	 * 在主界面按下返回键，提示用户是否退出应用
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 按下键盘上返回按钮
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// TODO 暂时在每次返回的时候，都进行备份数据库
//		    new BackupData(UserLoadActivity.this).execute("backupDatabase");
		    new BackupData().execute("backupDatabase");
		 // TODO 暂时在每次返回的时候，都进行备份数据库
		    new saveFavName().execute("backupDatabase");
		    
		    finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	// 利用webview来显示帮助的文本信息
	private void readHtmlFormAssets() {
		fav_list.setVisibility(View.GONE);
		selffav_list.setVisibility(View.GONE);
		mWebView.setVisibility(View.VISIBLE);
		WebSettings webSettings = mWebView.getSettings();

		webSettings.setLoadWithOverviewMode(true);
		// WebView双击变大，再双击后变小，当手动放大后，双击可以恢复到原始大小
		// webSettings.setUseWideViewPort(true);
		// 设置WebView可触摸放大缩小：
		// webSettings.setBuiltInZoomControls(true);
		// WebView 背景透明效果
		mWebView.setBackgroundColor(Color.TRANSPARENT);
		mWebView.loadUrl("file:///android_asset/html/SelfFavTVList_help.html");
	}
	
	// =================================================
	// 加上menu
	private static final int SUPPORT_ID = Menu.FIRST + 1;
	private static final int SETUP_ID = Menu.FIRST + 2;
	private static final int APP_ID = Menu.FIRST + 3;
	private static final int SHARE_ID = Menu.FIRST + 4;
	
	public boolean onCreateOptionsMenu(Menu menu) {
		/*
		 * 第一个参数是groupId，如果不需要可以设置为Menu.NONE
		 * 第二个参数就是item的ID，我们可以通过menu.findItem(id)来获取具体的item
		 * 第三个参数是item的顺序，一般可采用Menu.NONE，具体看本文最后MenuInflater的部分
		 * 第四个参数是显示的内容，可以是String，或者是引用Strings.xml的ID
		 */
		menu.add(Menu.NONE, SUPPORT_ID, Menu.NONE, "帮助可可");
		menu.add(Menu.NONE, SETUP_ID, Menu.NONE, "设置");
		menu.add(Menu.NONE, APP_ID, Menu.NONE, "热门应用");
		menu.add(Menu.NONE, SHARE_ID, Menu.NONE, "一键分享");

		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) { // 获取Id
		case SUPPORT_ID:
			Intent intent1 = new Intent(FavouriteActivity.this, SupportKK.class);
			startActivity(intent1);
			break;
		case SETUP_ID:
			Intent intent2 = new Intent(FavouriteActivity.this, SetupActivity.class);
			startActivity(intent2);
			break;
		case APP_ID:	
			//获取全部自定义广告数据
			Intent appWallIntent = new Intent(this, AppWall.class);
			this.startActivity(appWallIntent);
			break;
		case SHARE_ID:	
			// 打开平台选择面板，参数2为打开分享面板时是否强制登录,false为不强制登录
	        mController.openShare(FavouriteActivity.this, false);
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
