package org.stagex.danmaku.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keke.player.R;
import org.stagex.danmaku.adapter.ChannelAdapter;
import org.stagex.danmaku.util.AppWall;
import org.stagex.danmaku.util.SourceName;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.nmbb.oplayer.scanner.ChannelListBusiness;
import com.nmbb.oplayer.scanner.DbHelper;
import com.nmbb.oplayer.scanner.POChannelList;

public class ProvinceActivity extends Activity {
	private static final String LOGTAG = "SearchActivity";
	private ListView channel_list = null;
	private String provinceName = null;

	private List<POChannelList> listChannel = null;

	/* 频道收藏的数据库 */
	private DbHelper<POChannelList> mDbHelper;
	private Map<String, Object> mDbWhere = new HashMap<String, Object>(2);

	/* 顶部标题栏的控件 */
	private TextView button_back;

	// 更新收藏频道的数目
	private SharedPreferences sharedPreferences;
	private Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.province);

		/* 获得收藏类别的list */
		channel_list = (ListView) findViewById(R.id.channel_list);
		// 防止滑动黑屏
		channel_list.setCacheColorHint(Color.TRANSPARENT);

		/* 频道收藏的数据库 */
		mDbHelper = new DbHelper<POChannelList>();

		/* 顶部标题栏的控件 */
		button_back = (TextView) findViewById(R.id.back_btn);

		/* 设置监听 */
		setListensers();

		// 更新收藏频道的数目
		sharedPreferences = getSharedPreferences("keke_player", MODE_PRIVATE);
		editor = sharedPreferences.edit();

		Intent intent = getIntent();
		provinceName = intent.getStringExtra("province_name");
		button_back.setText(provinceName);

		setProvinceView();
	}

	/*
	 * 设置其他未分类台源的channel list
	 */
	private void setProvinceView() {
		// clear old listview
		if (listChannel != null) {
			listChannel.clear();
		}

		if (provinceName != null) {
			// get search channel list
			listChannel = ChannelListBusiness.getAllSearchChannels(
					"province_name", provinceName);
			ChannelAdapter adapter = new ChannelAdapter(this, listChannel);
			channel_list.setAdapter(adapter);
			channel_list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					POChannelList info = (POChannelList) channel_list
							.getItemAtPosition(arg2);

//					showAllSource(info.getAllUrl(), info.name,
//							info.program_path, info.save);
					// 直接进入播放界面
					startLiveMedia(info.getAllUrl(), info.name, info.save, "3", "地方频道", info.program_path);
				}
			});

			// 增加长按频道收藏功能
			channel_list
					.setOnItemLongClickListener(new OnItemLongClickListener() {

						@Override
						public boolean onItemLongClick(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							POChannelList info = (POChannelList) channel_list
									.getItemAtPosition(arg2);
							showFavMsg(arg1, info);
							return true;
						}
					});

			channel_list.setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					// TODO Auto-generated method stub

				}
			});
		}
	}

	/**
	 * 启动播放器界面
	 * 
	 * @param liveUrl
	 * @param name
	 * @param pos
	 */
	private void startLiveMedia(ArrayList<String> liveUrls, String name,
			Boolean channel_star, String sort, String sortName, String mPprograPath) {
		Intent intent = new Intent(ProvinceActivity.this,
				PlayerActivity.class);
		intent.putExtra("selected", 0);
		intent.putExtra("channelSort", sort);
		intent.putExtra("playlist", liveUrls);
		intent.putExtra("title", name);
		intent.putExtra("channelStar", channel_star);
		intent.putExtra("sortString", sortName);
		intent.putExtra("source", "线路" + Integer.toString(1) + "："
				+ SourceName.whichName(liveUrls.get(0)));
		intent.putExtra("prograPath", mPprograPath);

		startActivity(intent);
	}
	
	/**
	 * 显示所有的台源
	 */
	private void showAllSource(ArrayList<String> all_url, String name,
			String path, Boolean isStar) {
		// 如果该节目只有一个候选源地址，那么直接进入播放界面
		if (all_url.size() == 1) {
			Intent intent = new Intent(ProvinceActivity.this,
					PlayerActivity.class);
			ArrayList<String> playlist = new ArrayList<String>();
			playlist.add(all_url.get(0));
			intent.putExtra("selected", 0);
			intent.putExtra("playlist", playlist);
			intent.putExtra("title", name);
			intent.putExtra("channelStar", isStar);
			startActivity(intent);
		} else {
			// 否则进入候选源界面
			Intent intent = new Intent(ProvinceActivity.this,
					ChannelSourceActivity.class);
			intent.putExtra("all_url", all_url);
			intent.putExtra("channel_name", name);
			intent.putExtra("program_path", path);
			intent.putExtra("channelStar", isStar);
			startActivity(intent);
		}
	}

	/**
	 * 提示是否收藏
	 */
	private void showFavMsg(View view, POChannelList info) {

		final ImageView favView = (ImageView) view.findViewById(R.id.fav_icon);
		final POChannelList saveInfo = info;

		new AlertDialog.Builder(ProvinceActivity.this)
				.setIcon(R.drawable.ic_dialog_alert).setTitle("温馨提示")
				.setMessage("确定收藏该直播频道吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// do nothing - it will close on its own
						// TODO 增加加入数据库操作
						favView.setVisibility(View.VISIBLE);
						updateDatabase(saveInfo);
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
	 * 收藏后更新某一条数据信息
	 * 
	 */
	private void updateDatabase(POChannelList channelList) {
		channelList.save = true;

		// update
		Log.i(LOGTAG, "==============>" + channelList.name + "###"
				+ channelList.poId + "###" + channelList.save);

		mDbHelper.update(channelList);
	}

	// Listen for button clicks
	private void setListensers() {
		button_back.setOnClickListener(goListener);
	}

	// 按键监听
	private Button.OnClickListener goListener = new Button.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.back_btn:
				// 回到上一个界面(Activity)
				finish();
				break;
			default:
				Log.d(LOGTAG, "not supported btn id");
			}
		}
	};
	
	// =================================================
	// 加上menu
	private static final int SUPPORT_ID = Menu.FIRST + 1;
	private static final int SETUP_ID = Menu.FIRST + 2;
	private static final int APP_ID = Menu.FIRST + 3;
	
	public boolean onCreateOptionsMenu(Menu menu) {
		/*
		 * 第一个参数是groupId，如果不需要可以设置为Menu.NONE
		 * 第二个参数就是item的ID，我们可以通过menu.findItem(id)来获取具体的item
		 * 第三个参数是item的顺序，一般可采用Menu.NONE，具体看本文最后MenuInflater的部分
		 * 第四个参数是显示的内容，可以是String，或者是引用Strings.xml的ID
		 */
		menu.add(Menu.NONE, SUPPORT_ID, Menu.NONE, "帮助可可").setIcon(R.drawable.ic_tuangou_pressed);
		menu.add(Menu.NONE, SETUP_ID, Menu.NONE, "设置").setIcon(R.drawable.ic_setup2);
		menu.add(Menu.NONE, APP_ID, Menu.NONE, "热门应用").setIcon(R.drawable.ic_star);

		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) { // 获取Id
		case SUPPORT_ID:
			Intent intent1 = new Intent(ProvinceActivity.this, SupportKK.class);
			startActivity(intent1);
			break;
		case SETUP_ID:
			Intent intent2 = new Intent(ProvinceActivity.this, SetupActivity.class);
			startActivity(intent2);
			break;
		case APP_ID:	
			//获取全部自定义广告数据
			Intent appWallIntent = new Intent(this, AppWall.class);
			this.startActivity(appWallIntent);
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	// =================================================
}
