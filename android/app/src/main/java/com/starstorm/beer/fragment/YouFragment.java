package com.starstorm.beer.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.dd.CircularProgressButton;
import com.novoda.notils.caster.Views;
import com.parse.FunctionCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.starstorm.beer.R;
import com.starstorm.beer.activity.LoginActivity;
import com.starstorm.beer.service.ParseAuthService;
import com.starstorm.beer.service.ParseUserService;
import com.starstorm.beer.util.FacebookHelper;
import com.starstorm.beer.util.Toaster;

import java.util.Arrays;
import java.util.List;

public class YouFragment extends Fragment {

    private static final String TAG = YouFragment.class.getSimpleName();
    private final ParseAuthService authService = ParseAuthService.INSTANCE;
    private final ParseUserService userService = ParseUserService.INSTANCE;

    private TextView usernameText;
    private EditText emailField;
    private CircularProgressButton linkFacebookButton;
    private TextView facebookLoginNote;
    private CircularProgressButton saveUserButton;

    public static YouFragment newInstance() {
        return new YouFragment();
    }

    public YouFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_change_username:
                showUsernameChanger();
            return true;
            case R.id.action_logout:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showUsernameChanger() {
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
                        progressDialog.dismiss();
                        if (e == null) {
                            ParseUser.getCurrentUser().setUsername(newUsername);
                            showLoggedInDetails();
                            FacebookHelper.getFacebookIdInBackground();
                            dialog.dismiss();
                        } else {
                            Log.w(TAG, e.getMessage());
                            if (responseString != null) {
                                Log.w(TAG, responseString);
                                if (responseString.equals("username_taken")) {
                                    Toaster.showShort(getActivity(), "That username is already taken");
                                }
                            } else {
                                Crashlytics.logException(e);
                            }
                        }
                    }
                });
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.you, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_you, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usernameText = Views.findById(view, R.id.username_text);
        emailField = Views.findById(view, R.id.email_field);
        linkFacebookButton = Views.findById(view, R.id.link_facebook_button);
        facebookLoginNote = Views.findById(view, R.id.facebook_login_note);
        saveUserButton = Views.findById(view, R.id.save_user_button);

        linkFacebookButton.setIndeterminateProgressMode(true);
        saveUserButton.setIndeterminateProgressMode(true);

        showLoggedInDetails();
    }

    private void logOut() {
        try {
            authService.logOut();
            Toaster.showShort(getActivity(), "Logged out");
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            getActivity().startActivity(intent);
            getActivity().finish();
        } catch (Exception e) {
            Toaster.showShort(getActivity(), "Logout failed: " + e.getMessage());
            Crashlytics.logException(e);
        }
    }

    private void showLoggedInDetails() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        usernameText.setText(currentUser.getUsername());
        emailField.setText(currentUser.getEmail());

        saveUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUser();
            }
        });

        boolean isFacebookLinked = ParseFacebookUtils.isLinked(currentUser);

        if (isFacebookLinked) {
            facebookLoginNote.setVisibility(View.GONE);
            linkFacebookButton.setIdleText("Linked to Facebook");
            linkFacebookButton.setText("Linked to Facebook");
            linkFacebookButton.setOnClickListener(null);
        } else {
            facebookLoginNote.setVisibility(View.VISIBLE);
            linkFacebookButton.setIdleText("Link to Facebook");

            linkFacebookButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    linkFacebookButton.setProgress(1);
                    final ParseUser currentUser = ParseUser.getCurrentUser();
                    List<String> permissions = Arrays.asList("email");
                    ParseFacebookUtils.link(currentUser, permissions, getActivity(), new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                linkFacebookButton.setIdleText("Linked to Facebook");
                                linkFacebookButton.setText("Linked to Facebook");
                                FacebookHelper.getFacebookIdInBackground();
                            } else {
                                Log.e(TAG, e.getMessage());
                                Crashlytics.logException(e);
                            }
                            linkFacebookButton.setProgress(0);
                        }
                    });
                }
            });
        }
    }

    private void saveUser() {
        saveUserButton.setProgress(1);
        ParseUser.getCurrentUser().setEmail(emailField.getText().toString());

        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    saveUserButton.setProgress(0);
                    Toaster.showShort(getActivity(), "Your settings have been updated");
                } else {
                    Log.e(TAG, e.getMessage());
                    Crashlytics.logException(e);
                    Toaster.showShort(getActivity(), "Save error: " + e.getCode());
                    saveUserButton.setProgress(-1);
                }
            }
        });
    }
}
