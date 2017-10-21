package cc.metapro.openct.grades

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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cc.metapro.openct.R
import cc.metapro.openct.data.university.model.GradeInfo
import java.util.*

internal class GradeAdapter : RecyclerView.Adapter<GradeAdapter.GradeViewHolder>() {

    private val mGrades = ArrayList<GradeInfo>(0)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grade, parent, false)
        return GradeViewHolder(view)
    }

    override fun onBindViewHolder(holder: GradeViewHolder, position: Int) {
        val g = mGrades[position]
        holder.setInfo(g)
    }

    override fun getItemCount(): Int {
        return mGrades.size
    }

    fun updateGrades(grades: List<GradeInfo>) {
        mGrades.clear()
        mGrades.addAll(grades)
    }

    internal inner class GradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var mClassName: TextView

        fun setInfo(info: GradeInfo) {
            mClassName.text = info.toFullString()
        }
    }
}
