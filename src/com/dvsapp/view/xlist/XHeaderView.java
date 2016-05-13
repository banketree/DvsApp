package com.dvsapp.view.xlist;


import com.dvsapp.wisdom.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * The header view for {@link com.markmao.pulltorefresh.widget.XListView} and
 * {@link com.markmao.pulltorefresh.widget.XScrollView}
 * 
 * @author markmjw
 * @date 2013-10-08
 */
public class XHeaderView extends LinearLayout {
	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_REFRESHING = 2;

	private final int ROTATE_ANIM_DURATION = 180;

	private View mHeader, mHeaderContent, mTimeView;
	private ImageView mArrowImageView;
	private ProgressBar mProgressBar;
	private TextView mTitleTextView, mTimeTextView;

	private int mState = STATE_NORMAL;

	private Animation mRotateUpAnim;
	private Animation mRotateDownAnim;

	private boolean mIsFirst;

	public XHeaderView(Context context) {
		super(context);
		initView(context);
	}

	public XHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {
		// Initial set header view height 0
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, 0);
		mHeader = (LinearLayout) LayoutInflater.from(context).inflate(
				R.layout.view_xlist_header, null);

		addView(mHeader, lp);
		setGravity(Gravity.BOTTOM);

		mTimeView = findViewById(R.id.LinearLayout_time);
		mHeaderContent = findViewById(R.id.LinearLayout_head);
		mArrowImageView = (ImageView) findViewById(R.id.ImageView_down_arrow);
		mTitleTextView = (TextView) findViewById(R.id.TextView_refresh_title);
		mTimeTextView = (TextView) findViewById(R.id.TextView_refresh_time);
		mProgressBar = (ProgressBar) findViewById(R.id.ProgressBar_loading);

		mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateUpAnim.setFillAfter(true);

		mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateDownAnim.setFillAfter(true);
	}

	public void setState(int state) {
		if (state == mState && mIsFirst) {
			mIsFirst = true;
			return;
		}

		if (state == STATE_REFRESHING) {
			// show progress
			mArrowImageView.clearAnimation();
			mArrowImageView.setVisibility(View.INVISIBLE);
			mProgressBar.setVisibility(View.VISIBLE);
		} else {
			// show arrow image
			mArrowImageView.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.INVISIBLE);
		}

		switch (state) {
		case STATE_NORMAL:
			if (mState == STATE_READY) {
				mArrowImageView.startAnimation(mRotateDownAnim);
			}

			if (mState == STATE_REFRESHING) {
				mArrowImageView.clearAnimation();
			}

			mTitleTextView.setText(R.string.header_refresh_normal);
			break;

		case STATE_READY:
			if (mState != STATE_READY) {
				mArrowImageView.clearAnimation();
				mArrowImageView.startAnimation(mRotateUpAnim);

				mTitleTextView.setText(R.string.header_refresh_release);
			}
			break;

		case STATE_REFRESHING:

			mTitleTextView.setText(R.string.header_refresh_loading);
			break;

		default:
			break;
		}

		mState = state;
	}

	/**
	 * Set the header view visible height.
	 * 
	 * @param height
	 */
	public void setVisibleHeight(int height) {
		if (height < 0)
			height = 0;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mHeader
				.getLayoutParams();
		lp.height = height;
		mHeader.setLayoutParams(lp);
	}

	/**
	 * Get the header view visible height.
	 * 
	 * @return
	 */
	public int getVisibleHeight() {
		return mHeader.getHeight();
	}

	public View getTimeView() {
		return mTimeView;
	}

	public ProgressBar getProgressBar() {
		return mProgressBar;
	}

	public ImageView getArrowImageView() {
		return mArrowImageView;
	}

	public TextView getTitleTextView() {
		return mTitleTextView;
	}

	public TextView getTimeTextView() {
		return mTimeTextView;
	}
}
