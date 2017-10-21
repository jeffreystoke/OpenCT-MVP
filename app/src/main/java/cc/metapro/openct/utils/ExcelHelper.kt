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

import android.os.Environment
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ZipUtils
import org.json.JSONStringer
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.File

object ExcelHelper {

    private val outDir = File(Environment.getDataDirectory(), "tmp")

    /**
     * 将 Excel 2007+ 文件转换成 Json 数组对象 (只会提取第一个工作簿中的内容)
     * @param filePath 用户选择的 Excel 文件路径
     * @return 第一张数据表中的文本, 一行一串, 使用 \t 代替同行单元格分隔
     */
    fun getClassTableJson(filePath: String): String {
        // 判断文件是否存在
        val f = FileUtils.getFileByPath(filePath)
        if (!f.exists()) {
            throw Exception("couldn't find target excel file")
        }

        // 创建或清空输出文件夹
        if (!FileUtils.createOrExistsDir(outDir)) {
            throw Exception("couldn't create output dir")
        }
        FileUtils.deleteAllInDir(outDir)

        // 解压文件到固定目录
        ZipUtils.unzipFile(f, outDir)

        // 获取数字与文本的映射
        val sharedStrings = File(outDir, "xl/sharedStrings.xml")
        val strDoc = Jsoup.parse(sharedStrings.readText(), "", Parser.xmlParser())
        val valueMap = HashMap<String, String>()
        val elements = strDoc.getElementsByTag("t")
        for ((i, e) in elements.withIndex()) {
            valueMap.put("$i", e.text())
        }

        // 将映射的文本填入数字位置
        val worksheets = File(outDir, "xl/worksheets")
        val sheets = FileUtils.listFilesInDir(worksheets)
        if (sheets.isEmpty()) {
            throw Exception("no sheets available for parsing")
        }
        val targetSheet = sheets.first()
        val sheetDoc = Jsoup.parse(targetSheet.readText(), "", Parser.xmlParser())
        val rows = sheetDoc.getElementsByTag("row")
        val result = ArrayList<String>()
        for (row in rows) {
            val values = row.getElementsByTag("v")
            val builder = StringBuilder()
            for (v in values) {
                builder.append(valueMap[v.text()]).append("\t")
            }
            builder.delete(builder.length - 1, builder.length)
            result.add(builder.toString())
        }

        val tableContents = ArrayList<List<String>>()
        result.map { row -> row.split("\t").dropLastWhile { it.isEmpty() } }
                .forEachIndexed { idx, v -> tableContents[idx] = v }

        val headers: List<String>
        if (tableContents.isNotEmpty()) {
            headers = tableContents[0]
        } else {
            return ""
        }

        val stringer = JSONStringer()
        stringer.array()
        tableContents.filterIndexed { idx, _ -> idx > 0 }
                .forEachIndexed { i, _ ->
                    stringer.`object`()
                    tableContents[i].forEachIndexed { j, v ->
                        stringer.key(headers[j].trim()).value(v)
                    }
                    stringer.endObject()
                }
        stringer.endArray()

        return stringer.toString()
    }

}
