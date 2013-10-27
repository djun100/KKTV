package org.stagex.danmaku.adapter;

import java.util.List;

import org.keke.player.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

@SuppressLint("ResourceAsColor")
public class CustomExpandableAdapter extends BaseExpandableListAdapter {

	private List<String> groupArray;
	private List<List<ChannelInfo>> childArray;
	private LayoutInflater mLayoutInflater;
	private Activity activity;
	private Boolean fromPlaying;
	
	public int mCurGroup = -1;
	public int mCurChild = -1;

	public CustomExpandableAdapter(Activity activity, List<String> groupArray,
			List<List<ChannelInfo>> childArray, Boolean fromPlaying) {
		this.activity = activity;
		this.groupArray = groupArray;
		this.childArray = childArray;
		this.fromPlaying = fromPlaying;
		mLayoutInflater = LayoutInflater.from(activity);
	}

	@Override
	public int getGroupCount() {
		if (groupArray != null) {
			return groupArray.size();
		} else {
			return 0;
		}
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return childArray.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupArray.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childArray.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			if (fromPlaying)
				convertView = mLayoutInflater.inflate(R.layout.custom_expanditem2,
						null);
			else
				convertView = mLayoutInflater.inflate(R.layout.custom_expanditem,
					null);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView
					.findViewById(R.id.expand_name);
			viewHolder.name.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			viewHolder.name.setPadding(60, 0, 0, 0);
			if (fromPlaying == false)
				viewHolder.name.getPaint().setFakeBoldText(true);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		// FIXME 2013-10-26 由于会清除childArray和groupArray的内存，故要加以保护
		if (groupPosition < groupArray.size()) {
			viewHolder.name.setText(groupArray.get(groupPosition));
		} else {
//			Log.w("arrayOut", "=========arraryOut============");
		}
		
		if (fromPlaying) {
			// 标记当前正在播放的频道
			if (mCurGroup == groupPosition) {
				// FIXME 2013-10-22 好像只能用Color.YELLOW，否则颜色不对
				viewHolder.name.setTextColor(Color.YELLOW);
			} else {
				viewHolder.name.setTextColor(Color.WHITE);
			}
		}
		
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			if (fromPlaying)
				convertView = mLayoutInflater.inflate(R.layout.custom_expanditem2,
						null);
			else
				convertView = mLayoutInflater.inflate(R.layout.custom_expanditem,
						null);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView
					.findViewById(R.id.expand_name);
			viewHolder.name.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			viewHolder.name.setPadding(60, 0, 0, 0);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		// 由于会清除childArray和groupArray的内存，故要加以保护
		if (groupPosition < groupArray.size() && childPosition < childArray.get(groupPosition).size()) {
			viewHolder.name.setText(childArray.get(groupPosition)
					.get(childPosition).getName());
		} else {
//			Log.w("arrayOut", "=========arraryOut============");
		}
		
		if (fromPlaying) {
			// 标记当前正在播放的频道
			if (mCurGroup == groupPosition && mCurChild == childPosition) {
				// FIXME 2013-10-22 好像只能用Color.YELLOW，否则颜色不对
				viewHolder.name.setTextColor(Color.YELLOW);
			} else {
				viewHolder.name.setTextColor(Color.WHITE);
			}
		}
		
//		Log.d("test", "===> child name ==>"
//				+ childArray.get(groupPosition).get(childPosition).getName());

		return convertView;
	}

	private class ViewHolder {
		TextView name;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}