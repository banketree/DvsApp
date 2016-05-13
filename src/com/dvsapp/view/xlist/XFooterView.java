package com.dvsapp.view.xlist;

import com.dvsapp.wisdom.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * The footer view for {@link com.markmao.pulltorefresh.widget.XListView} and
 * {@link com.markmao.pulltorefresh.widget.XScrollView}
 */
public class XFooterView extends LinearLayout {
	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_LOADING = 2;

	private final int ROTATE_ANIM_DURATION = 180;

	private View mFootView;

	private ProgressBar mProgressBar;

	private TextView mLoadTextView;
	private ImageView mUpImageView;

	private Animation mRotateUpAnim;
	private Animation mRotateDownAnim;

	private int mState = STATE_NORMAL;

	public XFooterView(Context context) {
		super(context);
		initView(context);
	}

	public XFooterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {
		mFootView = LayoutInflater.from(context).inflate(
				R.layout.view_xlist_footer, null);
		mFootView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		addView(mFootView);

		mProgressBar = (ProgressBar) mFootView
				.findViewById(R.id.ProgressBar_loading);
		mLoadTextView = (TextView) mFootView.findViewById(R.id.TextView_load);
		mUpImageView = (ImageView) mFootView
				.findViewById(R.id.ImageView_up_arrow);

		mRotateUpAnim = new RotateAnimation(0.0f, 180.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateUpAnim.setFillAfter(true);

		mRotateDownAnim = new RotateAnimation(180.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateDownAnim.setFillAfter(true);
	}

	/**
	 * Set footer view state
	 * 
	 * @see #STATE_LOADING
	 * @see #STATE_NORMAL
	 * @see #STATE_READY
	 * 
	 * @param state
	 */
	public void setState(int state) {
		if (state == mState)
			return;

		if (state == STATE_LOADING) {
			// mHintImage.clearAnimation();
			// mHintImage.setVisibility(View.INVISIBLE);
			mProgressBar.setVisibility(View.VISIBLE);
			// mHintView.setVisibility(View.INVISIBLE);
		} else {
			// mHintView.setVisibility(View.VISIBLE);
			// mHintImage.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.INVISIBLE);
		}

		switch (state) {
		case STATE_NORMAL:
			if (mState == STATE_READY) {
				mUpImageView.startAnimation(mRotateDownAnim);
			}
			if (mState == STATE_LOADING) {
				mUpImageView.clearAnimation();
			}
			mLoadTextView.setText(R.string.footer_load_click);
			break;

		case STATE_READY:
			if (mState != STATE_READY) {
				mUpImageView.clearAnimation();
				mUpImageView.startAnimation(mRotateUpAnim);
				mLoadTextView.setText(R.string.footer_load_release);
			}
			break;

		case STATE_LOADING:
			loading();
			break;
		}

		mState = state;
	}

	/**
	 * Set footer view bottom margin.
	 * 
	 * @param margin
	 */
	public void setBottomMargin(int margin) {
		if (margin < 0)
			return;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mFootView
				.getLayoutParams();
		lp.bottomMargin = margin;
		mFootView.setLayoutParams(lp);
	}

	/**
	 * Get footer view bottom margin.
	 * 
	 * @return
	 */
	public int getBottomMargin() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mFootView
				.getLayoutParams();
		return lp.bottomMargin;
	}

	/**
	 * normal status
	 */
	public void normal() {
		mLoadTextView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
	}

	/**
	 * loading status
	 */
	public void loading() {
		mLoadTextView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
	}

	/**
	 * hide footer when disable pull load more
	 */
	public void hide() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mFootView
				.getLayoutParams();
		lp.height = 0;
		mFootView.setLayoutParams(lp);
	}

	/**
	 * show footer
	 */
	public void show() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mFootView
				.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		mFootView.setLayoutParams(lp);
	}

	public ProgressBar getProgressBar() {
		return mProgressBar;
	}

	public TextView getLoadTextView() {
		return mLoadTextView;
	}

	public ImageView getUpImageView() {
		return mUpImageView;
	}

}
