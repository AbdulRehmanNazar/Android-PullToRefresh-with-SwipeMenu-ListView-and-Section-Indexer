package com.enode.solutions.swipemu.pulltorefresh.sectionindexer;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.enode.solutions.swipemu.pulltorefresh.sectionindexer.CustomListView.PullToRefreshSwipeMenuListView;
import com.enode.solutions.swipemu.pulltorefresh.sectionindexer.SectionIndexer.SideBladeView;
import com.enode.solutions.swipemu.pulltorefresh.sectionindexer.adapter.PinnedHeaderSectionAdapter;
import com.enode.solutions.swipemu.pulltorefresh.sectionindexer.models.Countries;
import com.enode.solutions.swipemu.pulltorefresh.sectionindexer.parsers.CountriesParser;
import com.enode.solutions.swipemu.pulltorefresh.sectionindexer.pulltorefresh.RefreshTime;
import com.enode.solutions.swipemu.pulltorefresh.sectionindexer.swipemenulistview.SwipeMenu;
import com.enode.solutions.swipemu.pulltorefresh.sectionindexer.swipemenulistview.SwipeMenuCreator;
import com.enode.solutions.swipemu.pulltorefresh.sectionindexer.swipemenulistview.SwipeMenuItem;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.Toast;

public class MainActivity extends Activity implements PullToRefreshSwipeMenuListView.RefreshLoadMoreListViewListener
{
	private static final String ENG_FORMAT = "[A-Za-z0-9_\\-]+";
	private PullToRefreshSwipeMenuListView mListView;
	private SideBladeView mLetter;
	private PinnedHeaderSectionAdapter mAdapter;

