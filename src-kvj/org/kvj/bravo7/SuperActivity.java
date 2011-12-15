package org.kvj.bravo7;

import org.kvj.bravo7.ControllerConnector;
import org.kvj.bravo7.ControllerConnector.ControllerReceiver;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class SuperActivity<A extends ApplicationContext, T, S extends SuperService<T, A>> extends Activity implements ControllerReceiver<T>{

	Class<S> serviceClass = null;
	
	public SuperActivity(Class<S> serviceClass) {
		this.serviceClass = serviceClass;
	}
	private static final String TAG = "SuperActivity";
	protected T controller = null;
	ControllerConnector<A, T, S> connector = new ControllerConnector<A, T, S>(this, this);
	
	public void onController(T controller) {
		this.controller = controller;
	}

	@Override
	protected void onStart() {
		super.onStart();
		connector.connectController(serviceClass);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		connector.disconnectController();
	}
	
	public static void notifyUser(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
	
	public void notifyUser(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
	
}
