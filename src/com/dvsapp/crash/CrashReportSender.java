package com.dvsapp.crash;

import com.treecore.crash.TCrash;
import com.treecore.crash.TIReportSender;
import com.treecore.crash.data.CrashReportData;
import com.treecore.crash.data.ReportField;
import com.treecore.crash.exception.ReportSenderException;

import android.content.Context;

public class CrashReportSender implements TIReportSender {
	private final Context mContext;
	public final static String formUri = "http://10.32.202.155:9876";

	public CrashReportSender(Context mContext) {
		super();
		this.mContext = mContext;
		TCrash.getInstance().getErrorReporter()
				.putCustomData("PLATFORM", "ANDROID");
		TCrash.getInstance().getErrorReporter()
				.putCustomData("BUILD_ID", android.os.Build.ID);
		TCrash.getInstance().getErrorReporter()
				.putCustomData("DEVICE_NAME", android.os.Build.PRODUCT);

	}

	@Override
	public void send(CrashReportData arg0) throws ReportSenderException {
		// // 邮件发�??
		EmailIntentSender emailSender = new EmailIntentSender(mContext);
		emailSender.send(arg0);
		// 自定义发�?
		final String subject = mContext.getPackageName() + " Crash Report";
		final String body = subject + "\n" + buildBody(arg0);

		// try {
		// EotuHttpRequestAPI.reportBug(EotuAccount.getInstance().getPhone(),
		// body);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// HttpPostSender httpSender = new HttpPostSender(formUri, null);
		// httpSender.send(arg0);
		// // 发往谷歌
		// GoogleFormSender googleSender = new GoogleFormSender();
		// googleSender.send(arg0);
	}

	private String buildBody(CrashReportData errorContent) {
		final StringBuilder builder = new StringBuilder();
		for (ReportField field : TCrash.getInstance().getReportFields()) {
			builder.append(field.toString()).append("=");
			builder.append(errorContent.get(field));
			builder.append('\n');
		}
		return builder.toString();
	}
}
