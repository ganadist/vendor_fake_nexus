/*
 * Copyright (C) 2008 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.provision;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Slog;

/**
 * Application that sets the provisioned bit, like SetupWizard does.
 */
public class DefaultActivity extends Activity {
    private static final String TAG="FakeNexusProvision";

    private PackageManager mPm;

    private void setProvisionState() {
        Settings.Global.putInt(
                getContentResolver(),
                Settings.Global.DEVICE_PROVISIONED, 0);
        Settings.Secure.putInt(
                getContentResolver(),
                Settings.Secure.USER_SETUP_COMPLETE, 0);
    }

    private static final ComponentInfo[] EMPTY = {};
    private void resetComponentsEnableState(ComponentInfo[] components) {
        if (components == null) {
            components = EMPTY;
        }
        ComponentName name;
        for (ComponentInfo info : components) {
            try {
                name = new ComponentName(info.packageName, info.name);
                mPm.setComponentEnabledSetting(name,
                        PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, 0);
            } catch (Exception e) {
                Slog.e(TAG, "cannot restore enable state.", e);
            }
        }
    }

    private void resetGoogleComponentEnableState() {
        final String[] packages = {
                "com.google.android.setupwizard",
                "com.google.android.gms",
                "com.google.android.googlequicksearchbox",
                "com.android.vending",
                "com.google.android.gm",
                "com.google.android.gsf",
                "com.google.android.apps.gcs",
        };

        Slog.v(TAG, "First boot with Fake Nexus Rom. Enable Google Components");

        // restore enable state as provisioning
        for (String pkg : packages) {
            try {
                PackageInfo info = mPm.getPackageInfo(pkg,
                    PackageManager.GET_ACTIVITIES |
                    PackageManager.GET_PROVIDERS |
                    PackageManager.GET_RECEIVERS |
                    PackageManager.GET_SERVICES |
                    PackageManager.MATCH_DISABLED_COMPONENTS );

                resetComponentsEnableState(info.activities);
                resetComponentsEnableState(info.providers);
                resetComponentsEnableState(info.receivers);
                resetComponentsEnableState(info.services);
            } catch (PackageManager.NameNotFoundException e) {
                Slog.e(TAG, "cannot find package: ", e);
            }
        }
    }

    private void resetRuntimePermissions() {
        Slog.v(TAG, "Reset all runtime permissions");
        IPackageManager pm =
                IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        try {
            pm.resetRuntimePermissions();
        } catch (RemoteException e) {
            Slog.e(TAG, "Remote exception", e);
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "Bad argument", e);
        } catch (SecurityException e) {
            Slog.e(TAG, "Operation not allowed: ", e);
        }
    }

    private void disableProvision() {
        Slog.v(TAG, "Provision will be completed. Disable Provision component");
        // remove this activity from the package manager.
        ComponentName name = new ComponentName(this, DefaultActivity.class);
        mPm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPm = getPackageManager();
        UserManager um = (UserManager)getSystemService(Context.USER_SERVICE);

        if (um.isSystemUser()) {
            resetRuntimePermissions();

            /* set provision state to initiate setup wizard */
            setProvisionState();

            /* reset enabled state of Google compoenet packages */
            resetGoogleComponentEnableState();
        }

        disableProvision();

        /* start HOME intent for testing easy */
        final Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(homeIntent);

        // terminate the activity.
        finish();
    }
}

