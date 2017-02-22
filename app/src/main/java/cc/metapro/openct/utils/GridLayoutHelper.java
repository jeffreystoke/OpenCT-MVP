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

import android.support.v4.widget.Space;
import android.widget.GridLayout;

public class GridLayoutHelper {

    public static void fillGrids(GridLayout gridLayout) {
        if (gridLayout == null) return;
        for (int i = 0; i < gridLayout.getRowCount(); i++) {
            for (int j = 0; j < gridLayout.getColumnCount(); j++) {
                android.support.v7.widget.GridLayout.LayoutParams params = new android.support.v7.widget.GridLayout.LayoutParams();
                params.width = Constants.CLASS_WIDTH;
                params.height = Constants.CLASS_BASE_HEIGHT;
                params.rowSpec = android.support.v7.widget.GridLayout.spec(i);
                params.columnSpec = android.support.v7.widget.GridLayout.spec(j);
                gridLayout.addView(new Space(gridLayout.getContext()), params);
            }
        }
    }

    public static void fillGrids(android.support.v7.widget.GridLayout gridLayout) {
        if (gridLayout == null) return;
        for (int i = 0; i < gridLayout.getRowCount(); i++) {
            for (int j = 0; j < gridLayout.getColumnCount(); j++) {
                android.support.v7.widget.GridLayout.LayoutParams params = new android.support.v7.widget.GridLayout.LayoutParams();
                params.width = Constants.CLASS_WIDTH;
                params.height = Constants.CLASS_BASE_HEIGHT;
                params.rowSpec = android.support.v7.widget.GridLayout.spec(i);
                params.columnSpec = android.support.v7.widget.GridLayout.spec(j);
                gridLayout.addView(new Space(gridLayout.getContext()), params);
            }
        }
    }
}
