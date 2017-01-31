package cc.metapro.openct.classdetail;

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
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.utils.RecyclerViewHelper;

public class ClassDetailActivity extends AppCompatActivity {

    private static EnrichedClassInfo mInfo;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @BindView(R.id.appbar_image)
    ImageView mImageView;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    private boolean editEnabled = false;
    private ClassDetailAdapter mDetailAdapter;

    public static void actionStart(Context context, EnrichedClassInfo info) {
        mInfo = info;
        Intent intent = new Intent(context, ClassDetailActivity.class);
        context.startActivity(intent);
    }

    @OnClick(R.id.fab)
    public void toggleEnable() {
        if (editEnabled) {
            editEnabled = false;
            mFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_edit));
            mDetailAdapter.disableEdit();
        } else {
            editEnabled = true;
            mFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_dest_ok));
            mDetailAdapter.enableEdit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mImageView.setBackgroundColor(mInfo.getColor());
        mDetailAdapter = new ClassDetailAdapter(this, mInfo);
        RecyclerViewHelper.setRecyclerView(this, mRecyclerView, mDetailAdapter);
    }
}
