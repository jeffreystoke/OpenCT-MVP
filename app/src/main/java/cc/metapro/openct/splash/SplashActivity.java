package cc.metapro.openct.splash;

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

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.myclass.ClassActivity;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.PrefHelper;
import me.relex.circleindicator.CircleIndicator;

public class SplashActivity extends AppCompatActivity {

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    @BindView(R.id.indicator)
    CircleIndicator mIndicator;

    @BindView(R.id.fab)
    FloatingActionButton mFab;
    private int currentPageIndex = 0;
    private InitPagerAdapter mPagerAdapter;

    @OnClick(R.id.fab)
    public void nextPage() {
        mViewPager.setCurrentItem(currentPageIndex + 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PrefHelper.getBoolean(this, R.string.pref_initialed)) {
            Intent intent = new Intent(this, ClassActivity.class);
            startActivity(intent);
            finish();
        }
        PrefHelper.putBoolean(this, R.string.pref_initialed, true);

        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        setViewPager();
    }

    private void setViewPager() {
        mPagerAdapter = new InitPagerAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mPagerAdapter.notifyDataSetChanged();
        mIndicator.setViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPageIndex = position;
                if (position == 2) {
                    mFab.setImageResource(R.drawable.ic_ok);
                    mFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPagerAdapter.storeSettings();
                            Intent intent = new Intent(SplashActivity.this, ClassActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        ActivityUtils.encryptionCheck(this);
        super.onDestroy();
    }
}
