package com.starstorm.beer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.starstorm.beer.R;
import com.starstorm.beer.fragment.LoginFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoginActivity extends BaseActivity {

    Bundle savedInstanceState;
    LoginFragment loginFragment;

    @InjectView(R.id.login_progress)
    ProgressBar loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.savedInstanceState = savedInstanceState;

        setContentView(R.layout.activity_login);

        ButterKnife.inject(this);

        if (this.savedInstanceState == null) {
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
