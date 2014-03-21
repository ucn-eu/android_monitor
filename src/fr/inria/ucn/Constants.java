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

/**
 * @author Anna-Kaisa Pietilainen <anna-kaisa.pietilainen@inria.fr>
 *
 */
public interface Constants {

	/** Android logger tag for this app. */
	public static final String LOGTAG = "fr.inria.ucn";

	/** Intent action: automatic re-schedule alarm. */
	public static final String ACTION_SCHEDULE_ALARM = "fr.inria.ucn.intent.action.SCHEDULE_ALARM";
	
	/** Intent action: periodic collection round alarm. */
	public static final String ACTION_COLLECT_ALARM = "fr.inria.ucn.intent.action.COLLECT_ALARM";
	
	/** Intent action: uploader alarm. */
	public static final String ACTION_UPLOAD_ALARM = "fr.inria.ucn.intent.action.UPLOAD_ALARM";

	/** Intent action: start/stop collecting data. */
	public static final String ACTION_SCHEDULE = "fr.inria.ucn.intent.action.SCHEDULE";
	
	/** Intent action: start periodic collection round. */
	public static final String ACTION_COLLECT = "fr.inria.ucn.intent.action.COLLECT";
	
	/** Intent action: collector/listener has data. */
	public static final String ACTION_DATA = "fr.inria.ucn.intent.action.DATA";
	
	/** Intent action: upload data to remote server. */
	public static final String ACTION_UPLOAD = "fr.inria.ucn.intent.action.UPLOAD";
	
	/** Intent action: service status broadcast. */
	public static final String ACTION_STATUS = "fr.inria.ucn.intent.action.STATUS";

	/** Intent action: ask service to release the cpu wakelock. */
	public static final String ACTION_RELEASE_WL = "fr.inria.ucn.intent.action.RELEASE_WL";
	
	/** Intent extra data key constants. */
	public static final String INTENT_EXTRA_DATA = "fr.inria.ucn.intent.DATA";
	public static final String INTENT_EXTRA_SCHEDULER_START = "fr.inria.ucn.intent.SCHEDULER";
	public static final String INTENT_EXTRA_STATUS_KEY = "fr.inria.ucn.intent.STATUS_KEY";
	public static final String INTENT_EXTRA_STATUS_VALUE = "fr.inria.ucn.intent.STATUS_VALUE";
	public static final String INTENT_EXTRA_RELEASE_WL = "fr.inria.ucn.intent.RELEASE_WL";
	
	/** Status keys (for status bcasts and datastore). */
	public static final String STATUS_LAST_UPLOAD = "fr.inria.ucn.datastore.STATUS_LAST_UPLOAD";
	public static final String STATUS_LAST_UPLOAD_FAILED = "fr.inria.ucn.datastore.STATUS_LAST_UPLOAD_FAILED";
	public static final String STATUS_RUNNING_SINCE = "fr.inria.ucn.datastore.STATUS_RUNNING_SINCE";

	/** Datastore null uptime value constant. */
	public static final String NULL_UPTIME = "0";

	/** CPU wake-lock identifier. */
	public static final String CPU_WAKE_LOCK = "fr.inria.ucn.collector.wakelock";
	
    /** User label for the device. */
    public static final String PREF_LABEL = "pref_label";
    /** Periodic collection interval. */
    public static final String PREF_INTERVAL = "pref_interval";
    /** Periodic collection night pause start. */
    public static final String PREF_NIGHT_START = "pref_start_hour";
    /** Periodic collection night pause stop. */
    public static final String PREF_NIGHT_STOP = "pref_stop_hour";
    /** Data upload enabled. */
    public static final String PREF_UPLOAD = "pref_upload";
    /** Data upload url. */
    public static final String PREF_UPLOAD_URL = "pref_upload_url";
    /** Data upload enabled. */
    public static final String PREF_UPLOAD_WIFI = "pref_upload_wifi";
    /** Data upload write log file. */
    public static final String PREF_WRITE = "pref_write";
    /** Data upload log file name. */
    public static final String PREF_WRITE_FILE = "pref_write_file";	
    /** VPN tunnel configuration in use. */
    public static final String PREF_VPN_PROFILE = "pref_vpn_profile";
    /** VPN category key. */
    public static final String PREF_VPN = "pref_vpn";

    /* Hidden prefs to store some static info */
    public static final String PREF_HIDDEN_FIRST = "pref_hidden_first";
}
