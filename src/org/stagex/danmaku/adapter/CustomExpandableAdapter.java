package org.stagex.danmaku.adapter;

import java.util.List;
import org.keke.player.R;
import android.annotation.SuppressLint;
import android.app.Activity;
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
	Activity activity;
	private Boolean fromPlaying;

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
		viewHolder.name.setText(groupArray.get(groupPosition));
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
		viewHolder.name.setText(childArray.get(groupPosition)
				.get(childPosition).getName());

//		Log.d("test", "===> child name ==>"
//				+ childArray.get(groupPosition).get(childPosition).getName());

		return convertView;
	}

	class ViewHolder {
		TextView name;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}