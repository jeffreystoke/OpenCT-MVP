package cc.metapro.openct.data.source.local;

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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cc.metapro.openct.utils.CloseUtils;
import okio.Buffer;

public final class StoreHelper {

    private final static Gson gson = new Gson();

    public static String toJson(Object o) {
        return gson.toJson(o);
    }

    public static <T> List<T> fromJsonList(String jsonList, Class<T> classType) {
        JsonArray array = new JsonParser().parse(jsonList).getAsJsonArray();
        List<T> result = new ArrayList<>();
        for (JsonElement element : array) {
            result.add(gson.fromJson(element, classType));
        }
        return result;
    }

    static <T> T fromJson(String jsonElement, Class<T> tClass) {
        return gson.fromJson(jsonElement, tClass);
    }

    public static void storeBytes(String path, InputStream in) throws IOException {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new FileOutputStream(path));
            Buffer buffer = new Buffer();
            buffer.readFrom(in).writeTo(out);
        } finally {
            CloseUtils.close(out);
        }
    }

    public static void delFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
}
