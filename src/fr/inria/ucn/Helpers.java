/*******************************************************************************
 * Copyright (C) 2014 MUSE team Inria Paris - Rocquencourt
 * 
 * This file is part of UCNDataCollector.
 * 
 * UCNDataCollector is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * UCNDataCollector is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero Public License for more details.
 * 
 * You should have received a copy of the GNU Affero Public License
 * along with UCNDataCollector.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.inria.ucn;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Misc helper methods.
 * 
 * @author Anna-Kaisa Pietilainen <anna-kaisa.pietilainen@inria.fr>
 */
public final class Helpers {
	
	/* Hide the constructor, this class only has static methods */
	private Helpers() {};
	
	/* CPU wakelock for keeping the service alive on the background */
	private static PowerManager.WakeLock lock = null;
	
	/* Wifi wakelock for keeping the wifi device alive on the background */
	private static WifiManager.WifiLock wifilock = null;
	
	/**
	 * Acquire the wakelock to keep the CPU running.
	 * @param c
	 */
	public static synchronized void acquireLock(Context c) {
		PowerManager mgr = (PowerManager)c.getSystemService(Context.POWER_SERVICE);
		if (lock==null) {
			lock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,Constants.CPU_WAKE_LOCK);		
			lock.setReferenceCounted(true);
		}
		Log.d(Constants.LOGTAG, "acquire lock");
		lock.acquire();
	}
	
	/**
	 * Release the wakelock and let CPU sleep.
	 */
	public static synchronized void releaseLock() {
		Log.d(Constants.LOGTAG, "release lock");
		if (lock!=null)
			lock.release();
	}

	/**
	 * Acquire the wifi wakelock.
	 * @param c
	 */
	public static synchronized void acquireWifiLock(Context c) {
		WifiManager mgr = (WifiManager)c.getSystemService(Context.WIFI_SERVICE);
		if (wifilock==null) {
			wifilock = mgr.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "ucn");
			wifilock.setReferenceCounted(false);
		}
		Log.d(Constants.LOGTAG, "acquire wifilock");
		wifilock.acquire();
		acquireLock(c);
	}
	
	/**
	 * Release the wifi wakelock.
	 */
	public static synchronized void releaseWifiLock() {
		Log.d(Constants.LOGTAG, "release wifilock");
		if (wifilock!=null)
			wifilock.release();
		releaseLock();
	}
    
    /**
     * Retrieves a system property
     * @param key the property key
     * @param def the value to be returned if the key could not be resolved
     */
    public static String getSystemProperty(String key, String def) {
        try {
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            return getString.invoke(null, key).toString();
        } catch (Exception ex) {
            return def;
        }
    }
    
	/**
	 * Enable broadcast received component.
	 * @param c
	 * @param component
	 */
	public static void enableReceiver(Context c, @SuppressWarnings("rawtypes") Class component) {
		ComponentName receiver = new ComponentName(c, component);
		PackageManager pm = c.getPackageManager();
		pm.setComponentEnabledSetting(
				receiver,
		        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
		        PackageManager.DONT_KILL_APP);		
	}

	/**
	 * Disable broadcast received component.
	 * @param c
	 * @param component
	 */
	public static void disableReceiver(Context c, @SuppressWarnings("rawtypes") Class component) {		
		ComponentName receiver = new ComponentName(c, component);
		PackageManager pm = c.getPackageManager();
		pm.setComponentEnabledSetting(
				receiver,
		        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		        PackageManager.DONT_KILL_APP);		
	}

	/**
	 * Collectors and Listeners should use this method to send the results to the service.
	 * @param c
	 * @param cid data collection id (maps to mongodb collection used to store the data)
	 * @param ts  periodic collection timestamp or event time if triggered by timestamp
	 * @param data 
	 */
	@SuppressLint("SimpleDateFormat")
	public static void sendResultObj(Context c, String cid, long ts, JSONObject data) {
	    try {
	    	
			// wrap the collected data object to a common object format
			JSONObject res = new JSONObject();
	
			// data collection in the backend db
			res.put("collection", cid);
			
			// store unique user id to each result object
			res.put("uid", getDeviceUuid(c));
			
			// app version to help to detect data format changes
			try {
				PackageManager manager = c.getPackageManager();
				PackageInfo info = manager.getPackageInfo(c.getPackageName(), 0);
				res.put("app_version_name", info.versionName);
				res.put("app_version_code", info.versionCode);
			} catch (NameNotFoundException e) {
			}
			
			// event and current time in UTC JSON date format
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			res.put("ts_event", sdf.format(new Date(ts)));
			res.put("ts", sdf.format(new Date()));
			res.put("tz", TimeZone.getDefault().getID()); // devices current timezone
			res.put("tz_offset", TimeZone.getDefault().getOffset(ts)); // ts offset to this event
			
			// the data obj
			res.put("data", data);
			
			// ask the service to handle the data
			Intent intent = new Intent(c, CollectorService.class);
			intent.setAction(Constants.ACTION_DATA);
			intent.putExtra(Constants.INTENT_EXTRA_DATA, res.toString());
			c.startService(intent);
	    
	    	Log.d(Constants.LOGTAG, res.toString(4));
	    	
	    } catch (JSONException ex) {
			Log.w(Constants.LOGTAG, "failed to create json obj",ex);
	    }
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String getTelephonyPhoneType(int type) {
	    switch (type) {
	    case TelephonyManager.PHONE_TYPE_CDMA:
	      return "CDMA";
	    case TelephonyManager.PHONE_TYPE_GSM:
	      return "GSM";
	    case TelephonyManager.PHONE_TYPE_NONE:
	      return "None";
	    default:
	      return "Unknown["+type+"]";
	    }
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String getTelephonyNetworkType(int type) {
		switch (type) {
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				return "1xRTT";
			case TelephonyManager.NETWORK_TYPE_CDMA:
				return "CDMA";
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return "EDGE";
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				return "EVDO_0";
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				return "EVDO_A";
			case TelephonyManager.NETWORK_TYPE_GPRS:
				return "GPRS";
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				return "HSDPA";
			case TelephonyManager.NETWORK_TYPE_HSPA:
				return "HSPA";
			case TelephonyManager.NETWORK_TYPE_HSUPA:
				return "HSUPA";
			case TelephonyManager.NETWORK_TYPE_IDEN:
				return "IDEN";
			case TelephonyManager.NETWORK_TYPE_UMTS:
				return "UMTS";
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
				return "EVDO_B";
			case TelephonyManager.NETWORK_TYPE_LTE:
				return "LTE";
			case TelephonyManager.NETWORK_TYPE_EHRPD:
				return "EHRPD";
			case TelephonyManager.NETWORK_TYPE_HSPAP:
				return "HSPAP";
			default: 
				return "UNKNOWN [" + type + "]";
		}
	}	
	
	// Source : http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
	private volatile static UUID uuid = null;
    private static final String ID_PREFS_FILE = "ucn_device_id.xml";
    private static final String ID_PREFS_DEVICE_ID = "ucn_device_id";
    private static final String BAD_UUID = "9774d56d682e549c";
	private static UUID getUuid(Context c) {
		final SharedPreferences prefs = c.getSharedPreferences(ID_PREFS_FILE, Context.MODE_PRIVATE);
		final String id = prefs.getString(ID_PREFS_DEVICE_ID, null);
		final UUID uuid;
		if (id != null) {
			uuid = UUID.fromString(id);
		} else {
			final String androidId = Secure.getString(c.getContentResolver(), Secure.ANDROID_ID);
			try {
				if (!BAD_UUID.equals(androidId)) {
					uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
				} else {
					final String deviceId = ((TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
					if (deviceId != null)
						uuid = UUID.nameUUIDFromBytes(deviceId.getBytes("utf8"));
					else
						uuid = UUID.randomUUID();
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			prefs.edit().putString(ID_PREFS_DEVICE_ID, uuid.toString()).commit();
		}
		return uuid;
	}

	/**
	 * Get a unique id for this device.
	 * @param c
	 * @return
	 */
	public static UUID getDeviceUuid(Context c) {
		if (uuid==null) {
			synchronized (Helpers.class) {
				if (uuid==null)
					uuid = getUuid(c);				
			}
		}
		return uuid;
	}
	
	/**
	 * 
	 * @param c
	 * @param uid
	 * @return
	 * @throws JSONException
	 */
	public static JSONArray getPackagesForUid(Context c, int uid) throws JSONException {
		PackageManager pm = c.getPackageManager();
		JSONArray res = new JSONArray();
		String[] pkgs = pm.getPackagesForUid(uid);
		if (pkgs!=null) {
			for (int i = 0; i < pkgs.length; i++) {
				try {
					CharSequence appLabel = 
							pm.getApplicationLabel(
									pm.getApplicationInfo(
											pkgs[i], 
											PackageManager.GET_META_DATA));
					JSONObject pkg = new JSONObject();
					pkg.put("package", pkgs[i]);
					pkg.put("app_label",appLabel.toString());
					res.put(pkg);
				} catch (NameNotFoundException e) {
				} catch (Exception e) {
				}
			}
		}
		return res;
	}
	
	/**
	 * Check for the OpenVPN for Android app:
	 * https://play.google.com/store/apps/details?id=de.blinkt.openvpn
	 * @param c
	 * @return
	 */
	public static boolean isOpenVPNClientInstalled(Context c) {
		boolean res = false;
		PackageManager pm = c.getPackageManager();
		for (ApplicationInfo ai : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
			if (ai.packageName.contains("de.blinkt.openvpn")) {
				res = true;
				break;
			}
		}
		return res;
	}
	
	/**
	 * Check for the Llama app.
	 * @param c
	 * @return
	 */
	public static boolean isLlamaInstalled(Context c) {
		boolean res = false;
		PackageManager pm = c.getPackageManager();
		for (ApplicationInfo ai : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
			if (ai.packageName.contains("com.kebab.Llama")) {
				res = true;
				break;
			}
		}
		return res;		
	}
    
    /**
     * Read a given file and return a list of lines.
     * @param file
     * @return
     */
    public static List<String> readProc(String file) {
		List<String> lines = new ArrayList<String>();
		BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file), 500);
            String line;
			while ((line = in.readLine()) != null) {
				lines.add(line.trim());
			}
        } catch (FileNotFoundException e) {
        	Log.w(Constants.LOGTAG, "could not find " + file, e);
        } catch (IOException e) {
        	Log.w(Constants.LOGTAG, "could not read " + file, e);
        }
        if (in!=null)
			try {
				in.close();
			} catch (IOException e) {
			}
        return lines;
	}
    
    /**
     * Request data collection sample.
     *  
     * @param context
     * @param wl         Need wakelock?
     */
    public static void doSample(Context context, boolean wl) {
    	if (wl)
			Helpers.acquireLock(context);
		Intent sintent = new Intent(context, CollectorService.class);
		sintent.setAction(Constants.ACTION_COLLECT);
		if (wl)
			sintent.putExtra(Constants.INTENT_EXTRA_RELEASE_WL, true); // request service to release the wl
		context.startService(sintent);
    }
    
    /**
     * Request data upload.
     *  
     * @param context
     * @param wl         Need wakelock?
     */
    public static void doUpload(Context context, boolean wl) {
    	if (wl)
			Helpers.acquireLock(context);
		Intent sintent = new Intent(context, CollectorService.class);
		sintent.setAction(Constants.ACTION_UPLOAD);
		if (wl)
			sintent.putExtra(Constants.INTENT_EXTRA_RELEASE_WL, true); // request service to release the wl
		context.startService(sintent);
    }
    
    /**
     * Start (schedule) the data collector service.
     *  
     * @param context
     * @param wl         Need wakelock?
     */
    public static void startCollector(Context context, boolean wl) {
    	if (wl)
			Helpers.acquireLock(context);
		Intent sintent = new Intent(context, CollectorService.class);
		sintent.setAction(Constants.ACTION_SCHEDULE);
		sintent.putExtra(Constants.INTENT_EXTRA_SCHEDULER_START, true);
		if (wl)
			sintent.putExtra(Constants.INTENT_EXTRA_RELEASE_WL, true); // request service to release the wl
		context.startService(sintent);
    }
    
    /**
     * Stop (schedule) the data collector service.
     *  
     * @param context
     * @param wl         Need wakelock?
     */
    public static void stopCollector(Context context, boolean wl) {
    	if (wl)
			Helpers.acquireLock(context);
		Intent sintent = new Intent(context, CollectorService.class);
		sintent.setAction(Constants.ACTION_SCHEDULE);
		sintent.putExtra(Constants.INTENT_EXTRA_SCHEDULER_START, false);
		if (wl)
			sintent.putExtra(Constants.INTENT_EXTRA_RELEASE_WL, true); // request service to release the wl
		context.startService(sintent);    	
    }

    /**
     * @param c
     * @return <code>True</code> if user has enabled the night-time mode and current time is
     * within night, else <code>False</code>.
     */
    public static boolean isNightTime(Context c) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		if (!prefs.getBoolean(Constants.PREF_STOP_NIGHT, false))
			return false;
		
		int nstart = prefs.getInt(Constants.PREF_NIGHT_START, 23*3600);
		int nstop = prefs.getInt(Constants.PREF_NIGHT_STOP, 6*3600);
			
		Calendar nightstart = Calendar.getInstance();
		nightstart.roll(Calendar.HOUR_OF_DAY, -1*nightstart.get(Calendar.HOUR_OF_DAY));
		nightstart.roll(Calendar.MINUTE, -1*nightstart.get(Calendar.MINUTE));
		nightstart.roll(Calendar.SECOND, -1*nightstart.get(Calendar.SECOND));
		nightstart.roll(Calendar.MILLISECOND, -1*nightstart.get(Calendar.MILLISECOND));
		nightstart.add(Calendar.SECOND, nstart);

		Calendar nightstop = Calendar.getInstance();
		nightstop.roll(Calendar.HOUR_OF_DAY, -1*nightstop.get(Calendar.HOUR_OF_DAY));
		nightstop.roll(Calendar.MINUTE, -1*nightstop.get(Calendar.MINUTE));
		nightstop.roll(Calendar.SECOND, -1*nightstop.get(Calendar.SECOND));
		nightstop.roll(Calendar.MILLISECOND, -1*nightstop.get(Calendar.MILLISECOND));
		nightstop.add(Calendar.SECOND, nstop);
		if (nightstop.before(nightstart))
			nightstop.add(Calendar.HOUR, 24);
			
		Log.d(Constants.LOGTAG, "nightstart " + nstart + " -> " + nightstart.toString());
		Log.d(Constants.LOGTAG, "nightstop " + nstop + " -> " + nightstop.toString());
		
		Calendar now = Calendar.getInstance();		
		return (now.after(nightstart) && now.before(nightstop));
    }
    
    /**
     * 
     * @param c
     * @return -1 if not at night-time (or feature disabled), else milliseconds until morning.
     */
    public static long getNightEnd(Context c) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		if (!prefs.getBoolean(Constants.PREF_STOP_NIGHT, false))
			return -1;
		
		int nstart = prefs.getInt(Constants.PREF_NIGHT_START, 23*3600);
		int nstop = prefs.getInt(Constants.PREF_NIGHT_STOP, 6*3600);
			
		Calendar nightstart = Calendar.getInstance();
		nightstart.roll(Calendar.HOUR_OF_DAY, -1*nightstart.get(Calendar.HOUR_OF_DAY));
		nightstart.roll(Calendar.MINUTE, -1*nightstart.get(Calendar.MINUTE));
		nightstart.roll(Calendar.SECOND, -1*nightstart.get(Calendar.SECOND));
		nightstart.roll(Calendar.MILLISECOND, -1*nightstart.get(Calendar.MILLISECOND));
		nightstart.add(Calendar.SECOND, nstart);

		Calendar nightstop = Calendar.getInstance();
		nightstop.roll(Calendar.HOUR_OF_DAY, -1*nightstop.get(Calendar.HOUR_OF_DAY));
		nightstop.roll(Calendar.MINUTE, -1*nightstop.get(Calendar.MINUTE));
		nightstop.roll(Calendar.SECOND, -1*nightstop.get(Calendar.SECOND));
		nightstop.roll(Calendar.MILLISECOND, -1*nightstop.get(Calendar.MILLISECOND));
		nightstop.add(Calendar.SECOND, nstop);
		if (nightstop.before(nightstart))
			nightstop.add(Calendar.HOUR, 24);
			
		Log.d(Constants.LOGTAG, "nightstart " + nstart + " -> " + nightstart.toString());
		Log.d(Constants.LOGTAG, "nightstop " + nstop + " -> " + nightstop.toString());
		
		Calendar now = Calendar.getInstance();		
		if (now.after(nightstart) && now.before(nightstop)) {
			return nightstop.getTimeInMillis();
		} else {
			return -1;
		}
    }    
}
