/**
 * The MIT License (MIT)
 * Copyright (c) 2013 David Carver
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

package us.nineworlds.serenity.ui.listeners;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import us.nineworlds.serenity.R;
import us.nineworlds.serenity.SerenityApplication;
import us.nineworlds.serenity.core.model.VideoContentInfo;
import us.nineworlds.serenity.core.model.impl.Subtitle;
import us.nineworlds.serenity.core.model.impl.YouTubeVideoContentInfo;
import us.nineworlds.serenity.core.services.MovieMetaDataRetrievalIntentService;
import us.nineworlds.serenity.core.services.YouTubeTrailerSearchIntentService;
import us.nineworlds.serenity.ui.util.ImageInfographicUtils;
import us.nineworlds.serenity.ui.util.ImageUtils;
import us.nineworlds.serenity.widgets.SerenityAdapterView;
import us.nineworlds.serenity.widgets.SerenityAdapterView.OnItemSelectedListener;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Abstract class for handling video selection information. This can either be a
 * movie or a tv episode. This is primarily used in a detail view browsing
 * scenario.
 * 
 * @author dcarver
 * 
 */
public abstract class AbstractVideoOnItemSelectedListener implements
		OnItemSelectedListener {

	public static final String CRLF = "\r\n";
	public static final int WATCHED_VIEW_ID = 1000;
	public static final float WATCHED_PERCENT = 0.98f;
	public static Activity context;
	public Handler subtitleHandler;
	public Handler trailerHandler;
	private Animation shrink;
	private Animation fadeIn;
	private View previous;
	private ImageLoader imageLoader;
	protected int position;
	protected BaseAdapter adapter;
	protected VideoContentInfo videoInfo;

	public AbstractVideoOnItemSelectedListener(Activity c) {
		context = c;
		shrink = AnimationUtils.loadAnimation(context, R.anim.shrink);
		fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
		imageLoader = SerenityApplication.getImageLoader();

	}

	protected abstract void createVideoDetail(ImageView v);

	protected void createVideoMetaData(ImageView v) {
		fetchSubtitle(videoInfo);
	}

	/**
	 * Create the images representing info such as sound, ratings, etc based on
	 * the currently selected movie poster.
	 * 
	 * @param position
	 */
	protected void createInfographicDetails(ImageView v) {
		LinearLayout infographicsView = (LinearLayout) context
				.findViewById(R.id.movieInfoGraphicLayout);
		infographicsView.removeAllViews();
		
		

		ImageInfographicUtils imageUtilsNormal = new ImageInfographicUtils(80,
				48);
		ImageInfographicUtils imageUtilsAudioChannel = new ImageInfographicUtils(90,
				48);

		TextView durationView = imageUtilsNormal.createDurationView(videoInfo.getDuration(), context);
		if (durationView != null) {
			infographicsView.addView(durationView);
		}
		
		ImageView resv = imageUtilsNormal.createVideoCodec(videoInfo.getVideoCodec(), v.getContext());
		if (resv != null) {
			infographicsView.addView(resv);
		}
		
		ImageView resolution = imageUtilsNormal.createVideoResolutionImage(videoInfo.getVideoResolution(), v.getContext());
		if (resolution != null) {
			infographicsView.addView(resolution);
		}
		
		ImageView aspectv = imageUtilsNormal.createAspectRatioImage(
				videoInfo.getAspectRatio(), context);
		if (aspectv != null) {
			infographicsView.addView(aspectv);
		}
		

		ImageView acv = imageUtilsNormal.createAudioCodecImage(
				videoInfo.getAudioCodec(), context);
		if (acv != null) {
			infographicsView.addView(acv);
		}

		ImageView achannelsv = imageUtilsAudioChannel.createAudioChannlesImage(
				videoInfo.getAudioChannels(), v.getContext());
		if (achannelsv != null) {
			infographicsView.addView(achannelsv);
		}

		if (videoInfo.getRating() != 0) {
			RatingBar ratingBar = new RatingBar(context, null,
					android.R.attr.ratingBarStyleIndicator);
			ratingBar.setMax(4);
			ratingBar.setIsIndicator(true);
			ratingBar.setStepSize(0.1f);
			ratingBar.setNumStars(4);
			ratingBar.setPadding(0, 0, 0, 0);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			params.rightMargin = 15;
			ratingBar.setLayoutParams(params);
			
			double rating = videoInfo.getRating();
			ratingBar.setRating((float) (rating / 2.5));
			infographicsView.addView(ratingBar);
		}
		
		ImageView studiov = imageUtilsNormal.createStudioImage(videoInfo.getStudio(),
				context, videoInfo.getMediaTagIdentifier());
		if (studiov != null) {
			infographicsView.addView(studiov);
		}
	}

	public void fetchSubtitle(VideoContentInfo mpi) {
		subtitleHandler = new SubtitleHandler(mpi);
		Messenger messenger = new Messenger(subtitleHandler);
		Intent intent = new Intent(context,
				MovieMetaDataRetrievalIntentService.class);
		intent.putExtra("MESSENGER", messenger);
		intent.putExtra("key", mpi.id());
		context.startService(intent);
	}
	
	public void fetchTrailer(VideoContentInfo mpi) {
		trailerHandler = new TrailerHandler(mpi);
		Messenger messenger = new Messenger(trailerHandler);
		Intent intent = new Intent(context, YouTubeTrailerSearchIntentService.class);
		intent.putExtra("videoTitle", mpi.getTitle());
		intent.putExtra("year", mpi.getYear());
		context.startService(intent);
	}

	@Override
	public void onItemSelected(SerenityAdapterView<?> av, View v, int position,
			long id) {
		videoInfo = (VideoContentInfo) av.getItemAtPosition(position);
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean shouldShrink = preferences.getBoolean(
				"animation_shrink_posters", false);

		if (previous != null) {
			previous.setPadding(0, 0, 0, 0);
			if (shouldShrink) {
				previous.setAnimation(shrink);
			}
		}

		previous = v;

		v.setPadding(5, 5, 5, 5);
		v.clearAnimation();

		ImageView posterImageView = (ImageView) v.findViewById(R.id.posterImageView);
		
		createVideoDetail(posterImageView);
		createVideoMetaData(posterImageView);
		createInfographicDetails(posterImageView);
		changeBackgroundImage(posterImageView);
	}

	/**
	 * Change the background image of the activity.
	 * 
	 * @param v
	 */
	public void changeBackgroundImage(View v) {

		if (videoInfo.getBackgroundURL() == null) {
			return;
		}

		ImageView fanArt = (ImageView) context.findViewById(R.id.fanArt);
		imageLoader.displayImage(videoInfo.getBackgroundURL(), fanArt,
				SerenityApplication.getMovieOptions(), new AnimationImageLoaderListener());

	}

	public static class TrailerHandler extends Handler {
		
		private VideoContentInfo video;
		
		public TrailerHandler(VideoContentInfo mpi) {
			video = mpi;
		}
		
		@Override
		public void handleMessage(Message msg) {
			YouTubeVideoContentInfo yt = (YouTubeVideoContentInfo) msg.obj;
			if (yt.id() == null) {
				return;
			}
			
			LinearLayout infographicsView = (LinearLayout) context
					.findViewById(R.id.movieInfoGraphicLayout);
			ImageView ytImage = new ImageView(context);
			ytImage.setImageResource(R.drawable.yt_social_icon_red_128px);
			infographicsView.addView(ytImage);
			video.setTrailer(true);
			video.setTrailerId(yt.id());
		}
	}
	
	public static class SubtitleHandler extends Handler {

		private VideoContentInfo video;

		public SubtitleHandler(VideoContentInfo video) {
			this.video = video;
		}

		@Override
		public void handleMessage(Message msg) {
			List<Subtitle> subtitles = (List<Subtitle>) msg.obj;
			if (subtitles == null || subtitles.isEmpty()) {
				return;
			}

			TextView subtitleText = (TextView) context
					.findViewById(R.id.subtitleFilter);
			subtitleText.setVisibility(View.VISIBLE);
			Spinner subtitleSpinner = (Spinner) context
					.findViewById(R.id.videoSubtitle);
			View metaData = context.findViewById(R.id.metaDataRow);
			if (metaData.getVisibility() == View.GONE || metaData.getVisibility() == View.INVISIBLE) {
				metaData.setVisibility(View.VISIBLE);
			}

			ArrayList<Subtitle> spinnerSubtitles = new ArrayList<Subtitle>();
			Subtitle noSubtitle = new Subtitle();
			noSubtitle.setDescription("None");
			noSubtitle.setFormat("none");
			noSubtitle.setKey(null);

			spinnerSubtitles.add(noSubtitle);
			spinnerSubtitles.addAll(subtitles);

			ArrayAdapter<Subtitle> subtitleAdapter = new ArrayAdapter<Subtitle>(
					context, R.layout.serenity_spinner_textview,
					spinnerSubtitles);
			subtitleAdapter
					.setDropDownViewResource(R.layout.serenity_spinner_textview_dropdown);
			subtitleSpinner.setAdapter(subtitleAdapter);
			subtitleSpinner
					.setOnItemSelectedListener(new SubtitleSpinnerOnItemSelectedListener(
							video, context));
			subtitleSpinner.setVisibility(View.VISIBLE);
		}

	}
	
	private class AnimationImageLoaderListener extends SimpleImageLoadingListener {
		
		/* (non-Javadoc)
		 * @see com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener#onLoadingComplete(java.lang.String, android.view.View, android.graphics.Bitmap)
		 */
		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(context);
			boolean shouldFadeIn = preferences.getBoolean(
					"animation_background_fadein", false);
			if (shouldFadeIn) {
				view.startAnimation(fadeIn);
			}
		}
	}

}
