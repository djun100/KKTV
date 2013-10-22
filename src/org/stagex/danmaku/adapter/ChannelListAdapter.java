package org.stagex.danmaku.adapter;

import java.util.List;

import org.keke.player.R;

import com.nmbb.oplayer.scanner.POChannelList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("ResourceAsColor")
public class ChannelListAdapter extends BaseAdapter {
	private List<POChannelList> infos;
	private Context mContext;

	private LayoutInflater mLayoutInflater;
	
	public int mCurrentIndex = -1;

	public ChannelListAdapter(Context context, List<POChannelList> infos) {
		this.infos = infos;
		this.mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return infos.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return infos.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.channel_list_item2,
					null);

			viewHolder = new ViewHolder();

			viewHolder.textName = (TextView) convertView
					.findViewById(R.id.channel_name);
			viewHolder.textIndex = (TextView) convertView
					.findViewById(R.id.channel_index);
			viewHolder.hotView = (ImageView) convertView
					.findViewById(R.id.hot_icon);
			viewHolder.newView = (ImageView) convertView
					.findViewById(R.id.new_icon);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.textName.setText(infos.get(position).name);
		viewHolder.textIndex.setText(Integer.toString(position + 1));
		// Log.d("channelList", "===> list position" + position);
		// 标记当前正在播放的频道
		if (mCurrentIndex == position) {
			// FIXME 2013-10-22 好像只能用Color.YELLOW，否则颜色不对
			viewHolder.textName.setTextColor(Color.YELLOW);
			viewHolder.textIndex.setTextColor(Color.YELLOW);
		} else {
			viewHolder.textName.setTextColor(Color.WHITE);
			viewHolder.textIndex.setTextColor(Color.WHITE);
		}

		// 判断是否是热门频道，暂时使用HOT字样
		if (infos.get(position).mode.equalsIgnoreCase("HOT"))
			viewHolder.hotView.setVisibility(View.VISIBLE);
		else
			viewHolder.hotView.setVisibility(View.GONE);
		// 判断是否是新频道，暂时用NEW字样
		if (infos.get(position).mode.equalsIgnoreCase("NEW"))
			viewHolder.newView.setVisibility(View.VISIBLE);
		else
			viewHolder.newView.setVisibility(View.GONE);

		return convertView;
	}

	private class ViewHolder {
		TextView textName;
		TextView textIndex;
		ImageView hotView;
		ImageView newView;
	}
}
