package cc.metapro.openct.data.university.item.classinfo;

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

import java.util.Arrays;

public class ClassDuring {

    @NonNull
    private boolean[] weeks = new boolean[30];
    private boolean odd = false;
    private boolean even = false;

    public ClassDuring(@NonNull boolean[] weeks, boolean odd, boolean even) {
        this.weeks = weeks;
        this.odd = odd;
        this.even = even;
    }

    public boolean hasClass(int week) {
        if (week > 0 && week < 30) {
            if (weeks[week - 1]) {
                if (odd && (week % 2 == 1)) return true;
                if (even && (week % 2 == 0)) return true;
                if (!odd && !even) return true;
            }
        }
        return false;
    }

    public int getLastWeek() {
        for (int i = weeks.length - 1; i >= 0; i--) {
            if (weeks[i]) return i + 1;
        }
        return 0;
    }

    public int getFirstWeek() {
        for (int i = 0; i < weeks.length; i++) {
            if (weeks[i]) return i + 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassDuring) {
            ClassDuring during = (ClassDuring) obj;
            return Arrays.equals(weeks, during.weeks) && odd == during.odd && even == during.even;
        }
        return false;
    }
}
