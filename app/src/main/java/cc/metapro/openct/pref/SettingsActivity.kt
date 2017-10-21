package cc.metapro.openct.pref

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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cc.metapro.openct.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)

        // setup actionbar
        val ab = actionBar
        ab?.setDisplayHomeAsUpEnabled(true)

        fragmentManager
                .beginTransaction()
                .replace(R.id.pref_container, SchoolPreferenceFragment())
                .commit()
    }

    override fun onStop() {
        super.onStop()
        storeCustom()
    }

    private fun storeCustom() {
    }
}
