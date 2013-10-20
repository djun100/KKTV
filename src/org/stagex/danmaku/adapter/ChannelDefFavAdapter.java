package org.stagex.danmaku.adapter;

import java.util.List;

import org.keke.player.R;

import com.nmbb.oplayer.scanner.POUserDefChannel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChannelDefFavAdapter extends BaseAdapter {
	private List<POUserDefChannel> infos;
	private Context mContext;
	// 判断是否是在播放界面切台
	private Boolean isAtPlaylist;

	private LayoutInflater mLayoutInflater;

	public ChannelDefFavAdapter(Context context, List<POUserDefChannel> infos,
			Boolean isAtPlaylist) {
		this.infos = infos;
		this.mContext = context;
		this.isAtPlaylist = isAtPlaylist;

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
		// TODO 两种模式，如果在列表界面，则加载user_load_item.xml
		// 如果在播放界面，则加载透明的user_load_item2.xml
		if (isAtPlaylist) {

			ViewHolder2 viewHolder2;
			if (convertView == null) {

				convertView = mLayoutInflater.inflate(R.layout.user_load_item2,
						null);
				viewHolder2 = new ViewHolder2();
				viewHolder2.text = (TextView) convertView
						.findViewById(R.id.channel_name);
				viewHolder2.textIndex = (TextView) convertView
						.findViewById(R.id.channel_index);
				convertView.setTag(viewHolder2);
			} else {
				viewHolder2 = (ViewHolder2) convertView.getTag();
			}

			viewHolder2.textIndex.setText(Integer.toString(position + 1));
			viewHolder2.text.setText(infos.get(position).name);
			return convertView;
		} else {
			ViewHolder viewHolder;
			if (convertView == null) {

				convertView = mLayoutInflater.inflate(R.layout.user_load_item,
						null);
				viewHolder = new ViewHolder();
				viewHolder.text = (TextView) convertView
						.findViewById(R.id.channel_name);
				viewHolder.data_txt = (TextView) convertView
						.findViewById(R.id.save_date);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.text.setText(infos.get(position).name);
			viewHolder.data_txt.setText(infos.get(position).date);
			viewHolder.data_txt.setVisibility(View.VISIBLE);
			return convertView;
		}

	}

	private class ViewHolder2 {
		TextView text;
		TextView textIndex;
	}

	private class ViewHolder {
		TextView text;
		TextView data_txt;
	}
}
