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

import android.support.annotation.Keep;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cc.metapro.openct.data.source.StoreHelper;

@Keep
public class BorrowInfo {
    private String mAuthor;
    private String mBookTitle;
    private String mContent;
    private String mBorrowDate;
    private String mDueDate;

    public BorrowInfo(String bookTitle, String author, String content, String borrowDate, String dueDate) {
        mBookTitle = bookTitle;
        mAuthor = author;
        mContent = content;
        mBorrowDate = borrowDate;
        mDueDate = dueDate;
    }

    public boolean isExceeded() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            Date date = format.parse(mDueDate);
            Date now = new Date();
            return date.before(now);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getBookTitle() {
        return mBookTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getContent() {
        return mContent;
    }

    public String getBorrowDate() {
        return "借阅时间  " + mBorrowDate;
    }

    public String getDueDate() {
        return "应还时间  " + mDueDate;
    }

    @Override
    public String toString() {
        return StoreHelper.getJsonText(this);
    }

}
