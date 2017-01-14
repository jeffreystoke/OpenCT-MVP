package cc.metapro.openct.customviews;

/*
 *  Copyright 2015 2017 metapro.cc Jeffctor
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

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class InitDiaolgHelper {

    @BindView(R.id.info_init_cms_username)
    EditText cmsUsername;
    @BindView(R.id.info_init_cms_password)
    EditText cmsPassword;
    @BindView(R.id.info_init_lib_username)
    EditText libUsername;
    @BindView(R.id.info_init_lib_password)
    EditText libPassword;
    @BindView(R.id.info_init_school)
    Spinner schoolSpinner;
    @BindView(R.id.info_init_week)
    Spinner weekSpinner;
    private Context mContext;

    public InitDiaolgHelper(Context context) {
        mContext = context;
    }

    public AlertDialog getInitDialog() {
        AlertDialog.Builder ab = new AlertDialog.Builder(mContext);

        View view = LayoutInflater.from(mContext).inflate(R.layout.info_init_dialog_layout, null);

        ButterKnife.bind(this, view);

        ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String school = mContext.getResources().getStringArray(R.array.pref_school_values)[schoolSpinner.getSelectedItemPosition()];
                String week = mContext.getResources().getStringArray(R.array.pref_week_seq_values)[weekSpinner.getSelectedItemPosition()];
                SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = p.edit();
                editor.putString(Constants.PREF_SCHOOL_ABBR_KEY, school);
                editor.putString(Constants.PREF_CURRENT_WEEK_KEY, week);
                editor.putString(Constants.PREF_CMS_USERNAME_KEY, cmsUsername.getText().toString());
                editor.putString(Constants.PREF_CMS_PASSWORD_KEY, cmsPassword.getText().toString());
                editor.putString(Constants.PREF_LIB_USERNAME_KEY, libUsername.getText().toString());
                editor.putString(Constants.PREF_LIB_PASSWORD_KEY, libPassword.getText().toString());
                editor.apply();

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                alertDialog.setTitle("提示");
                alertDialog.setMessage("你还可以在 设置 中修改你的信息哦~");
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton("好的, 我知道了", null);
                alertDialog.show();

                ActivityUtils.encryptionCheck(mContext);

                Observable
                        .create(new ObservableOnSubscribe<Integer>() {
                            @Override
                            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                                Loader.loadUniversity(mContext);
                                e.onComplete();
                            }
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorReturn(new Function<Throwable, Integer>() {
                            @Override
                            public Integer apply(Throwable throwable) throws Exception {
                                Toast.makeText(mContext, "FATAL: 加载学校信息失败", Toast.LENGTH_LONG).show();
                                return 0;
                            }
                        })
                        .subscribe();
            }
        });
        ab.setTitle("初始化个人信息");
        ab.setCancelable(false);
        ab.setView(view);
        return ab.create();
    }

}
