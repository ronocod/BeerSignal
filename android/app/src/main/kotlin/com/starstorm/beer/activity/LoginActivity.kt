package com.starstorm.beer.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.view.View
import android.widget.ProgressBar

import com.novoda.notils.caster.Views
import com.parse.ParseFacebookUtils
import com.parse.ParseUser
import com.starstorm.beer.R
import com.starstorm.beer.fragment.LoginFragment

public class LoginActivity : ActionBarActivity() {

    private var loginFragment: LoginFragment? = null
    private var loginProgress: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        loginProgress = Views.findById<ProgressBar>(this, R.id.login_progress)

        if (savedInstanceState == null) {
            loginFragment = LoginFragment.newInstance()
            getFragmentManager().beginTransaction().add(R.id.container, loginFragment).hide(loginFragment).commit()
        }
    }

    override fun onResume() {
        super.onResume()

        if (ParseUser.getCurrentUser() != null) {
            // logged in, go to main page
            val intent = Intent(this, javaClass<MainActivity>())
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
            return
        }

        // hide progress indicator
        loginProgress!!.setVisibility(View.GONE)

        // show login fragment
        getFragmentManager().beginTransaction().show(loginFragment).commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data)
    }
}
