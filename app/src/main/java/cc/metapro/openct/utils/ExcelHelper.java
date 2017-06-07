package cc.metapro.openct.utils;

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

import android.support.annotation.NonNull;

import net.lingala.zip4j.exception.ZipException;

import org.json.JSONException;
import org.json.JSONStringer;

import java.io.IOException;

import cc.metapro.openct.XLSXReader;

public class ExcelHelper {

    public static String xlsxToTable(@NonNull String path) {
        try {
            XLSXReader reader = new XLSXReader(path);
            return reader.getSheets()[0];
        } catch (ZipException | IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String tableToJson(@NonNull String table) throws JSONException {
        String[] rows = table.split("\n");
        String[][] tableContents = new String[rows.length][];
        int i = 0;
        for (String row : rows) {
            String[] cols = row.split("\t");
            tableContents[i++] = cols;
        }

        String[] headers = null;
        if (tableContents.length > 0) {
            headers = tableContents[0];
        }

        if (headers == null) {
            return "";
        }

        JSONStringer stringer = new JSONStringer();
        stringer.array();
        for (i = 1; i < tableContents.length; i++) {
            stringer.object();
            for (int j = 0; j < tableContents[i].length && j < headers.length; j++) {
                stringer.key(headers[j].trim()).value(tableContents[i][j]);
            }
            stringer.endObject();
        }
        stringer.endArray();

        return stringer.toString();
    }
}
