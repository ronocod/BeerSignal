package com.starstorm.beer.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.crashlytics.android.Crashlytics;
import com.dd.CircularProgressButton;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.starstorm.beer.R;
import com.starstorm.beer.activity.MainActivity;
import com.starstorm.beer.service.ParseUserService;
import com.starstorm.beer.service.UserService;
import com.starstorm.beer.util.FacebookHelper;
import com.starstorm.beer.util.Toaster;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoginFragment extends BaseFragment {

    private static final String TAG = LoginFragment.class.getSimpleName();
    private final UserService userService = ParseUserService.INSTANCE;

    @InjectView(R.id.username_field)
    EditText mUsernameField;
    @InjectView(R.id.password_field)
    EditText mPasswordField;
    @InjectView(R.id.facebook_login_button)
    CircularProgressButton mFacebookLoginButton;
    @InjectView(R.id.login_button)
    CircularProgressButton mLoginButton;
    @InjectView(R.id.signup_button)
    CircularProgressButton mSignupButton;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.inject(this, view);

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });
        mSignupButton.setIndeterminateProgressMode(true);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn();
            }
        });
        mLoginButton.setIndeterminateProgressMode(true);

        mFacebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logInWithFacebook();
            }
        });
        mFacebookLoginButton.setIndeterminateProgressMode(true);

    }

    private void logInWithFacebook() {
        mFacebookLoginButton.setProgress(1);
        ParseFacebookUtils.logIn(getActivity(), new LogInCallback() {
            @Override
            public void done(final ParseUser parseUser, ParseException e) {
                if (e == null) {
                    // Login successful
                    if (parseUser.isNew()) {
                        showUsernameChooser();

                    } else {
                        // Logging in existing user
                        FacebookHelper.getFacebookIdInBackground();
                        onLoginSuccess();
                    }
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    mFacebookLoginButton.setProgress(0);
                    if (e.getCause() != null) {
                        Log.e(TAG, e.getCause().getMessage());
                    }
                    if (e.getCause() == null || !(e.getCause() instanceof com.facebook.FacebookOperationCanceledException)) {
                        Crashlytics.logException(e);
                        Toaster.showShort(getActivity(), "Login failed: " + e.getMessage());
                    }
                }
            }

            private void showUsernameChooser() {
                // User has just signed up through Facebook, ask them for a username
                // to replace the default shitty long Base64 string that Parse gives
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                alert.setTitle("Choose your username");

                // Set an EditText view to get user input
                final View input = getActivity().getLayoutInflater().inflate(R.layout.dialog_choose_username, null);
                final EditText inputField = (EditText) input.findViewById(R.id.choose_username_field);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int whichButton) {

                        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage("Checking username");
                        progressDialog.show();

                        final String newUsername = inputField.getText().toString();

                        // Do something with value!

                        userService.changeUsername(newUsername, new FunctionCallback<String>() {
                            @Override
                            public void done(String responseString, ParseException e) {
                                if (e == null) {
                                    FacebookHelper.getFacebookIdInBackground();
                                    progressDialog.dismiss();
                                    dialog.dismiss();
                                    // Hooray! Let them use the app now.
                                    onLoginSuccess();
                                } else {
                                    if (responseString.equals("username_taken")) {
                                        Toaster.showShort(getActivity(), "That username is already taken");
                                        progressDialog.dismiss();
                                        showUsernameChooser();
                                    } else {
                                        Crashlytics.logException(e);
                                    }
                                }
                            }
                        });
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                        ParseUser.getCurrentUser().deleteInBackground();
                        mFacebookLoginButton.setProgress(0);
                    }
                });

                alert.show();
            }

            public void onLoginSuccess() {
                Toaster.showShort(getActivity(), "Logged in");
                // don't set mFacebookLoginButton to complete as it looks bad while transitioning to the activity
                finishLogin(ParseUser.getCurrentUser());
            }
        });
    }

    private void signUp() {
        final ParseUser me = new ParseUser();
        me.setUsername(mUsernameField.getText().toString().toLowerCase());
        me.setPassword(mPasswordField.getText().toString());

        // other fields can be set just like with ParseObject
//        user.put("phone", "650-555-0000");

        try {
            mSignupButton.setProgress(1);
            me.signUpInBackground(new SignUpCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        // Hooray! Let them use the app now.
                        Toaster.showShort(getActivity(), "Signed up");
                        finishLogin(me);
                    } else {
                        // Sign up didn't succeed. Look at the ParseException
                        // to figure out what went wrong
                        Crashlytics.logException(e);
                        mSignupButton.setProgress(0);
                        Toaster.showShort(getActivity(), "Signup failed: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Crashlytics.logException(e);
            mSignupButton.setProgress(0);
            Toaster.showShort(getActivity(), "Signup failed: " + e.getMessage());
        }
    }

    private void logIn() {
        final String username = mUsernameField.getText().toString().toLowerCase();
        final String password = mPasswordField.getText().toString();
        try {
            mLoginButton.setProgress(1);
            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    // Hooray! Let them use the app now.
                    if (e == null) {
                        // Hooray! Let them use the app now.
                        Toaster.showShort(getActivity(), "Logged in");
                        finishLogin(parseUser);
                    } else {
                        // Sign up didn't succeed. Look at the ParseException
                        // to figure out what went wrong
                        Crashlytics.logException(e);
                        mLoginButton.setProgress(0);
                        Toaster.showShort(getActivity(), "Login failed: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Crashlytics.logException(e);
            mLoginButton.setProgress(0);
            Toaster.showShort(getActivity(), "Login failed: " + e.getMessage());
        }
    }

    private void finishLogin(ParseUser parseUser) {
        ParseInstallation.getCurrentInstallation().put("currentUser", parseUser);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        openMainActivity();
    }

    private void openMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        getActivity().startActivity(intent);
        getActivity().finish();
    }
}
