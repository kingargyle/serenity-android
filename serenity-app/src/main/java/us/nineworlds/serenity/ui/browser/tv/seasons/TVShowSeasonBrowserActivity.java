/**
 * The MIT License (MIT)
 * Copyright (c) 2012 David Carver
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package us.nineworlds.serenity.ui.browser.tv.seasons;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.LinearLayoutManager;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import net.ganin.darv.DpadAwareRecyclerView;
import us.nineworlds.serenity.R;
import us.nineworlds.serenity.core.menus.MenuDrawerItem;
import us.nineworlds.serenity.core.menus.MenuDrawerItemImpl;
import us.nineworlds.serenity.recyclerutils.SpaceItemDecoration;
import us.nineworlds.serenity.ui.activity.SerenityVideoActivity;
import us.nineworlds.serenity.ui.adapters.AbstractPosterImageGalleryAdapter;
import us.nineworlds.serenity.ui.adapters.MenuDrawerAdapter;
import us.nineworlds.serenity.ui.util.DisplayUtils;
import us.nineworlds.serenity.widgets.SerenityGallery;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.BaseAdapter;

import com.jess.ui.TwoWayGridView;

/**
 * @author dcarver
 *
 */
public class TVShowSeasonBrowserActivity extends SerenityVideoActivity {

	@BindView(R.id.tvShowSeasonImageGallery)
	DpadAwareRecyclerView tvShowSeasonsGallery;

	@BindView(R.id.tvShowSeasonImageGalleryLayout)
	View tvShowSeasonsMainView;

	private boolean restarted_state = false;
	private String key;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar.setCustomView(R.layout.season_custom_actionbar);
		actionBar.setDisplayShowCustomEnabled(true);

		key = getIntent().getExtras().getString("key");

		createSideMenu();

		ButterKnife.bind(this);

		DisplayUtils.overscanCompensation(this, getWindow().getDecorView());
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (restarted_state == false) {
			setupSeasons();
		}
		restarted_state = false;
	}

	protected void setupSeasons() {

		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		tvShowSeasonsGallery.setLayoutManager(linearLayoutManager);
		tvShowSeasonsGallery.setAdapter(new TVShowSeasonImageGalleryAdapter(this, key));
		tvShowSeasonsGallery.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.horizontal_spacing)));
		tvShowSeasonsGallery.setOnItemSelectedListener(new TVShowSeasonOnItemSelectedListener(this));
		tvShowSeasonsGallery.setOnItemClickListener(new TVShowSeasonOnItemClickListener(this));
//		tvShowSeasonsGallery
//				.setOnItemLongClickListener(new SeasonOnItemLongClickListener(
//						this));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		populateMenuDrawer();
		restarted_state = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see us.nineworlds.serenity.ui.activity.SerenityActivity#createSideMenu()
	 */
	@Override
	protected void createSideMenu() {
		setContentView(R.layout.activity_tvbrowser_show_seasons);

		View fanArt = findViewById(R.id.fanArt);
		fanArt.setBackgroundResource(R.drawable.tvshows);

		initMenuDrawerViews();

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.menudrawer_selector, R.string.drawer_open,
				R.string.drawer_closed) {
			@Override
			public void onDrawerOpened(View drawerView) {

				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle(R.string.app_name);
				drawerList.requestFocusFromTouch();
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				getSupportActionBar().setTitle(R.string.app_name);
			}
		};

		drawerLayout.setDrawerListener(drawerToggle);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		populateMenuDrawer();
	}

	protected void populateMenuDrawer() {
		List<MenuDrawerItem> drawerMenuItem = new ArrayList<MenuDrawerItem>();
		drawerMenuItem.add(new MenuDrawerItemImpl("Play All from Queue",
				R.drawable.menu_play_all_queue));

		drawerList.setAdapter(new MenuDrawerAdapter(this, drawerMenuItem));
		drawerList
				.setOnItemClickListener(new TVShowSeasonMenuDrawerOnItemClickedListener(
						drawerLayout));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see us.nineworlds.serenity.ui.activity.SerenityActivity#onKeyDown(int,
	 * android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean menuKeySlidingMenu = PreferenceManager
				.getDefaultSharedPreferences(this).getBoolean(
						"remote_control_menu", true);
		if (menuKeySlidingMenu) {
			if (keyCode == KeyEvent.KEYCODE_MENU) {
				if (drawerLayout.isDrawerOpen(linearDrawerLayout)) {
					drawerLayout.closeDrawers();
				} else {
					drawerLayout.openDrawer(linearDrawerLayout);
				}
				return true;
			}
		}

		if (keyCode == KeyEvent.KEYCODE_BACK
				&& drawerLayout.isDrawerOpen(linearDrawerLayout)) {
			drawerLayout.closeDrawers();
			if (tvShowSeasonsGallery != null) {
				tvShowSeasonsGallery.requestFocusFromTouch();
			}
			return true;
		}

		View focusView = getCurrentFocus();

		DpadAwareRecyclerView gallery = (DpadAwareRecyclerView) findViewById(R.id.tvShowSeasonImageGallery);
		DpadAwareRecyclerView gridView = (DpadAwareRecyclerView) findViewById(R.id.episodeGridView);
		if (gridView == null) {
			gridView = (DpadAwareRecyclerView) findViewById(R.id.tvShowGridView);
		}

		if (gallery == null && gridView == null) {
			return super.onKeyDown(keyCode, event);
		}

//		AbstractPosterImageGalleryAdapter adapter = null;
//		if (focusView instanceof TwoWayGridView) {
//			adapter = (BaseAdapter) gridView.getAdapter();
//		} else {
//			adapter = (BaseAdapter) gallery.getAdapter();
//		}
//
//		if (adapter != null) {
//			int itemsCount = adapter.getCount();
//
//			if (contextMenuRequested(keyCode)) {
//				View view = null;
//				if (focusView instanceof TwoWayGridView) {
//					view = gridView.getSelectedView();
//				} else if (gallery != null) {
//					view = gallery.getSelectedView();
//				}
//				if (view == null) {
//					return super.onKeyDown(keyCode, event);
//				}
//				view.performLongClick();
//				return true;
//			}
//
//			if (gallery != null) {
//				if (isKeyCodeSkipBack(keyCode)) {
//					int selectedItem = gallery.getSelectedItemPosition();
//					int newPosition = selectedItem - 10;
//					if (newPosition < 0) {
//						newPosition = 0;
//					}
//					gallery.setSelection(newPosition);
//					gallery.requestFocusFromTouch();
//					return true;
//				}
//				if (isKeyCodeSkipForward(keyCode)) {
//					int selectedItem = gallery.getSelectedItemPosition();
//					int newPosition = selectedItem + 10;
//					if (newPosition > itemsCount) {
//						newPosition = itemsCount - 1;
//					}
//					gallery.setSelection(newPosition);
//					gallery.requestFocusFromTouch();
//					return true;
//				}
//			}
//		}

		return super.onKeyDown(keyCode, event);

	}

	@Override
	public AbstractPosterImageGalleryAdapter getAdapter() {
		return null;
	}

	/**
	 * Nothing really to update here now, so will return null.
	 *
	 */
	@Override
	protected DpadAwareRecyclerView findGalleryView() {
		return null;
	}

	/**
	 * We want to update playback position and onscreen info when completing.
	 *
	 * So pass back the appropriate grid view in this case.
	 */
	@Override
	protected DpadAwareRecyclerView findGridView() {
		return (DpadAwareRecyclerView) findViewById(R.id.episodeGridView);
	}
}
