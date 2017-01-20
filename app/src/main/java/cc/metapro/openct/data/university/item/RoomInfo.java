package cc.metapro.openct.data.university.item;

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

import java.util.HashMap;
import java.util.Map;

public class RoomInfo {

    private String mPlace;

    private Map<String, Boolean> mAvailInfo;

    public RoomInfo(int week, int day, int time, @NonNull String place) {
        mAvailInfo = new HashMap<>();
        mAvailInfo.put(getQueryString(week, day, time), true);
        mPlace = place;
    }

    public void addInfo(int week, int day, int time) {
        mAvailInfo.put(getQueryString(week, day, time), true);
    }

    public boolean isWanted(String place) {
        return mPlace.startsWith(place);
    }

    public boolean isAvailable(int week, int day, int time) {
        String s = getQueryString(week, day, time);
        return mAvailInfo.get(s);
    }

    public String getPlace() {
        return mPlace;
    }

    public String getAllAvailTime() {
        StringBuilder sb = new StringBuilder();
        for (String s : mAvailInfo.keySet()) {
            String[] tmp = s.split("&.*?&");
            int end = Integer.parseInt(tmp[2]);
            sb.append("第 ").append(tmp[0]).append(" 周 ");
            sb.append("周 ").append(tmp[1]).append("\n");
            sb.append(end - 1).append(", ").append(end).append(" 节").append("\n\n\n");
        }
        sb.delete(sb.length() - 3, sb.length() - 1);
        return sb.toString();
    }

    private String getQueryString(int week, int day, int time) {
        return week + "&w&" + day + "&d&" + time + "&t&";
    }
}
