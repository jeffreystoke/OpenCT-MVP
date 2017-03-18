package cc.metapro.openct.splash.schoolselection;

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
import android.support.annotation.Keep;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.openctservice.ServiceGenerator;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.university.UniversityInfo;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.MyObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

class SchoolAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private List<String> mSchools;
    private LayoutInflater inflater;
    private List<String> mAllSchools;

    SchoolAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        List<UniversityInfo> universityList = DBManger.getInstance(context).getSchools();
        mAllSchools = new ArrayList<>();
        mSchools = new ArrayList<>();
        if (universityList.isEmpty()) {
            showGitNotice(context);
        } else {
            setSchools(universityList);
        }
    }

    private void setSchools(List<UniversityInfo> universityList) {
        Collections.sort(universityList);
        mAllSchools.clear();
        mSchools.clear();
        for (UniversityInfo info : universityList) {
            mAllSchools.add(info.name);
            mSchools.add(info.name);
        }
    }

    private void showGitNotice(final Context context) {
        Observable.create(new ObservableOnSubscribe<List<UniversityInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<UniversityInfo>> e) throws Exception {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.notice)
                        .setMessage(R.string.fetch_remote_school_list_tip)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog progressDialog = ActivityUtils.getProgressDialog(context, R.string.loading_university_info_list);
                                progressDialog.show();

                                Observable<List<UniversityInfo>> observable = Observable.create(new ObservableOnSubscribe<List<UniversityInfo>>() {
                                    @Override
                                    public void subscribe(ObservableEmitter<List<UniversityInfo>> e) throws Exception {
                                        List<UniversityInfo> universityInfoList = ServiceGenerator
                                                .createOpenCTService().getOnlineUniversityInfo().execute().body();
                                        if (universityInfoList == null || universityInfoList.isEmpty()) {
                                            e.onError(new Exception(context.getString(R.string.fetch_school_list_fail)));
                                        } else {
                                            e.onNext(universityInfoList);
                                        }
                                    }
                                });
                                Observer<List<UniversityInfo>> observer = new MyObserver<List<UniversityInfo>>("OpenCT GIT FETCH") {
                                    @Override
                                    public void onNext(final List<UniversityInfo> universityList) {
                                        progressDialog.dismiss();
                                        DBManger.updateSchools(context, universityList);
                                        setSchools(universityList);
                                        Toast.makeText(context, R.string.updated_schools, Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        super.onError(e);
                                        progressDialog.dismiss();
                                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                };

                                observable.subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(observer);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
            }
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    void setTextFilter(String filter) {
        List<String> targetList = new ArrayList<>();
        for (String s : mAllSchools) {
            if (s.contains(filter)) {
                targetList.add(s);
            }
        }
        mSchools = targetList;
    }

    void clearTextFilter() {
        mSchools.clear();
        mSchools.addAll(mAllSchools);
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.item_header, parent, false);
            holder.headerText = (TextView) convertView.findViewById(R.id.header_text);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        String headerText = mSchools.get(position).substring(0, 2);
        holder.headerText.setText(headerText);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        return mSchools.get(position).substring(0, 2).hashCode();
    }

    @Override
    public int getCount() {
        return mSchools.size();
    }

    @Override
    public Object getItem(int position) {
        return mSchools.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_school, parent, false);
            holder.schoolText = (TextView) convertView.findViewById(R.id.school_name_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.schoolText.setText(mSchools.get(position));
        return convertView;
    }

    private class HeaderViewHolder {
        TextView headerText;
    }

    private class ViewHolder {
        TextView schoolText;
    }
}
