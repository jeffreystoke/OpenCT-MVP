package cc.metapro.openct

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

import android.app.Application
import cc.metapro.openct.utils.Constants
import com.blankj.utilcode.util.CrashUtils
import com.blankj.utilcode.util.Utils

class OpenCT : Application() {

    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        CrashUtils.init()

        initStatic()
    }

    private fun initStatic() {
        val metrics = resources.displayMetrics
        Constants.CLASS_WIDTH = Math.round(metrics.widthPixels * (2.0 / 15.0)).toInt()
        Constants.CLASS_BASE_HEIGHT = Math.round(metrics.heightPixels * (1.0 / 15.0)).toInt()
        Constants.NAME = getString(R.string.class_name)
        Constants.TIME = getString(R.string.class_time)
        Constants.TYPE = getString(R.string.class_type)
        Constants.DURING = getString(R.string.class_during)
        Constants.PLACE = getString(R.string.class_place)
        Constants.TEACHER = getString(R.string.class_teacher)
        Constants.CAPTCHA_FILE = cacheDir.path + "/captcha"
    }
}
