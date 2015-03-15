package com.starstorm.beer.fragment

import android.app.AlertDialog
import android.app.Fragment
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import com.crashlytics.android.Crashlytics
import com.dd.CircularProgressButton
import com.novoda.notils.caster.Views
import com.parse.FunctionCallback
import com.parse.LogInCallback
import com.parse.ParseException
import com.parse.ParseFacebookUtils
import com.parse.ParseInstallation
import com.parse.ParseUser
import com.parse.SignUpCallback
import com.starstorm.beer.R
import com.starstorm.beer.activity.MainActivity
import com.starstorm.beer.service.ParseUserService
import com.starstorm.beer.util.FacebookHelper
import com.starstorm.beer.util.Toaster

public class LoginFragment// Required empty public constructor
: Fragment() {
    private val userService = ParseUserService

    private var usernameField: EditText? = null
    private var passwordField: EditText? = null
    private var facebookLoginButton: CircularProgressButton? = null
    private var loginButton: CircularProgressButton? = null
    private var signupButton: CircularProgressButton? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameField = Views.findById<EditText>(view, R.id.username_field)
        passwordField = Views.findById<EditText>(view, R.id.password_field)
        facebookLoginButton = Views.findById<CircularProgressButton>(view, R.id.facebook_login_button)
        loginButton = Views.findById<CircularProgressButton>(view, R.id.login_button)
        signupButton = Views.findById<CircularProgressButton>(view, R.id.signup_button)

        signupButton!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                signUp()
            }
        })
        signupButton!!.setIndeterminateProgressMode(true)

        loginButton!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                logIn()
            }
        })
        loginButton!!.setIndeterminateProgressMode(true)

        facebookLoginButton!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                logInWithFacebook()
            }
        })
        facebookLoginButton!!.setIndeterminateProgressMode(true)

    }

    private fun logInWithFacebook() {
        facebookLoginButton!!.setProgress(1)
        ParseFacebookUtils.logIn(getActivity(), object : LogInCallback {
            override fun done(parseUser: ParseUser, e: ParseException?) {
                if (e == null) {
                    // Login successful
                    if (parseUser.isNew()) {
                        showUsernameChooser()

                    } else {
                        // Logging in existing user
                        FacebookHelper.getFacebookIdInBackground()
                        onLoginSuccess()
                    }
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    facebookLoginButton!!.setProgress(0)
                    val cause = e.getCause()
                    if (cause != null) {
                        Log.e(TAG, cause.getMessage())
                    }
                    if (cause == null || cause !is com.facebook.FacebookOperationCanceledException) {
                        Crashlytics.logException(e)
                        Toaster.showShort(getActivity(), "Login failed: " + e.getMessage())
                    }
                }
            }

            private fun showUsernameChooser() {
                // User has just signed up through Facebook, ask them for a username
                // to replace the default shitty long Base64 string that Parse gives
                val alert = AlertDialog.Builder(getActivity())

                alert.setTitle("Choose your username")

                // Set an EditText view to get user input
                val input = getActivity().getLayoutInflater().inflate(R.layout.dialog_choose_username, null)
                val inputField = input.findViewById(R.id.choose_username_field) as EditText
                alert.setView(input)

                alert.setPositiveButton("Ok", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, whichButton: Int) {

                        val progressDialog = ProgressDialog(getActivity())
                        progressDialog.setMessage("Checking username")
                        progressDialog.show()

                        val newUsername = inputField.getText().toString()

                        // Do something with value!

                        userService.changeUsername(newUsername, object : FunctionCallback<String> {
                            override fun done(responseString: String, e: ParseException?) {
                                if (e == null) {
                                    FacebookHelper.getFacebookIdInBackground()
                                    progressDialog.dismiss()
                                    dialog.dismiss()
                                    // Hooray! Let them use the app now.
                                    onLoginSuccess()
                                } else {
                                    if (responseString == "username_taken") {
                                        Toaster.showShort(getActivity(), "That username is already taken")
                                        progressDialog.dismiss()
                                        showUsernameChooser()
                                    } else {
                                        Crashlytics.logException(e)
                                    }
                                }
                            }
                        })
                    }
                })

                alert.setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, whichButton: Int) {
                        // Canceled.
                        ParseUser.getCurrentUser().deleteInBackground()
                        facebookLoginButton!!.setProgress(0)
                    }
                })

                alert.show()
            }

            public fun onLoginSuccess() {
                Toaster.showShort(getActivity(), "Logged in")
                // don't set facebookLoginButton to complete as it looks bad while transitioning to the activity
                finishLogin(ParseUser.getCurrentUser())
            }
        })
    }

    private fun signUp() {
        val me = ParseUser()
        me.setUsername(usernameField!!.getText().toString().toLowerCase())
        me.setPassword(passwordField!!.getText().toString())

        // other fields can be set just like with ParseObject
        //        user.put("phone", "650-555-0000");

        try {
            signupButton!!.setProgress(1)
            me.signUpInBackground(object : SignUpCallback {
                override fun done(e: ParseException?) {
                    if (e == null) {
                        // Hooray! Let them use the app now.
                        Toaster.showShort(getActivity(), "Signed up")
                        finishLogin(me)
                    } else {
                        // Sign up didn't succeed. Look at the ParseException
                        // to figure out what went wrong
                        Crashlytics.logException(e)
                        signupButton!!.setProgress(0)
                        Toaster.showShort(getActivity(), "Signup failed: " + e.getMessage())
                    }
                }
            })
        } catch (e: Exception) {
            Crashlytics.logException(e)
            signupButton!!.setProgress(0)
            Toaster.showShort(getActivity(), "Signup failed: " + e.getMessage())
        }

    }

    private fun logIn() {
        val username = usernameField!!.getText().toString().toLowerCase()
        val password = passwordField!!.getText().toString()
        try {
            loginButton!!.setProgress(1)
            ParseUser.logInInBackground(username, password, object : LogInCallback {
                override fun done(parseUser: ParseUser, e: ParseException?) {
                    // Hooray! Let them use the app now.
                    if (e == null) {
                        // Hooray! Let them use the app now.
                        Toaster.showShort(getActivity(), "Logged in")
                        finishLogin(parseUser)
                    } else {
                        // Sign up didn't succeed. Look at the ParseException
                        // to figure out what went wrong
                        Crashlytics.logException(e)
                        loginButton!!.setProgress(0)
                        Toaster.showShort(getActivity(), "Login failed: " + e.getMessage())
                    }
                }
            })
        } catch (e: Exception) {
            Crashlytics.logException(e)
            loginButton!!.setProgress(0)
            Toaster.showShort(getActivity(), "Login failed: " + e.getMessage())
        }

    }

    private fun finishLogin(parseUser: ParseUser) {
        ParseInstallation.getCurrentInstallation().put("currentUser", parseUser)
        ParseInstallation.getCurrentInstallation().saveInBackground()
        openMainActivity()
    }

    private fun openMainActivity() {
        val intent = Intent(getActivity(), javaClass<MainActivity>())
        getActivity().startActivity(intent)
        getActivity().finish()
    }

    class object {

        private val TAG = javaClass<LoginFragment>().getSimpleName()

        public fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}
