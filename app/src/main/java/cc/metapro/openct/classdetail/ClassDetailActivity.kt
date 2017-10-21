package cc.metapro.openct.classdetail

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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import cc.metapro.openct.R
import cc.metapro.openct.data.university.model.classinfo.ClassTime
import cc.metapro.openct.data.university.model.classinfo.EnrichedClassInfo
import cc.metapro.openct.utils.base.BaseActivity
import cc.metapro.openct.utils.base.BasePresenter
import kotlinx.android.synthetic.main.activity_class_detail.*
import java.util.*

class ClassDetailActivity : BaseActivity() {

    private lateinit var mDetailAdapter: ClassDetailAdapter
    private lateinit var mInfoEditing: EnrichedClassInfo
    private lateinit var mClassTimes: MutableList<ClassTime>
    private lateinit var mOldName: String

    override val presenter: BasePresenter?
        get() = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
    }

    override val layout: Int
        get() = R.layout.activity_class_detail

    private fun showColorPicker() {
//        val dialog = ColorPickerDialog.newBuilder().setColor(mInfoEditing.color).create()
//        dialog.setColorPickerDialogListener(object : ColorPickerDialogListener {
//            override fun onColorSelected(dialogId: Int, @ColorInt color: Int) {
//                mInfoEditing.color = color
//                mBackground.setColorFilter(color)
//            }
//
//            override fun onDialogDismissed(dialogId: Int) {
//
//            }
//        })
//        dialog.show(fragmentManager, "color_picker")
    }

    override fun onResume() {
        super.onResume()
        mOldName = intent.getStringExtra(KEY_CLASS_NAME)
        mInfoEditing = EnrichedClassInfo(getString(R.string.new_class), getString(R.string.mandatory), ClassTime())

        mClassTimes = ArrayList(mInfoEditing.timeSet)
        Collections.sort(mClassTimes)

//        mName.setText(mInfoEditing.name)
//        mType.setText(mInfoEditing.type)
//        mBackground.setColorFilter(mInfoEditing.color)
        setRecyclerView()
    }

    private fun setRecyclerView() {
//        mDetailAdapter = ClassDetailAdapter(this, mClassTimes)
//        RecyclerViewHelper.setRecyclerView(this, recycler_view, mDetailAdapter)
//        mRecyclerView.isItemViewSwipeEnabled = true
//        mRecyclerView.setOnItemMoveListener(object : OnItemMoveListener {
//            override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
//                return false
//            }
//
//            override fun onItemDismiss(position: Int) {
//                val toRemove = mClassTimes[position]
//                mClassTimes.remove(toRemove)
//                mDetailAdapter.notifyDataSetChanged()
//                val prefix = mName.text.toString() + " " +
//                        DateHelper.weekDayTrans(this@ClassDetailActivity, toRemove.weekDay) + " " +
//                        toRemove.timeString + " "
//
//                val msg = prefix + getString(R.string.deleted)
//                val snackbar = Snackbar.make(mRecyclerView, msg, BaseTransientBottomBar.LENGTH_INDEFINITE)
//                snackbar.setAction(android.R.string.cancel) {
//                    mClassTimes.add(toRemove)
//                    mDetailAdapter.notifyDataSetChanged()
//                    snackbar.dismiss()
//                    val msg = prefix + getString(R.string.restored)
//                    Snackbar.make(mRecyclerView, msg, BaseTransientBottomBar.LENGTH_LONG).show()
//                    mRecyclerView.smoothScrollToPosition(mClassTimes.size - 1)
//                }
//                snackbar.show()
//            }
//        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.class_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val id = item.itemId
//        if (id == R.id.add) {
//            if (!mClassTimes.isEmpty()) {
//                mClassTimes.add(0, ClassTime(mClassTimes[mClassTimes.size - 1]))
//            } else {
//                mClassTimes.add(0, ClassTime())
//            }
//            mDetailAdapter.notifyDataSetChanged()
//        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
//        mInfoEditing.name = mName.text.toString()
//        mInfoEditing.type = mType.text.toString()
//        mInfoEditing.setTimes(ArraySet(mClassTimes))
//        try {
//            if (mClassTimes.isEmpty()) {
//                try {
//                    DBManger.getInstance(this).updateSingleClass(mOldName, "", null)
//                } catch (e: Exception) {
//                    Log.d(TAG, e.message, e)
//                }
//
//            } else {
//                try {
//                    DBManger.getInstance(this).updateSingleClass(mOldName, mInfoEditing.name!!, mInfoEditing)
//                } catch (e: Exception) {
//                    Log.d(TAG, e.message, e)
//                    Toast.makeText(this, R.string.class_with_same_name, Toast.LENGTH_LONG).show()
//                    return
//                }
//
//            }
//        } finally {
//            Constants.sClasses.setInfoByName(mOldName, mInfoEditing)
//        }
        super.onBackPressed()
    }

    companion object {

        private val KEY_CLASS_NAME = "class_name"
        private val TAG = ClassDetailActivity::class.java.name

        fun actionStart(context: Context, name: String) {
            val intent = Intent(context, ClassDetailActivity::class.java)
            intent.putExtra(KEY_CLASS_NAME, name)
            context.startActivity(intent)
        }
    }
}
