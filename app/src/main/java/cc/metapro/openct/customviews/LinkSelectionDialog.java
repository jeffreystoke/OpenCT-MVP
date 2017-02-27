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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.LoginPresenter;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.MyObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class LinkSelectionDialog extends DialogFragment {

    private static final String TAG = LinkSelectionDialog.class.getSimpleName();
    private static String TYPE;
    private static Elements mLinks;
    private static Document DOCUMENT;
    private static LoginPresenter mPresenter;
    @BindView(R.id.radio_group)
    RadioGroup mRadioGroup;

    private List<RadioButton> mRadioButtons;

    public static LinkSelectionDialog newInstance(String type, Document document, LoginPresenter presenter) {
        TYPE = type;
        DOCUMENT = document;
        mPresenter = presenter;
        return new LinkSelectionDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_link_selection, null);
        ButterKnife.bind(this, view);
        setView();
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.target_selection)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.not_in_range_above, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                final Button neutralButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEUTRAL);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < mRadioButtons.size(); i++) {
                            if (mRadioButtons.get(i).isChecked()) {
                                Element target = mLinks.get(i);
                                if (Constants.TYPE_CLASS.equals(TYPE)) {
                                    Constants.advCustomInfo.setFirstClassUrlPattern(target.toString());
                                } else if (Constants.TYPE_GRADE.equals(TYPE)) {
                                    Constants.advCustomInfo.setFirstGradeUrlPattern(target.toString());
                                } else if (Constants.TYPE_BORROW.equals(TYPE)) {
                                    Constants.advCustomInfo.setFirstBorrowPattern(target.toString());
                                }
                                DBManger.getInstance(getActivity()).updateAdvancedCustomClassInfo(Constants.advCustomInfo);
                                Constants.checkAdvCustomInfo(getActivity());
                                mPresenter.loadTargetPage(getFragmentManager(), target.absUrl("href"));
                                break;
                            }
                        }
                        dismiss();
                    }
                });

                neutralButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < mRadioButtons.size(); i++) {
                            if (mRadioButtons.get(i).isChecked()) {
                                Element target = mLinks.get(i);
                                if (Constants.TYPE_CLASS.equals(TYPE)) {
                                    Constants.advCustomInfo.setFirstClassUrlPattern(target.toString());
                                } else if (Constants.TYPE_GRADE.equals(TYPE)) {
                                    Constants.advCustomInfo.setFirstGradeUrlPattern(target.toString());
                                } else if (Constants.TYPE_BORROW.equals(TYPE)) {
                                    Constants.advCustomInfo.setFirstBorrowPattern(target.toString());
                                }
                                break;
                            }
                        }
                        mLinks = DOCUMENT.select("a");
                        addRadioOptions();
                        neutralButton.setText(R.string.click_to_go);
                        neutralButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for (int i = 0; i < mRadioButtons.size(); i++) {
                                    if (mRadioButtons.get(i).isChecked()) {
                                        Element target = mLinks.get(i);
                                        if (Constants.TYPE_CLASS.equals(TYPE)) {
                                            Constants.advCustomInfo.addClassUrlPattern(target.toString());
                                        } else if (Constants.TYPE_GRADE.equals(TYPE)) {
                                            Constants.advCustomInfo.addGradeUrlPattern(target.toString());
                                        } else if (Constants.TYPE_BORROW.equals(TYPE)) {
                                            Constants.advCustomInfo.addBorrowPattern(target.toString());
                                        }
                                        break;
                                    }
                                }
                                if (Constants.TYPE_GRADE.equalsIgnoreCase(TYPE) || Constants.TYPE_CLASS.equalsIgnoreCase(TYPE)) {
                                    for (int i = 0; i < mRadioButtons.size(); i++) {
                                        if (mRadioButtons.get(i).isChecked()) {
                                            final Element target = mLinks.get(i);
                                            ActivityUtils.getProgressDialog(getActivity(), R.string.loading_target_page);
                                            Observable<Document> observable = Observable.create(new ObservableOnSubscribe<Document>() {
                                                @Override
                                                public void subscribe(ObservableEmitter<Document> e) throws Exception {
                                                    CmsFactory factory = Loader.getCms(getActivity());
                                                    e.onNext(factory.getPageDom(target.absUrl("href")));
                                                }
                                            });

                                            Observer<Document> observer = new MyObserver<Document>(TAG) {
                                                @Override
                                                public void onNext(Document document) {
                                                    mLinks = document.select("a");
                                                    addRadioOptions();
                                                    ActivityUtils.dismissProgressDialog();
                                                }
                                            };

                                            observable.subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(observer);
                                        }
                                    }
                                } else if (Constants.TYPE_BORROW.equalsIgnoreCase(TYPE)) {
//                                    LibraryFactory factory = Loader.getLibrary(getActivity());
                                }
                            }
                        });
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for (int i = 0; i < mRadioButtons.size(); i++) {
                                    if (mRadioButtons.get(i).isChecked()) {
                                        Element target = mLinks.get(i);
                                        if (Constants.TYPE_CLASS.equals(TYPE)) {
                                            Constants.advCustomInfo.addClassUrlPattern(target.toString());
                                        } else if (Constants.TYPE_GRADE.equals(TYPE)) {
                                            Constants.advCustomInfo.addGradeUrlPattern(target.toString());
                                        } else if (Constants.TYPE_BORROW.equals(TYPE)) {
                                            Constants.advCustomInfo.addBorrowPattern(target.toString());
                                        }
                                        DBManger.getInstance(getActivity()).updateAdvancedCustomClassInfo(Constants.advCustomInfo);
                                        Constants.checkAdvCustomInfo(getActivity());
                                        mPresenter.loadTargetPage(getFragmentManager(), target.absUrl("href"));
                                        break;
                                    }
                                }
                                dismiss();
                            }
                        });
                    }
                });
            }
        });
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constants.checkAdvCustomInfo(getActivity());
    }

    private void setView() {
        switch (TYPE) {
            case Constants.TYPE_CLASS:
                mLinks = DOCUMENT.select("a:matches(课表|课程)");
                break;
            case Constants.TYPE_GRADE:
                mLinks = DOCUMENT.select("a:matches(成绩)");
                break;
            case Constants.TYPE_SEARCH:
                break;
            case Constants.TYPE_BORROW:
                mLinks = DOCUMENT.select("a:matches(借阅)");
                break;
        }

        addRadioOptions();
    }

    private void addRadioOptions() {
        mRadioButtons = new ArrayList<>(mLinks.size());
        mRadioGroup.removeAllViews();

        for (Element link : mLinks) {
            RadioButton button = new RadioButton(getActivity());
            button.setText(link.text());
            button.setGravity(Gravity.CENTER);
            mRadioGroup.addView(button);
            mRadioButtons.add(button);
        }
    }

}
