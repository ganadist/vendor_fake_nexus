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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Slog;

/**
 * Application that sets the provisioned bit, like SetupWizard does.
 */
public class DefaultActivity extends Activity {
    private static final String TAG="FakeNexusProvision";

    private PackageManager mPm;

    private void enableSetupWizard() {
        final boolean device_provisioned = Settings.Global.getInt(
                getContentResolver(),
                Settings.Global.DEVICE_PROVISIONED, 0) == 1;
        final boolean user_setup_completed = Settings.Secure.getInt(
                getContentResolver(),
                Settings.Secure.USER_SETUP_COMPLETE, 0) == 1;

        ComponentName name;
        if (!device_provisioned || !user_setup_completed) {
            Slog.v(TAG, "Device is not provisioned yet. Enable SetupWizard");
            // enable setup wizard for provisioning
            final String setupwizard = "com.google.android.setupwizard";
            final String[] activities = {
                    "com.google.android.setupwizard.SetupWizardActivity",
            };
            for (String activity : activities) {
                try {
                    name = new ComponentName(setupwizard, activity);
                    mPm.setComponentEnabledSetting(name,
                            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, 0);
                } catch (Exception e) { }
            }
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

        enableSetupWizard();
        disableProvision();

        // terminate the activity.
        finish();
    }
}

