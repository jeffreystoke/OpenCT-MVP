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
import android.support.v4.view.ViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.myclass.ClassActivity;
import cc.metapro.openct.utils.PrefHelper;
import cc.metapro.openct.utils.base.BaseActivity;

public class SplashActivity extends BaseActivity {

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    private boolean misScrolled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PrefHelper.getBoolean(this, R.string.pref_initialed, false)) {
            startActivity(new Intent(this, ClassActivity.class));
            finish();
            return;
        }

        PrefHelper.putBoolean(this, R.string.pref_initialed, true);
        ButterKnife.bind(this);
        setViewPager();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_splash;
    }

    private void setViewPager() {
        final InitPagerAdapter pagerAdapter = new InitPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:
                        if (mViewPager.getCurrentItem() == pagerAdapter.getCount() - 1 && !misScrolled) {
                            pagerAdapter.getItem(pagerAdapter.getCount() - 1).setUserVisibleHint(false);
                            startActivity(new Intent(SplashActivity.this, ClassActivity.class));
                            finish();
                        }
                        misScrolled = false;
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        misScrolled = false;
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        misScrolled = true;
                        break;
                }
            }
        });
    }
}
