package cc.metapro.openct.allclasses

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
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import cc.metapro.openct.R
import cc.metapro.openct.utils.ExcelHelper
import cc.metapro.openct.utils.FileUtils
import cc.metapro.openct.utils.base.BaseDialog
import org.json.JSONException
import java.io.File


class ExcelDialog : BaseDialog() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(activity)
                .setTitle(R.string.import_from_excel)
                .setMessage(R.string.select_file_tip)
                .setPositiveButton(R.string.select_file, null)
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.refer_to_usage) { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://github.com/jeffreystoke/openct-mvp#import-from-xlsx-excel-2007")
                    startActivity(intent)
                }
                .create()

        dialog.setOnShowListener { dialog ->
            val positiveButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val hasPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_EXTERNAL_FILE_WRITE)
                    } else {
                        showFilerChooser()
                    }
                } else {
                    showFilerChooser()
                }
            }
        }
        return dialog
    }

    override fun onDestroy() {
        super.onDestroy()
        mCallback = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_SELECT_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val uri = data!!.data
            var path: String? = null
            path = FileUtils.getPath(activity, uri)
            if (path != null) {
                val file = File(path)
                Log.d("FileChooser", file.absolutePath)
                try {
                    val table = ExcelHelper.xlsxToTable(path)
                    mCallback!!.onJsonResult(ExcelHelper.tableToJson(table))
                    dismiss()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_FILE_WRITE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showFilerChooser()
            } else {
                dismiss()
                Toast.makeText(activity, R.string.no_write_permission, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showFilerChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_schedule_file)), FILE_SELECT_CODE)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.fail_file_chooser, Toast.LENGTH_LONG).show()
        }

    }

    interface ExcelCallback {
        fun onJsonResult(json: String)
    }

    companion object {

        private val FILE_SELECT_CODE = 101
        private val REQUEST_EXTERNAL_FILE_WRITE = 102

        private var mCallback: ExcelCallback? = null

        fun newInstance(callback: ExcelCallback): ExcelDialog {
            mCallback = callback
            return ExcelDialog()
        }
    }

}
