package com.starstorm.beer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.novoda.notils.caster.Views;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.starstorm.beer.R;
import com.starstorm.beer.fragment.LoginFragment;

public class LoginActivity extends ActionBarActivity {

    private LoginFragment loginFragment;
    private ProgressBar loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        loginProgress = Views.findById(this, R.id.login_progress);

        if (savedInstanceState == null) {
            loginFragment = LoginFragment.newInstance();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, loginFragment)
                    .hide(loginFragment)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ParseUser.getCurrentUser() != null) {
            // logged in, go to main page
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        // hide progress indicator
        loginProgress.setVisibility(View.GONE);

        // show login fragment
        getFragmentManager().beginTransaction()
                .show(loginFragment)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }
}
