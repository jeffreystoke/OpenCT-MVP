package cc.metapro.openct.homepage.schoolselection;

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
import android.support.annotation.Keep;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cc.metapro.openct.R;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

@Keep
class SchoolAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private List<String> mSchools;
    private LayoutInflater inflater;
    private String[] allSchools;

    SchoolAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        allSchools = context.getResources().getStringArray(R.array.school_names);
        mSchools = new ArrayList<>();
        mSchools.addAll(Arrays.asList(allSchools));
    }

    void setTextFilter(String filter) {
        List<String> targetList = new ArrayList<>();
        for (String s : allSchools) {
            if (s.contains(filter)) {
                targetList.add(s);
            }
        }
        mSchools = targetList;
    }

    void clearTextFilter() {
        mSchools.clear();
        mSchools.addAll(Arrays.asList(allSchools));
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
