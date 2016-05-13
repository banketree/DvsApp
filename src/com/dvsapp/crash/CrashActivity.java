/*
 *  Copyright 2010 Emmanuel Astier & Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.dvsapp.crash;

import java.io.IOException;

import com.treecore.crash.*;
import com.treecore.crash.data.CrashReportData;
import com.treecore.crash.data.ReportField;
import com.treecore.crash.data.TCrashReportPersister;
import com.treecore.activity.TActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CrashActivity extends TActivity {
	protected static final String TAG = CrashActivity.class.getSimpleName();
	private SharedPreferences prefs;
	private EditText userComment;
	private EditText userEmail;
	private String mReportFileName = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mReportFileName = getIntent().getStringExtra(
				TCrash.EXTRA_REPORT_FILE_NAME);
		if (mReportFileName == null) {
			finish();
		}

		requestWindowFeature(Window.FEATURE_LEFT_ICON);

		final LinearLayout root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setPadding(10, 10, 10, 10);
		root.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));

		final ScrollView scroll = new ScrollView(this);
		root.addView(scroll, new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));

		final TextView text = new TextView(this);

		text.setText("测试奔溃模块");
		scroll.addView(text, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		// Add an optional prompt for user comments
		final TextView label = new TextView(this);
		label.setText("评论:");

		label.setPadding(label.getPaddingLeft(), 10, label.getPaddingRight(),
				label.getPaddingBottom());
		root.addView(label, new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		userComment = new EditText(this);
		userComment.setLines(2);
		root.addView(userComment, new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		final LinearLayout buttons = new LinearLayout(this);
		buttons.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		buttons.setPadding(buttons.getPaddingLeft(), 10,
				buttons.getPaddingRight(), buttons.getPaddingBottom());

		final Button yes = new Button(this);
		yes.setText(android.R.string.yes);
		yes.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Retrieve user comment
				final String comment = userComment != null ? userComment
						.getText().toString() : "";

				TCrashReportPersister persister = new TCrashReportPersister();
				try {
					Log.d(TAG, "Add user comment to " + mReportFileName);
					final CrashReportData crashData = persister
							.load(mReportFileName);
					crashData.put(ReportField.USER_COMMENT, comment);
					crashData.put(ReportField.USER_EMAIL, "banketree@qq.com");
					persister.store(crashData, mReportFileName);
				} catch (IOException e) {
					Log.w(TAG, "User comment not added: ", e);
				}

				// Start the report sending task
				Log.v(TAG, "About to start SenderWorker from CrashReportDialog");
				TCrash.getInstance().getErrorReporter()
						.startSendingReports(true);
				makeText("发�??");
				finish();
			}

		});
		buttons.addView(yes, new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
		final Button no = new Button(this);
		no.setText(android.R.string.no);
		no.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Let's delete all non approved reports. We keep approved and
				// silent reports.
				TCrash.getInstance().getErrorReporter()
						.deletePendingNonApprovedReports(false);
				finish();
			}

		});
		buttons.addView(no, new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
		root.addView(buttons, new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		setContentView(root);

		// final int resTitle = TAcra.getConfig().resDialogTitle();
		// if (resTitle != 0) {
		// setTitle(resTitle);
		// }
		//
		// getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
		// TAcra.getConfig().resDialogIcon());
	}
}
