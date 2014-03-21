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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Anna-Kaisa Pietilainen <anna-kaisa.pietilainen@inria.fr>
 *
 */
public class OnBootReceiver extends BroadcastReceiver {

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			Log.d(Constants.LOGTAG, "received android.intent.action.BOOT_COMPLETED - re-schedule service");

			// get wake-lock to keep the CPU up
			Helpers.acquireLock(context);
			
			// start the service to do the actual work
			Intent sintent = new Intent(context, CollectorService.class);
			sintent.setAction(Constants.ACTION_SCHEDULE);
			sintent.putExtra(Constants.INTENT_EXTRA_SCHEDULER_START, true);
			sintent.putExtra(Constants.INTENT_EXTRA_RELEASE_WL, true); // request service to release the wl
			context.startService(sintent);
		}
	}
}