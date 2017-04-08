package cc.metapro.openct.customviews;

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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.LoginPresenter;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.base.MyObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class LinkSelectionDialog extends DialogFragment {

    private static final String TAG = LinkSelectionDialog.class.getSimpleName();
    private static String TYPE;
    private static Elements sLinks;
    private static Document DOCUMENT;
    private static LoginPresenter mPresenter;
    private Element mTarget;

    private AlertDialog mDialog;

    public static LinkSelectionDialog newInstance(String type, Document document, LoginPresenter presenter) {
        TYPE = type;
        DOCUMENT = document;
        mPresenter = presenter;
        return new LinkSelectionDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.target_selection)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.not_in_range_above, null)
                .create();

        setView();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button positiveButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                final Button neutralButton = mDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Constants.TYPE_CLASS.equals(TYPE)) {
                            Constants.advCustomInfo.setFirstClassUrlPattern(mTarget.toString());
                        } else if (Constants.TYPE_GRADE.equals(TYPE)) {
                            Constants.advCustomInfo.setFirstGradeUrlPattern(mTarget.toString());
                        } else if (Constants.TYPE_BORROW.equals(TYPE)) {
                            Constants.advCustomInfo.setFirstBorrowPattern(mTarget.toString());
                        }
                        DBManger.getInstance(getActivity()).updateAdvCustomInfo(Constants.advCustomInfo);
                        Constants.checkAdvCustomInfo(getActivity());
                        mPresenter.loadTargetPage(getFragmentManager(), mTarget.absUrl("href"));
                        dismiss();
                    }
                });

                neutralButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Constants.TYPE_CLASS.equals(TYPE)) {
                            Constants.advCustomInfo.setFirstClassUrlPattern(mTarget.toString());
                        } else if (Constants.TYPE_GRADE.equals(TYPE)) {
                            Constants.advCustomInfo.setFirstGradeUrlPattern(mTarget.toString());
                        } else if (Constants.TYPE_BORROW.equals(TYPE)) {
                            Constants.advCustomInfo.setFirstBorrowPattern(mTarget.toString());
                        }

                        sLinks = DOCUMENT.select("a");
                        neutralButton.setText(R.string.click_to_go);
                        neutralButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Constants.TYPE_CLASS.equals(TYPE)) {
                                    Constants.advCustomInfo.addClassUrlPattern(mTarget.toString());
                                } else if (Constants.TYPE_GRADE.equals(TYPE)) {
                                    Constants.advCustomInfo.addGradeUrlPattern(mTarget.toString());
                                } else if (Constants.TYPE_BORROW.equals(TYPE)) {
                                    Constants.advCustomInfo.addBorrowPattern(mTarget.toString());
                                }

                                if (Constants.TYPE_GRADE.equalsIgnoreCase(TYPE) || Constants.TYPE_CLASS.equalsIgnoreCase(TYPE)) {
                                    Observable<Document> observable = Observable.create(new ObservableOnSubscribe<Document>() {
                                        @Override
                                        public void subscribe(ObservableEmitter<Document> e) throws Exception {
                                            CmsFactory factory = Loader.getCms(getActivity());
                                            e.onNext(factory.getPageDom(mTarget.absUrl("href")));
                                        }
                                    });

                                    Observer<Document> observer = new MyObserver<Document>(TAG) {
                                        @Override
                                        public void onNext(Document document) {
                                            sLinks = document.select("a");

                                        }
                                    };

                                    observable.subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(observer);
                                } else if (Constants.TYPE_BORROW.equalsIgnoreCase(TYPE)) {
//                                    LibraryFactory factory = Loader.getLibrary(getActivity());
                                }
                            }
                        });
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Constants.TYPE_CLASS.equals(TYPE)) {
                                    Constants.advCustomInfo.addClassUrlPattern(mTarget.toString());
                                } else if (Constants.TYPE_GRADE.equals(TYPE)) {
                                    Constants.advCustomInfo.addGradeUrlPattern(mTarget.toString());
                                } else if (Constants.TYPE_BORROW.equals(TYPE)) {
                                    Constants.advCustomInfo.addBorrowPattern(mTarget.toString());
                                }
                                DBManger.getInstance(getActivity()).updateAdvCustomInfo(Constants.advCustomInfo);
                                Constants.checkAdvCustomInfo(getActivity());
                                mPresenter.loadTargetPage(getFragmentManager(), mTarget.absUrl("href"));
                                dismiss();
                            }
                        });
                    }
                });
            }
        });

        return mDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constants.checkAdvCustomInfo(getActivity());
    }

    private void setView() {
        switch (TYPE) {
            case Constants.TYPE_CLASS:
                sLinks = DOCUMENT.select("a:matches(课表|课程)");
                break;
            case Constants.TYPE_GRADE:
                sLinks = DOCUMENT.select("a:matches(成绩)");
                break;
            case Constants.TYPE_SEARCH:
                break;
            case Constants.TYPE_BORROW:
                sLinks = DOCUMENT.select("a:matches(借阅)");
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TYPE = null;
        mPresenter = null;
        sLinks = null;
        DOCUMENT = null;
    }

    static class LinkAdapter extends RecyclerView.Adapter<LinkAdapter.LinkHolder> {

        private LayoutInflater mInflater;

        LinkAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public LinkHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_header, parent, false);
            return new LinkHolder(view);
        }

        @Override
        public void onBindViewHolder(LinkHolder holder, int position) {
            holder.setText(sLinks.get(position).text());
        }

        @Override
        public int getItemCount() {
            return sLinks.size();
        }

        static class LinkHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.header_text)
            TextView mHeaderText;

            LinkHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            void setText(String text) {
                mHeaderText.setText(text);
            }
        }
    }
}
