package cc.metapro.openct.customviews

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

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnEditorAction
import cc.metapro.openct.R
import cc.metapro.openct.data.source.local.StoreHelper
import cc.metapro.openct.data.university.UniversityFactory
import cc.metapro.openct.utils.ActivityUtils
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.base.BaseDialog
import cc.metapro.openct.utils.base.LoginPresenter
import cc.metapro.openct.utils.base.MyObserver
import com.rengwuxian.materialedittext.MaterialEditText
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CaptchaDialog : BaseDialog() {
    @BindView(R.id.captcha_image)
    internal var mTextView: TextView? = null
    @BindView(R.id.captcha_edit_text)
    internal var mEditText: MaterialEditText? = null

    @OnClick(R.id.captcha_image)
    fun loadCaptcha() {
        val observable = Observable.create(ObservableOnSubscribe<String> { e ->
            UniversityFactory.getOneMoreCAPTCHA()
            e.onNext("")
        })

        val observer = object : MyObserver<String>(TAG) {
            override fun onNext(s: String) {
                setCaptchaImg()
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                Toast.makeText(context, getString(R.string.fetch_captcha_fail) + e.message, Toast.LENGTH_LONG).show()
            }
        }

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    @OnEditorAction(R.id.captcha_edit_text)
    fun onEnter(i: Int, keyEvent: KeyEvent?): Boolean {
        if (i == EditorInfo.IME_ACTION_GO || keyEvent != null && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
            val code = mEditText!!.text.toString()
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(activity, R.string.enter_captcha, Toast.LENGTH_SHORT).show()
            } else {
                mLoginPresenter!!.loadUserCenter(fragmentManager, code)
                dismiss()
            }
            return true
        }
        return false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(activity).inflate(R.layout.diaolg_captcha, null)
        ButterKnife.bind(this, view)
        setCaptchaImg()
        val builder = ActivityUtils.getAlertBuilder(activity, R.string.captcha)
        val dialog = ActivityUtils.addViewToAlertDialog(builder, view)
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val code = mEditText!!.text.toString()
                if (TextUtils.isEmpty(code)) {
                    Toast.makeText(activity, R.string.enter_captcha, Toast.LENGTH_SHORT).show()
                } else {
                    mLoginPresenter!!.loadUserCenter(fragmentManager, code)
                    dismiss()
                }
            }
        }

        return dialog
    }

    private fun setCaptchaImg() {
        val drawable = BitmapDrawable.createFromPath(Constants.CAPTCHA_FILE)
        if (drawable != null) {
            mTextView!!.background = drawable
            mTextView!!.text = ""
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        mTextView!!.setText(R.string.press_to_get_captcha)
        mTextView!!.background = null
        mEditText!!.setText("")
        StoreHelper.delFile(Constants.CAPTCHA_FILE!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        mLoginPresenter = null
    }

    companion object {

        private val TAG = CaptchaDialog::class.java.simpleName
        private var mLoginPresenter: LoginPresenter? = null

        fun newInstance(presenter: LoginPresenter): CaptchaDialog {
            mLoginPresenter = presenter
            return CaptchaDialog()
        }
    }
}
