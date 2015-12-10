package com.enode.solutions.swipemu.pulltorefresh.sectionindexer.adapter;

import java.util.Arrays;
import java.util.List;

import com.enode.solutions.swipemu.pulltorefresh.sectionindexer.CustomListView.PullToRefreshSwipeMenuListView;
import com.enode.solutions.swipemu.pulltorefresh.sectionindexer.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class PinnedHeaderSectionAdapter extends BaseAdapter implements SectionIndexer, PullToRefreshSwipeMenuListView.PinnedHeaderAdapter, OnScrollListener
{
	private int mLocationPosition = -1;
	private List<String> mDatas;
	private List<String> mFriendsSections;
	private List<Integer> mFriendsPositions;
	private LayoutInflater inflater;

	public PinnedHeaderSectionAdapter(Context context, List<String> datas, List<String> friendsSections, List<Integer> friendsPositions)
	{
		inflater = LayoutInflater.from(context);
		mDatas = datas;
		mFriendsSections = friendsSections;
		mFriendsPositions = friendsPositions;
	}

	@Override
	public int getCount()
	{
		return mDatas.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		int section = getSectionForPosition(position);
		if (convertView == null)
		{
			convertView = inflater.inflate(R.layout.listview_item, null);
		}
		LinearLayout mHeaderParent = (LinearLayout) convertView.findViewById(R.id.friends_item_header_parent);
		TextView mHeaderText = (TextView) convertView.findViewById(R.id.friends_item_header_text);
		if (getPositionForSection(section) == position)
		{
			mHeaderParent.setVisibility(View.VISIBLE);
			mHeaderText.setText(mFriendsSections.get(section));
		}
		else
		{
			mHeaderParent.setVisibility(View.GONE);
		}
		TextView textView = (TextView) convertView.findViewById(R.id.friends_item_1);
		textView.setText(mDatas.get(position));
		return convertView;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState)
	{

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{
		if (view instanceof PullToRefreshSwipeMenuListView)
		{
			((PullToRefreshSwipeMenuListView) view).configurePinnedHeaderView(firstVisibleItem);
		}
	}

	@Override
	public int getPinnedHeaderState(int position)
	{
		int realPosition = position;
		if (realPosition < 0 || (mLocationPosition != -1 && mLocationPosition == realPosition))
		{
			return PINNED_HEADER_GONE;
		}
		mLocationPosition = -1;
		int section = getSectionForPosition(realPosition);
		int nextSectionPosition = getPositionForSection(section + 1);
		if (nextSectionPosition != -1 && realPosition == nextSectionPosition - 1)
		{
			return PINNED_HEADER_PUSHED_UP;
		}
		return PINNED_HEADER_VISIBLE;
	}

	@Override
	public void configurePinnedHeader(View header, int position, int alpha)
	{
		// TODO Auto-generated method stub
		int realPosition = position;
		int section = getSectionForPosition(realPosition);
		String title = (String) getSections()[section];
		// ((TextView) header.findViewById(R.id.friends_list_header_text)).setText(title);
	}

	@Override
	public Object[] getSections()
	{
		// TODO Auto-generated method stub
		return mFriendsSections.toArray();
	}

	@Override
	public int getPositionForSection(int section)
	{
		if (section < 0 || section >= mFriendsSections.size())
		{
			return -1;
		}
		return mFriendsPositions.get(section);
	}

	@Override
	public int getSectionForPosition(int position)
	{
		if (position < 0 || position >= getCount())
		{
			return -1;
		}
		int index = Arrays.binarySearch(mFriendsPositions.toArray(), position);
		return index >= 0 ? index : -index - 2;
	}

	@Override
	public int getItemViewType(int position)
	{
		int returnViewType = 0;
		if (mDatas.size() > 0)
		{
			if (position % 2 == 0)
			{
				returnViewType = 0;
			}
			else
			{
				returnViewType = 1;
			}
		}
		return returnViewType;
	}

	@Override
	public int getViewTypeCount()
	{
		// menu type count
		return 2;
	}

}
