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

import android.support.annotation.NonNull;

import java.util.List;

import cc.metapro.openct.data.university.model.BookInfo;
import cc.metapro.openct.data.university.model.BorrowInfo;
import cc.metapro.openct.data.university.model.GradeInfo;
import cc.metapro.openct.data.university.model.classinfo.EnrichedClassInfo;

public interface SchoolSource {

    List<EnrichedClassInfo> getClasses();

    List<GradeInfo> getGrades();

    List<BorrowInfo> getBorrows();

    List<BookInfo> getBooks(@NonNull String type, @NonNull String query);
}
