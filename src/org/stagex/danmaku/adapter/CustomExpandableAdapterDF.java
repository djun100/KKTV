package org.stagex.danmaku.adapter;

import java.util.List;

import org.keke.player.R;

import com.nmbb.oplayer.scanner.POChannelList;

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
public class CustomExpandableAdapterDF extends BaseExpandableListAdapter {

	private List<ProvinceInfo> groupArray;
	private List<List<POChannelList>> childArray;
	private LayoutInflater mLayoutInflater;
	private Activity activity;
	
	public int mCurGroup = -1;
	public int mCurChild = -1;

	public CustomExpandableAdapterDF(Activity activity, List<ProvinceInfo> groupArray,
			List<List<POChannelList>> childArray) {
		this.activity = activity;
		this.groupArray = groupArray;
		this.childArray = childArray;
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
		return groupArray.get(groupPosition);		//FIXME
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childArray.get(groupPosition).get(childPosition);		//FIXME
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
		ViewHolderDF viewHolder;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.custom_expanditem2,
				null);
			viewHolder = new ViewHolderDF();
			viewHolder.name = (TextView) convertView
					.findViewById(R.id.expand_name);
			viewHolder.name.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			viewHolder.name.setPadding(60, 0, 0, 0);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolderDF) convertView.getTag();
		}
		viewHolder.name.setText(groupArray.get(groupPosition).getProvinceName());
		
		// 标记当前正在播放的频道
		if (mCurGroup == groupPosition) {
			// FIXME 2013-10-22 好像只能用Color.YELLOW，否则颜色不对
			viewHolder.name.setTextColor(Color.YELLOW);
		} else {
			viewHolder.name.setTextColor(Color.WHITE);
		}
		
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolderDF viewHolder;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.custom_expanditem2,
					null);
			viewHolder = new ViewHolderDF();
			viewHolder.name = (TextView) convertView
					.findViewById(R.id.expand_name);
			viewHolder.name.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			viewHolder.name.setPadding(60, 0, 0, 0);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolderDF) convertView.getTag();
		}
		viewHolder.name.setText(childArray.get(groupPosition)
				.get(childPosition).name);

		// 标记当前正在播放的频道
		if (mCurGroup == groupPosition && mCurChild == childPosition) {
			// FIXME 2013-10-22 好像只能用Color.YELLOW，否则颜色不对
			viewHolder.name.setTextColor(Color.YELLOW);
		} else {
			viewHolder.name.setTextColor(Color.WHITE);
		}
		
//		Log.d("test", "===> child name ==>"
//				+ childArray.get(groupPosition).get(childPosition).getName());

		return convertView;
	}

	private class ViewHolderDF {
		TextView name;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}