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
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;

import org.jsoup.nodes.Document;

import cc.metapro.openct.R;
import cc.metapro.openct.custom.CustomActivity;
import cc.metapro.openct.customviews.CaptchaDialog;
import cc.metapro.openct.customviews.LinkSelectionDialog;
import cc.metapro.openct.customviews.TableChooseDialog;
import cc.metapro.openct.utils.base.LoginPresenter;

public final class ActivityUtils {

    private static ProgressDialog sProgressDialog;

    public static void showCaptchaDialog(FragmentManager manager, LoginPresenter presenter) {
        try {
            CaptchaDialog.newInstance(presenter).show(manager, "captcha_dialog");
        } catch (Exception ignored) {

        }
    }

    public static void showLinkSelectionDialog(FragmentManager manager, String type, Document document, LoginPresenter presenter) {
        LinkSelectionDialog.newInstance(type, document, presenter, true, false).show(manager, "link_selection_dialog");
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

    private static void showProgressDialog(Context context, @StringRes int messageId, boolean cancelable) {
        if (sProgressDialog != null) {
            sProgressDialog.dismiss();
            sProgressDialog = null;
        }
        sProgressDialog = new ProgressDialog(context);
        sProgressDialog.setMessage(context.getString(messageId));
        sProgressDialog.setCancelable(cancelable);
        sProgressDialog.show();
    }

    public static void showProgressDialog(Context context, @StringRes int messageId) {
        showProgressDialog(context, messageId, true);
    }

    public static void dismissProgressDialog() {
        if (sProgressDialog != null) {
            sProgressDialog.dismiss();
            sProgressDialog = null;
        }
    }

    public static AlertDialog addViewToAlertDialog(@NonNull final AlertDialog.Builder builder, @NonNull final View view) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }

        ScrollView scrollView = new ScrollView(builder.getContext());
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        scrollView.addView(view);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) builder.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        AlertDialog dialog = builder.setView(scrollView).create();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        return dialog;
    }

    public static AlertDialog.Builder getAlertBuilder(@NonNull Context context, @StringRes int title) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, null);
    }
}
