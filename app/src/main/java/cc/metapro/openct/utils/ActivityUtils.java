package cc.metapro.openct.utils;

/*
 *  Copyright 2016 - 2017 OpenCT open source class table
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;

import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

import cc.metapro.openct.LoginPresenter;
import cc.metapro.openct.R;
import cc.metapro.openct.customviews.CaptchaDialog;

@Keep
public final class ActivityUtils {

    private static final String TAG = "ACTIVITY_UTILS";

    private static ProgressDialog pd;

    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
    }

    public static ProgressDialog getProgressDialog(Context context, int messageId) {
        pd = new ProgressDialog(context);
        pd.setMessage(context.getString(messageId));
        return pd;
    }

    public static void dismissProgressDialog() {
        if (pd == null) return;
        if (pd.isShowing()) {
            pd.dismiss();
        }
    }

    public static void encryptionCheck(final Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean needEncrypt = preferences.getBoolean(context.getString(R.string.pref_need_encryption), true);
        boolean cmsPasswordEncrypted = preferences.getBoolean(context.getString(R.string.pref_cms_password_encrypted), false);
        boolean libPasswordEncrypted = preferences.getBoolean(context.getString(R.string.pref_lib_password_encrypted), false);

        SharedPreferences.Editor editor = preferences.edit();
        // 设置不加密, 将加密的部分还原
        if (!needEncrypt) {
            if (cmsPasswordEncrypted) {
                try {
                    editor.putBoolean(context.getString(R.string.pref_cms_password_encrypted), false);
                    String password = preferences.getString(context.getString(R.string.pref_cms_password), "");
                    password = AESCrypt.decrypt(Constants.seed, password);
                    editor.putString(context.getString(R.string.pref_cms_password), password);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }
            if (libPasswordEncrypted) {
                try {
                    editor.putBoolean(context.getString(R.string.pref_lib_password_encrypted), false);
                    String password = preferences.getString(context.getString(R.string.pref_lib_password), "");
                    password = AESCrypt.decrypt(Constants.seed, password);
                    editor.putString(context.getString(R.string.pref_lib_password), password);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }
        }
        // 设置加密, 将未加密的部分加密
        else {
            if (!cmsPasswordEncrypted) {
                String cmsPassword = preferences.getString(context.getString(R.string.pref_cms_password), "");
                try {
                    if (!TextUtils.isEmpty(cmsPassword)) {
                        cmsPassword = AESCrypt.encrypt(Constants.seed, cmsPassword);
                        editor.putString(context.getString(R.string.pref_cms_password), cmsPassword);
                        editor.putBoolean(context.getString(R.string.pref_cms_password_encrypted), true);
                    }
                } catch (Exception exp) {
                    Log.e(TAG, exp.getMessage(), exp);
                }
            }
            if (!libPasswordEncrypted) {
                try {
                    String libPassword = preferences.getString(context.getString(R.string.pref_lib_password), "");
                    if (!TextUtils.isEmpty(libPassword)) {
                        libPassword = AESCrypt.encrypt(Constants.seed, libPassword);
                        editor.putString(context.getString(R.string.pref_lib_password), libPassword);
                        editor.putBoolean(context.getString(R.string.pref_lib_password_encrypted), true);
                    }
                } catch (Exception exp) {
                    Log.e(TAG, exp.getMessage(), exp);
                }
            }
        }
        editor.apply();
    }

    public static void showCaptchaDialog(FragmentManager manager, LoginPresenter presenter) {
        CaptchaDialog.newInstance(presenter).show(manager, "captcha_dialog");
    }
}
