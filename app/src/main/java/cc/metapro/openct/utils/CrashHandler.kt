package cc.metapro.openct.utils

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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import cc.metapro.openct.OpenCT
import cc.metapro.openct.R
import java.io.*
import java.util.*

class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private var mContext: OpenCT? = null

    private fun init(context: OpenCT) {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        mContext = context
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (ContextCompat.checkSelfPermission(mContext!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {
                dumpExceptionToSDCard(ex)
                if (mDefaultHandler != null) {
                    mDefaultHandler!!.uncaughtException(thread, ex)
                }
                Thread(Runnable {
                    Looper.prepare()
                    Toast.makeText(mContext, R.string.crash_pop, Toast.LENGTH_LONG).show()
                    Looper.loop()
                }).start()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    @Throws(IOException::class)
    private fun dumpExceptionToSDCard(ex: Throwable) {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            Log.w(TAG, "sdcard unmounted,skip dump exception")
        }

        val file = File(Environment.getExternalStorageDirectory(), FILE_NAME)
        try {
            val pw = PrintWriter(BufferedWriter(FileWriter(file)))
            pw.println(Date().toString())
            dumpPhoneInfo(pw)
            pw.println()
            ex.printStackTrace(pw)

            pw.close()
        } catch (e: Exception) {
            Log.e(TAG, "dump crash info failed")
        }

    }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun dumpPhoneInfo(pw: PrintWriter) {
        val pm = mContext!!.packageManager
        val pi = pm.getPackageInfo(mContext!!.packageName, PackageManager.GET_ACTIVITIES)
        pw.print("OpenCT Version: ")
        pw.print(pi.versionName)
        pw.print("_")
        pw.println(pi.versionCode)

        pw.print("OS Version: ")
        pw.print(Build.VERSION.RELEASE)
        pw.print("_")
        pw.println(Build.VERSION.SDK_INT)

        pw.print("Vendor: ")
        pw.println(Build.MANUFACTURER)

        pw.print("Model: ")
        pw.println(Build.MODEL)
    }

    companion object {

        private val TAG = "OpenCT"
        private val FILE_NAME = "openct-crash.txt"

        fun initInstance(context: OpenCT): CrashHandler {
            val handler = CrashHandler()
            handler.init(context)
            return handler
        }
    }

}
