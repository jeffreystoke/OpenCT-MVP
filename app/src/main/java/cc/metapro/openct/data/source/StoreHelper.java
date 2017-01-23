package cc.metapro.openct.data.source;

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

import android.content.Context;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Keep
public final class StoreHelper {

    private final static Gson gson = new Gson();

    @NonNull
    static String getAssetText(Context context, String filename) throws IOException {
        InputStream fis = null;
        BufferedReader br = null;
        try {
            fis = context.getAssets().open(filename);
            br = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String tmp = br.readLine();
            while (tmp != null) {
                sb.append(tmp);
                tmp = br.readLine();
            }
            return sb.toString();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getJsonText(Object infos) {
        return gson.toJson(infos);
    }

    public static void storeBytes(String path, InputStream in) throws IOException {
        DataInputStream din = null;
        DataOutputStream out = null;
        try {
            din = new DataInputStream(in);
            out = new DataOutputStream(new FileOutputStream(path));
            byte[] buffer = new byte[2048];
            int count;
            while ((count = din.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
        } finally {
            if (din != null) {
                try {
                    din.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void delFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
}
