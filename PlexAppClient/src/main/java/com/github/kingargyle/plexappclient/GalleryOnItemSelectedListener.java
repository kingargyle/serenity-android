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

package com.github.kingargyle.plexappclient;

import android.graphics.Typeface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class GalleryOnItemSelectedListener implements
		OnItemSelectedListener {
	
	private View mainView;
	private MainMenuTextView preSelected;
	
	/**
	 * 
	 */
	public GalleryOnItemSelectedListener(View v) {
		mainView = v;
	}

	/* (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
	 */
	public void onItemSelected(AdapterView<?> arg0, View v, int position,
			long arg3) {
		switch (position) {
		case 0: {
			mainView.setBackgroundResource(R.drawable.movies);
			break;
		}
		case 1: {
			mainView.setBackgroundResource(R.drawable.tvshows);
			break;
		}
		
		case 2: {
			mainView.setBackgroundResource(R.drawable.music);
			break;
		}
		
		default: {
			mainView.setBackgroundResource(R.drawable.settings);
		}
		}
		mainView.refreshDrawableState();
		
		if (v instanceof MainMenuTextView) {
			MainMenuTextView tv = (MainMenuTextView) v;
			tv.setTextSize(tv.getTextSize() + 20);
			tv.setTypeface(null, Typeface.BOLD);
			if (preSelected != null) {
				preSelected.setTextSize(30);
				preSelected.refreshDrawableState();
				preSelected.setTypeface(null, Typeface.NORMAL);
			}
			preSelected = tv;
		}
		
	}

	/* (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
	 */
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}


}
