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

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.source.local.DBManger;
import cc.metapro.openct.data.source.local.LocalHelper;
import cc.metapro.openct.data.source.remote.RemoteSource;
import cc.metapro.openct.data.university.UniversityInfo;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.base.MyObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
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
            getRemoteSchoolList(context);
        } else {
            setSchools(universityList);
        }
    }

    private void setSchools(List<UniversityInfo> universityList) {
        Collections.sort(universityList);
        mAllSchools.clear();
        mSchools.clear();
        for (UniversityInfo info : universityList) {
            mAllSchools.add(info.getName());
            mSchools.add(info.getName());
        }
    }

    private void getRemoteSchoolList(final Context context) {
        Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(@NonNull ObservableEmitter observableEmitter) throws Exception {
                ActivityUtils.showProgressDialog(context, R.string.loading_university_info_list);
                Observer<List<UniversityInfo>> observer = new MyObserver<List<UniversityInfo>>("Fetch_Universities") {
                    @Override
                    public void onNext(List<UniversityInfo> universityInfos) {
                        super.onNext(universityInfos);
                        ActivityUtils.dismissProgressDialog();
                    }
                };

                Observable.create(new ObservableOnSubscribe<List<UniversityInfo>>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<List<UniversityInfo>> observableEmitter) throws Exception {
                        RemoteSource source = new RemoteSource(LocalHelper.getUniversity(context).getName());
                        List<UniversityInfo> universityList = source.getUniversities();
                        DBManger.updateSchools(context, universityList);
                        setSchools(universityList);
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(observer);
            }
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    Observable setTextFilter(final String filter) {
        return Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(@NonNull ObservableEmitter observableEmitter) throws Exception {
                synchronized (SchoolAdapter.class) {
                    if (TextUtils.isEmpty(filter)) {
                        mSchools = mAllSchools;

                    } else {
                        String t = filter.trim();
                        char[] chars = t.toCharArray();
                        String[] s = new String[chars.length];
                        for (int i = 0; i < chars.length; i++) {
                            s[i] = chars[i] + "";
                        }

                        List<String> targetList = new ArrayList<>();
                        for (String tmp : mAllSchools) {
                            boolean match = true;
                            for (String c : s) {
                                if (!tmp.contains(c)) {
                                    match = false;
                                    break;
                                }
                            }
                            if (match) {
                                targetList.add(tmp);
                            }
                        }
                        mSchools = targetList;
                    }
                }
                observableEmitter.onNext("");
            }
        })
                .subscribeOn(Schedulers.io());
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
            convertView = inflater.inflate(R.layout.item_floating_header, parent, false);
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