	private List<String> mSections;
	private Map<String, List<Countries>> mMap;
	private List<Integer> mPositions;
	private Map<String, Integer> mIndexer;
	private List<String> list11;
	private ArrayList<Countries> countriesList;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_main);
		initData();
		initView();
	}

	public String loadJSONFromAsset()
	{
		String json = null;
		try
		{
			InputStream is = getAssets().open("country.json");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			json = new String(buffer, "UTF-8");
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
		return json;
	}

	private void initData()
	{
		String lang = Locale.getDefault().getCountry().toLowerCase();
		// lang ="cn";
		countriesList = new ArrayList<Countries>();
		String json = loadJSONFromAsset();
		countriesList = new CountriesParser().parseData(json);

		// extract initials
		String initial;

		for (int i = 0; i < countriesList.size(); i++)
		{
			initial = countriesList.get(i).getCountryEngName().substring(0, 1);
			countriesList.get(i).setInitial(initial);
		}

		Collections.sort(countriesList, new Comparator<Countries>()
		{
			@Override
			public int compare(Countries lhs, Countries rhs)
			{
				return lhs.getCountryEngName().compareTo(rhs.getCountryEngName());
			}
		});

		list11 = new ArrayList<String>();
		for (int i = 0; i < countriesList.size(); i++)
		{
			String nameToShow;

			nameToShow = countriesList.get(i).getCountryEngName() + " ( +" + countriesList.get(i).getCountryPrefixCode() + ")";

			list11.add(nameToShow);

		}

		mSections = new ArrayList<String>(); // section list
		mMap = new HashMap<String, List<Countries>>(); // section name ->
		mPositions = new ArrayList<Integer>();
		mIndexer = new HashMap<String, Integer>();

		for (int i = 0; i < countriesList.size(); i++)
		{

			String firstName = countriesList.get(i).getInitial();
			if (firstName.matches(ENG_FORMAT))
			{
				if (mSections.contains(firstName))
				{
					mMap.get(firstName).add(countriesList.get(i));
				}
				else
				{
					mSections.add(firstName);
					List<Countries> list = new ArrayList<Countries>();
					list.add(countriesList.get(i));
					mMap.put(firstName, list);
				}
			}
			else
			{

				if (mSections.contains("#"))
				{
					mMap.get("#").add(countriesList.get(i));
				}
				else
				{
					mSections.add("#");
					List<Countries> list = new ArrayList<Countries>();
					list.add(countriesList.get(i));
					mMap.put("#", list);
				}
			}
		}
		// Sort the sections
		Collections.sort(mSections);

		int position = 0;
		for (int i = 0; i < mSections.size(); i++)
		{
			mIndexer.put(mSections.get(i), position);
			mPositions.add(position);
			position += mMap.get(mSections.get(i)).size();
		}
	}

	private void initView()
	{
		mListView = (PullToRefreshSwipeMenuListView) findViewById(R.id.friends_display);
		mLetter = (SideBladeView) findViewById(R.id.friends_myletterlistview);
		mLetter.setOnItemClickListener(new SideBladeView.OnItemClickListener()
		{

			@Override
			public void onItemClick(String s)
			{
				if (mIndexer.get(s) != null)
				{
					mListView.setSelection(mIndexer.get(s));
				}
			}
		});
		mAdapter = new PinnedHeaderSectionAdapter(this, list11, mSections, mPositions);
		mListView.setAdapter(mAdapter);
		// Register scroll listener with the adapter to move the target position
		mListView.setOnScrollListener(mAdapter);
		// Enable Pullto refresh enable
		mListView.setPullRefreshEnable(true);
		// Enable Load more or Footer refresh enableload
		mListView.setPullLoadEnable(true);
		// Enable Swipe enable
		mListView.setSwipeEnable(true);

		mListView.setRefeshLoadMoreListViewListener(this);
		mHandler = new Handler();

		// step 1. create a MenuCreator
		SwipeMenuCreator creator = new SwipeMenuCreator()
		{

			@Override
			public void create(SwipeMenu menu)
			{
				if (menu.getViewType() == 0)
				{
					addTwoMenuItem(menu);
				}
				else
				{
					addThreeMenuItem(menu);
				}
			}
		};
		// set creator
		mListView.setMenuCreator(creator);

		// step 2. listener item click event
		mListView.setOnMenuItemClickListener(new PullToRefreshSwipeMenuListView.OnMenuItemClickListener()
		{
			@Override
			public void onMenuItemClick(int position, SwipeMenu menu, int index)
			{
				switch (index)
				{
				case 0:
					// Item 1
					Toast.makeText(MainActivity.this, "Item 1 Clicked", Toast.LENGTH_LONG).show();
					break;
				case 1:
					// Item 2
					Toast.makeText(MainActivity.this, "Item 2 Clicked", Toast.LENGTH_LONG).show();
					break;
				case 2:
					// Item 3
					Toast.makeText(MainActivity.this, "Item 3 Clicked", Toast.LENGTH_LONG).show();
					break;
				}
			}
		});
		// set SwipeListener
		mListView.setOnSwipeListener(new PullToRefreshSwipeMenuListView.OnSwipeListener()
		{

			@Override
			public void onSwipeStart(int position)
			{
				// swipe start
			}

			@Override
			public void onSwipeEnd(int position)
			{
				// swipe end
			}
		});

		// Animation on close
		mListView.setCloseInterpolator(new BounceInterpolator());

		// test item long click
		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				Toast.makeText(getApplicationContext(), position + " long click", Toast.LENGTH_SHORT).show();
				return false;
			}
		});

		mListView.setPinnedHeaderView(LayoutInflater.from(this).inflate(R.layout.listview_item, mListView, false));
	}

	@Override
	public void onRefresh()
	{
		mHandler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
				RefreshTime.setRefreshTime(getApplicationContext(), df.format(new Date()));
				Toast.makeText(MainActivity.this, "PullToRefresh done", Toast.LENGTH_LONG).show();
				onLoad();
			}
		}, 3000);
	}

	@Override
	public void onLoadMore()
	{
		mHandler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
				RefreshTime.setRefreshTime(getApplicationContext(), df.format(new Date()));
				Toast.makeText(MainActivity.this, "Load More done", Toast.LENGTH_LONG).show();
				onLoad();
			}
		}, 3000);
	}

	private void onLoad()
	{
		mListView.setRefreshTime(RefreshTime.getRefreshTime(getApplicationContext()));
		mListView.stopRefresh();

		mListView.stopLoadMore();

	}

	public void addTwoMenuItem(SwipeMenu menu)
	{
		// create "First" item
		SwipeMenuItem item1 = new SwipeMenuItem(getApplicationContext());
		// set item background
		item1.setBackground(new ColorDrawable(Color.rgb(41, 46, 82)));
		// set item width
		item1.setWidth(150);
		// set item title
		item1.setTitle("Item 1");
		// set item title fontsize
		item1.setTitleSize(18);
		// set item title font color
		item1.setTitleColor(Color.WHITE);
		// add to menu
		menu.addMenuItem(item1);

		// create "Second" item
		SwipeMenuItem item2 = new SwipeMenuItem(getApplicationContext());
		// set item background
		item2.setBackground(new ColorDrawable(Color.rgb(56, 162, 217)));
		// set item width
		item2.setWidth(150);
		// set a icon
		item2.setTitle("Item 2");
		// Set size
		item2.setTitleSize(18);
		// set item title font color
		item2.setTitleColor(Color.WHITE);

		// add to menu
		menu.addMenuItem(item2);
	}

	public void addThreeMenuItem(SwipeMenu menu)
	{
		// create "First" item
		SwipeMenuItem item1 = new SwipeMenuItem(getApplicationContext());
		// set item background
		item1.setBackground(new ColorDrawable(Color.rgb(41, 46, 82)));
		// set item width
		item1.setWidth(150);
		// set item title
		item1.setTitle("Item 1");
		// set item title fontsize
		item1.setTitleSize(18);
		// set item title font color
		item1.setTitleColor(Color.WHITE);
		// add to menu
		menu.addMenuItem(item1);

		// create "Second" item
		SwipeMenuItem item2 = new SwipeMenuItem(getApplicationContext());
		// set item background
		item2.setBackground(new ColorDrawable(Color.rgb(56, 162, 217)));
		// set item width
		item2.setWidth(150);
		// set a icon
		item2.setTitle("Item 2");
		// Set size
		item2.setTitleSize(18);
		// set item title font color
		item2.setTitleColor(Color.WHITE);

		// add to menu
		menu.addMenuItem(item2);

		// create third item
		SwipeMenuItem item3 = new SwipeMenuItem(getApplicationContext());
		// set item background
		item3.setBackground(new ColorDrawable(Color.rgb(125, 178, 107)));
		// set item width
		item3.setWidth(150);
		// set a icon
		item3.setTitle("Item 3");
		// Set size
		item3.setTitleSize(18);
		// set item title font color
		item3.setTitleColor(Color.WHITE);

		// add to menu
		menu.addMenuItem(item3);
	}

}
