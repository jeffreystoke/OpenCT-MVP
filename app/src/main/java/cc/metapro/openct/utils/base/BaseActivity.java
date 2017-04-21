package cc.metapro.openct.utils.base;

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

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import cc.metapro.openct.R;
import cc.metapro.openct.utils.PrefHelper;


public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        int theme = PrefHelper.getInt(this, R.string.pref_theme_activity, R.style.AppTheme);
        setTheme(theme);
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
    }

    @LayoutRes
    protected abstract int getLayout();

    @StyleRes
    protected final int getThemeByColor(@ColorInt int color) {
        if (color == ContextCompat.getColor(this, R.color.material_white)) {
            return R.style.AppTheme;
        } else if (color == ContextCompat.getColor(this, R.color.material_blue)) {
            return R.style.AppTheme_Blue;
        } else if (color == ContextCompat.getColor(this, R.color.material_teal)) {
            return R.style.AppTheme_Teal;
        } else if (color == ContextCompat.getColor(this, R.color.material_green)) {
            return R.style.AppTheme_Green;
        } else if (color == ContextCompat.getColor(this, R.color.material_grey_blue)) {
            return R.style.AppTheme_Grey;
        } else if (color == ContextCompat.getColor(this, R.color.material_amber)) {
            return R.style.AppTheme_Amber;
        } else if (color == ContextCompat.getColor(this, R.color.material_indigo)) {
            return R.style.AppTheme_Indigo;
        } else if (color == ContextCompat.getColor(this, R.color.material_red)) {
            return R.style.AppTheme_Red;
        } else {
            throw new UnsupportedOperationException("the color is not supported");
        }
    }
}
