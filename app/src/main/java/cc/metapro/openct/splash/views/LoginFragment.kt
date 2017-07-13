package cc.metapro.openct.splash.views


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

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView

import butterknife.BindView
import butterknife.ButterKnife
import cc.metapro.openct.R
import cc.metapro.openct.splash.SplashContract

import cc.metapro.openct.utils.Constants.TYPE_CMS
import cc.metapro.openct.utils.Constants.TYPE_LIB

class LoginFragment : Fragment(), SplashContract.LoginView {

    @BindView(R.id.img)
    internal var mImageView: ImageView? = null
    @BindView(R.id.username)
    internal var mUsername: EditText? = null
    @BindView(R.id.password)
    internal var mPassword: EditText? = null

    private var mPresenter: SplashContract.Presenter? = null
    private var mType = TYPE_CMS
    private var showed = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_userpass, container, false)
        ButterKnife.bind(this, view)
        mType = arguments.getInt(KEY_TYPE)
        if (mType == TYPE_LIB) {
            mImageView!!.setImageResource(R.drawable.ic_lib_header)
        } else {
            mImageView!!.setImageResource(R.drawable.ic_cms_header)
        }
        return view
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser) {
            showed = true
        } else if (showed) {
            val username = mUsername!!.text.toString()
            val password = mPassword!!.text.toString()
            if (mType == TYPE_CMS) {
                mPresenter!!.storeCMSUserPass(username, password)
            } else if (mType == TYPE_LIB) {
                mPresenter!!.storeLibUserPass(username, password)
            }
        }
        super.setUserVisibleHint(isVisibleToUser)
    }

    override fun setPresenter(p: SplashContract.Presenter) {
        mPresenter = p
    }

    companion object {
        private val KEY_TYPE = "type"

        fun getInstance(type: Int): LoginFragment {
            val bundle = Bundle()
            bundle.putInt(KEY_TYPE, type)

            val fragment = LoginFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
