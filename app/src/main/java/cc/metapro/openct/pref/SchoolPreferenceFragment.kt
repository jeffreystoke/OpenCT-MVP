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

import android.content.Intent
import android.os.Bundle
import android.preference.*
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import butterknife.ButterKnife
import cc.metapro.openct.R
import cc.metapro.openct.data.source.local.DBManger
import cc.metapro.openct.data.source.local.LocalHelper
import cc.metapro.openct.data.university.DetailCustomInfo
import cc.metapro.openct.splash.schoolselection.SchoolSelectionActivity
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.PrefHelper
import cc.metapro.openct.utils.REHelper
import com.scottyab.aescrypt.AESCrypt
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import java.util.*

class SchoolPreferenceFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {

    private var mSchoolPreference: Preference? = null
    private var mCmsPasswordPreference: Preference? = null
    private var mLibPasswordPreference: Preference? = null
    private var mCustomEnablePreference: SwitchPreference? = null
    private var mCustomSchoolNamePreference: Preference? = null
    private var mDailyClassCountPreference: Preference? = null
    private var mClassSettingScreen: PreferenceScreen? = null
    private var mPreferences: MutableList<Preference>? = null

    private var mTimePreferences: MutableList<Preference>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_school)
        setHasOptionsMenu(false)
        ButterKnife.bind(this, activity)
        mPreferences = ArrayList<Preference>()
        mTimePreferences = ArrayList<Preference>()

        bindPreferences()
        bindListener()
    }

    private fun bindPreferences() {
        mDailyClassCountPreference = findPreference(getString(R.string.pref_daily_class_count))
        mPreferences!!.add(mDailyClassCountPreference!!)

        mClassSettingScreen = findPreference(getString(R.string.pref_class_settings)) as PreferenceScreen
        addTimePreferences()

        findPreference(getString(R.string.pref_custom_action_clear)).onPreferenceClickListener = Preference.OnPreferenceClickListener {
            AlertDialog.Builder(activity)
                    .setTitle(R.string.notice)
                    .setMessage(R.string.clear_action_confirm)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { dialog, which ->
                        val manger = DBManger.getInstance(activity)
                        manger!!.delAdvancedCustomInfo()
                        Constants.sDetailCustomInfo = DetailCustomInfo()
                        Constants.checkAdvCustomInfo(activity)
                    }.show()
            false
        }

        mSchoolPreference = findPreference(getString(R.string.pref_school_name))
        mSchoolPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            if (mCustomEnablePreference!!.isChecked) {
                Toast.makeText(activity, R.string.disable_custom_first, Toast.LENGTH_LONG).show()
            } else {
                startActivityForResult(Intent(activity, SchoolSelectionActivity::class.java),
                        SchoolSelectionActivity.REQUEST_SCHOOL_NAME)
            }
            true
        }
        mPreferences!!.add(mSchoolPreference!!)
        mCmsPasswordPreference = findPreference(getString(R.string.pref_cms_password))
        mPreferences!!.add(mCmsPasswordPreference!!)

        mLibPasswordPreference = findPreference(getString(R.string.pref_lib_password))
        mPreferences!!.add(mLibPasswordPreference!!)

        mCustomEnablePreference = findPreference(getString(R.string.pref_custom_enable)) as SwitchPreference
        mCustomEnablePreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            if (mCustomEnablePreference!!.isChecked) {
                bindSummary(mSchoolPreference!!, PrefHelper.getString(activity, mCustomSchoolNamePreference!!.key, ""))
            } else {
                bindSummary(mSchoolPreference!!, PrefHelper.getString(activity, mSchoolPreference!!.key, ""))
            }
            true
        }
        mPreferences!!.add(mCmsPasswordPreference!!)

        mCustomSchoolNamePreference = findPreference(getString(R.string.pref_custom_school_name))
        mPreferences!!.add(mCustomSchoolNamePreference!!)

        mPreferences!!.add(findPreference(getString(R.string.pref_cms_username)))
        mPreferences!!.add(findPreference(getString(R.string.pref_lib_username)))
        mPreferences!!.add(findPreference(getString(R.string.pref_custom_cms_sys)))
        mPreferences!!.add(findPreference(getString(R.string.pref_custom_cms_url)))
        mPreferences!!.add(findPreference(getString(R.string.pref_custom_lib_sys)))
        mPreferences!!.add(findPreference(getString(R.string.pref_custom_lib_url)))
        mPreferences!!.add(findPreference(getString(R.string.pref_homepage_selection)))
        mPreferences!!.add(findPreference(getString(R.string.pref_empty_class_motto)))
        mPreferences!!.add(findPreference(getString(R.string.pref_class_name_re)))
        mPreferences!!.add(findPreference(getString(R.string.pref_class_type_re)))
        mPreferences!!.add(findPreference(getString(R.string.pref_class_time_re)))
        mPreferences!!.add(findPreference(getString(R.string.pref_class_during_re)))
        mPreferences!!.add(findPreference(getString(R.string.pref_class_teacher_re)))
        mPreferences!!.add(findPreference(getString(R.string.pref_class_place_re)))
        mPreferences!!.add(findPreference(getString(R.string.pref_every_class_time)))
        mPreferences!!.add(findPreference(getString(R.string.pref_rest_time)))
    }

    private fun bindListener() {
        for (preference in mPreferences!!) {
            preference.onPreferenceChangeListener = this
            val value = preference.sharedPreferences.getString(preference.key, "")
            bindSummary(preference, value)
        }
    }

    private fun addTimePreferences() {
        val count = Integer.parseInt(PrefHelper.getString(activity, R.string.pref_daily_class_count, "12"))
        for (preference in mTimePreferences!!) {
            mClassSettingScreen!!.removePreference(preference)
            mPreferences!!.remove(preference)
        }
        mTimePreferences!!.clear()

        for (i in 0..count - 1) {
            val preference = Preference(activity)
            val key = Constants.TIME_PREFIX + i
            var prefix = ""
            if (8 + i < 10) prefix += "0"
            val t = if (8 + i > 23) 23 else 8 + i
            val defaultValue = prefix + t + ":00"
            preference.key = key
            preference.title = getString(R.string.the_class_seq, i + 1)
            preference.setDefaultValue(defaultValue)
            val value = PrefHelper.getString(activity, key, defaultValue)
            preference.summary = value

            mPreferences!!.add(preference)
            preference.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
                val value = PrefHelper.getString(activity, key, defaultValue)
                val parts = REHelper.getUserSetTime(value)
                val dialog = TimePickerDialog.newInstance({ view, hourOfDay, minute, second ->
                    var value = ""
                    if (hourOfDay < 10) {
                        value += "0"
                    }
                    value += hourOfDay.toString() + ":"
                    if (minute < 10) {
                        value += "0"
                    }
                    value += minute
                    PrefHelper.putString(activity, key, value)
                    preference.summary = value
                }, parts[0], parts[1], 0, true)
                dialog.show(fragmentManager, "time_picker")
                true
            }
            mClassSettingScreen!!.addPreference(preference)
            mPreferences!!.add(preference)
            mTimePreferences!!.add(preference)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val value = newValue.toString()
        bindSummary(preference, value)

        if (preference == mCustomSchoolNamePreference) {
            if (mCustomEnablePreference!!.isChecked) {
                bindSummary(mSchoolPreference!!, value)
            }
        } else if (preference == mCmsPasswordPreference) {
            return encryption(preference, value, R.string.pref_cms_password)
        } else if (preference == mLibPasswordPreference) {
            return encryption(preference, value, R.string.pref_lib_password)
        } else if (preference == mDailyClassCountPreference) {
            PrefHelper.putString(activity, R.string.pref_daily_class_count, value)
            addTimePreferences()
            return false
        }
        return true
    }

    private fun encryption(preference: Preference, password: String, passwordId: Int): Boolean {
        var password = password
        if (!TextUtils.isEmpty(password)) {
            try {
                password = AESCrypt.encrypt(Constants.seed, password)
                PrefHelper.putString(activity, getString(passwordId), password)
                preference.setSummary(R.string.encrypted)
                return false
            } catch (e: Exception) {
                Log.e("ENCRYPTION FAIL", e.message)
            }

        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null) {
                val schoolName = data.getStringExtra(SchoolSelectionActivity.SCHOOL_RESULT)
                bindSummary(mSchoolPreference!!, schoolName)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun bindSummary(preference: Preference, value: String) {
        if (preference is ListPreference) {
            val listPref = preference
            val index = listPref.findIndexOfValue(value)
            listPref.summary = if (index >= 0) listPref.entries[index] else null
        } else if (preference !is CheckBoxPreference) {
            if (preference == mSchoolPreference) {
                val pref = preference.sharedPreferences
                if (pref.getBoolean(getString(R.string.pref_custom_enable), false)) {
                    preference.summary = pref.getString(getString(R.string.pref_custom_school_name), "")
                } else {
                    preference.summary = value
                }
                LocalHelper.needUpdateUniversity()
            } else {
                preference.summary = value
            }
        }
    }
}
