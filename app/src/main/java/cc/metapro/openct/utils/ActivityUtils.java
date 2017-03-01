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
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import com.scottyab.aescrypt.AESCrypt;

import org.jsoup.nodes.Document;

import java.security.GeneralSecurityException;

import cc.metapro.openct.LoginPresenter;
import cc.metapro.openct.R;
import cc.metapro.openct.custom.CustomActivity;
import cc.metapro.openct.customviews.CaptchaDialog;
import cc.metapro.openct.customviews.LinkSelectionDialog;
import cc.metapro.openct.customviews.TableChooseDialog;

public final class ActivityUtils {

    public static ProgressDialog getProgressDialog(Context context, int messageId) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage(context.getString(messageId));
        return pd;
    }

    public static void encryptionCheck(final Context context) {
        boolean cmsPasswordEncrypted = PrefHelper.getBoolean(context, R.string.pref_cms_password_encrypted);
        boolean libPasswordEncrypted = PrefHelper.getBoolean(context, R.string.pref_lib_password_encrypted);
        if (!PrefHelper.getBoolean(context, R.string.pref_need_encryption)) {
            if (cmsPasswordEncrypted) {
                try {
                    PrefHelper.putBoolean(context, R.string.pref_cms_password_encrypted, false);
                    String password = PrefHelper.getString(context, R.string.pref_cms_password, "");
                    password = AESCrypt.decrypt(Constants.seed, password);
                    PrefHelper.putString(context, R.string.pref_cms_password, password);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }

            if (libPasswordEncrypted) {
                try {
                    PrefHelper.putBoolean(context, R.string.pref_lib_password_encrypted, false);
                    String password = PrefHelper.getString(context, R.string.pref_lib_password, "");
                    password = AESCrypt.decrypt(Constants.seed, password);
                    PrefHelper.putString(context, R.string.pref_cms_password, password);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (!cmsPasswordEncrypted) {
                try {
                    PrefHelper.putBoolean(context, R.string.pref_cms_password_encrypted, true);
                    String password = PrefHelper.getString(context, R.string.pref_cms_password, "");
                    password = AESCrypt.encrypt(Constants.seed, password);
                    PrefHelper.putString(context, R.string.pref_cms_password, password);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }

            if (!libPasswordEncrypted) {
                try {
                    PrefHelper.putBoolean(context, R.string.pref_lib_password_encrypted, true);
                    String password = PrefHelper.getString(context, R.string.pref_lib_password, "");
                    password = AESCrypt.encrypt(Constants.seed, password);
                    PrefHelper.putString(context, R.string.pref_lib_password, password);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void showCaptchaDialog(FragmentManager manager, LoginPresenter presenter) {
        try {
            CaptchaDialog.newInstance(presenter).show(manager, "captcha_dialog");
        } catch (Exception ignored) {

        }
    }

    public static void showLinkSelectionDialog(FragmentManager manager, String type, Document document, LoginPresenter presenter) {
        LinkSelectionDialog.newInstance(type, document, presenter).show(manager, "link_selection_dialog");
    }

    public static void showTableChooseDialog(FragmentManager manager, String type, Document document, LoginPresenter presenter) {
        TableChooseDialog.newInstance(type, document, presenter).show(manager, "table_choose_dialog");
    }

    public static void showAdvCustomTip(final Context context, final String type) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.load_fail)
                .setMessage(R.string.load_fail_tip)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CustomActivity.actionStart(context, type);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create().show();
    }
}
