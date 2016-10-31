/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.provision;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;

import java.io.FileNotFoundException;
import java.io.IOException;


public class RingtoneMigrate extends BroadcastReceiver {

    private static final String TAG="FakeNexusProvision";
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private Context mContext;
    private PackageManager mPm;

    @Override
    public void onReceive(Context context, Intent intent) {
        Slog.v(TAG, "start Ringtone migration");
        mContext = context;
        mPm = context.getPackageManager();

        boolean failed = false;
        final String[] SOUND_SETTINGS = {
                Settings.System.RINGTONE,
                Settings.System.NOTIFICATION_SOUND,
                Settings.System.ALARM_ALERT,
        };
        for (String name: SOUND_SETTINGS) {
            if (!isRingtoneAvailable(name)) {
                if (!setDefaultRingtone(name)) {
                    failed = true;
                };
            }
        }

        if (failed) {
            Slog.e(TAG, "Ringtone migration is failed.");
        } else {
            Slog.v(TAG, "Ringtone migration is finished. Disable this component");
            disableProvision();
        }

    }

    private boolean isRingtoneAvailable(String name) {
        ContentResolver cr = mContext.getContentResolver();
        String uri = Settings.System.getString(cr, name);
        boolean found = false;
        if (uri != null) {
            try {
                ParcelFileDescriptor pfd = cr.openFileDescriptor(Uri.parse(uri), "r");
                pfd.close();
                found = true;
            } catch (FileNotFoundException e) {
                Slog.w(TAG, "cannot find ringtone: '" + uri + "' for " + name);
            } catch (IOException e) {
            }
        }
        return found;
    }

    private boolean setDefaultRingtone(String name) {
        String filename = SystemProperties.get(
                DEFAULT_RINGTONE_PROPERTY_PREFIX + name);
        boolean isSet = false;
        if (TextUtils.isEmpty(filename)) {
            Slog.w(TAG, "no factory ringtone is set for " + name + ". ignore it");
            return true;
        }

        ContentResolver cr = mContext.getContentResolver();
        String [] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
        };
        Cursor cursor = cr.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, projection,
                MediaStore.Audio.Media.DATA + " like '%" + filename + "'",
                null,
                null);

        if (cursor == null) {
            Slog.w(TAG, "cannot find factory ringtone for " + name);
            return isSet;
        }
        if (cursor.getCount() != 0) {
            Slog.i(TAG, "found ringtones : count = " + cursor.getCount() + " columns = " + cursor.getColumnCount());
            cursor.moveToFirst();

            int id = cursor.getInt(0);
            String data = cursor.getString(1);
            String uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI.toString() + "/" + id;
            Settings.System.putString(cr, name, uri);
            isSet = true;
            Slog.i(TAG, "Ringtone migration for " + name + " + is set : " + uri + "(" + data + ")");
        }
        cursor.close();

        return isSet;
    }

    private void disableProvision() {
        ComponentName name = new ComponentName(mContext, RingtoneMigrate.class);
        mPm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);

    }
}
