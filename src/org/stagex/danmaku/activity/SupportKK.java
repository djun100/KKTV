package org.stagex.danmaku.activity;

import org.keke.player.R;

import br.com.dina.ui.widget.UITableView;
import br.com.dina.ui.widget.UITableView.ClickListener;
import cn.waps.AdView;
import cn.waps.AppConnect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SupportKK extends Activity {
	/** Called when the activity is first created. */
	private static final String LOGTAG = "SupportKK";

	/* 顶部标题栏的控件 */
	private TextView button_back;

	UITableView tableView1;
	UITableView tableView2;
	UITableView tableView3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.supportkeke);

		/* 顶部标题栏的控件 */
		button_back = (TextView) findViewById(R.id.back_btn);

		// =====================================================
		tableView1 = (UITableView) findViewById(R.id.tableView1);
		createList1();
		Log.d(LOGTAG, "total items: " + tableView1.getCount());
		tableView1.commit();

		tableView2 = (UITableView) findViewById(R.id.tableView2);
		createList2();
		Log.d(LOGTAG, "total items: " + tableView2.getCount());
		tableView2.commit();

		tableView3 = (UITableView) findViewById(R.id.tableView3);
		createList3();
		Log.d(LOGTAG, "total items: " + tableView3.getCount());
		tableView3.commit();

		/* 广告栏控件 */
		LinearLayout container = (LinearLayout) findViewById(R.id.AdLinearLayout);
		new AdView(SupportKK.this, container).DisplayAd();
		
		/* 设置监听 */
		setListensers();
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

	// ===========================================================================
	/**
	 * 采用圆角布局的ListView
	 */
	private void createList1() {
		CustomClickListener1 listener = new CustomClickListener1();
		tableView1.setClickListener(listener);

		tableView1.addBasicItem(R.drawable.ic_about, "点击横幅广告", "花几秒钟浏览广告或点击喜欢的应用");
	}

	/**
	 * 设置监听事件
	 * 
	 * @author jgf
	 * 
	 */
	private class CustomClickListener1 implements ClickListener {
		@Override
		public void onClick(int index) {

			switch (index) {
			case 0:
				// 让弹出的广告可见
				/* 广告栏控件 */
				LinearLayout container = (LinearLayout) findViewById(R.id.AdLinearLayout);
				new AdView(SupportKK.this, container).DisplayAd();
				break;
			default:
				Log.d(LOGTAG, "not supported btn id");
			}

		}
	}

	/**
	 * 采用圆角布局的ListView
	 */
	private void createList2() {
		CustomClickListener2 listener = new CustomClickListener2();
		tableView2.setClickListener(listener);

		tableView2.addBasicItem(R.drawable.ic_app, "应用商店", "第一时间到可可挑选喜欢的应用");

	}

	/**
	 * 设置监听事件
	 * 
	 * @author jgf
	 * 
	 */
	private class CustomClickListener2 implements ClickListener {
		@Override
		public void onClick(int index) {

			switch (index) {
			case 0:
				AppConnect.getInstance(SupportKK.this).showOffers(
						SupportKK.this);
				break;
			case 1:

			default:
				Log.d(LOGTAG, "not supported btn id");
			}
		}
	}

	/**
	 * 采用圆角布局的ListView
	 */
	private void createList3() {
		CustomClickListener3 listener = new CustomClickListener3();
		tableView3.setClickListener(listener);

		tableView3.addBasicItem(R.drawable.ic_tuangou, "大众团购", "利用可可提供的大众点评团购入口");
	}

	/**
	 * 设置监听事件
	 * 
	 * @author jgf
	 * 
	 */
	private class CustomClickListener3 implements ClickListener {
		@Override
		public void onClick(int index) {

			switch (index) {
			case 0:
				AppConnect.getInstance(SupportKK.this).showTuanOffers(
						SupportKK.this);
				break;
			default:
				Log.d(LOGTAG, "not supported btn id");
			}
		}
	}
	// ===========================================================================
}
