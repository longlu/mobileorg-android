package com.matburt.mobileorg.ng;

import java.util.ArrayList;
import java.util.List;

import org.kvj.bravo7.ApplicationContext;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageItemInfo;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.matburt.mobileorg.ng.service.DataController;

public class App extends ApplicationContext {

	public static final String SYNCHRONIZER_PLUGIN_ACTION = "com.matburt.mobileorg.ng.SYNCHRONIZE";

	public static List<PackageItemInfo> discoverSynchronizerPlugins(
			Context context) {
		Intent discoverSynchro = new Intent(SYNCHRONIZER_PLUGIN_ACTION);
		List<ResolveInfo> packages = context.getPackageManager()
				.queryIntentActivities(discoverSynchro, 0);
		Log.d("MobileOrg", "Found " + packages.size()
				+ " total synchronizer plugins");

		ArrayList<PackageItemInfo> out = new ArrayList<PackageItemInfo>();

		for (ResolveInfo info : packages) {
			out.add(info.activityInfo);
			Log.d("MobileOrg", "Found synchronizer plugin: "
					+ info.activityInfo.packageName);
		}
		return out;
	}

	@Override
	protected void init() {
		publishBean(new DataController(this, getApplicationContext()));
	}

}
