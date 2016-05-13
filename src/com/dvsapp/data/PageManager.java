package com.dvsapp.data;

import java.util.ArrayList;
import java.util.List;

//分页加载
public class PageManager<T> {
	private int mCurPage = 0;
	private int mPageCount = 20;
	private int mCount = -1;
	private boolean mBNetError = false;
	private List<T> mDatas = new ArrayList<T>();

	public void reset() {
		mCurPage = 0;
		mPageCount = 20;
		mCount = -1;
		mBNetError = false;
		mDatas.clear();
	}

	public void setCurPage(int curPage) {
		mCurPage = curPage;
	}

	public int getCurPage() {
		return mCurPage;
	}

	public void setPageCount(int count) {
		mPageCount = count;
	}

	public int getPageCount() {
		return mPageCount;
	}

	public void setCount(int count) {
		mCount = count;
	}

	public int getCount() {
		return mCount;
	}

	public synchronized List<T> getDatas() {
		return mDatas;
	}

	public void setNetError(boolean error) {
		mBNetError = error;
	}

	public boolean hasMore() {
		if (mBNetError)
			return false;

		if (mCount == -1)
			return true;
		else if (mCount == 0)
			return false;
		int page = mCount / mPageCount;
		return page != mCurPage - 1;
	}
}
