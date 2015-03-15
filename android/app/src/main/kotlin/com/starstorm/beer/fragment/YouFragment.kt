package com.starstorm.beer.fragment

import android.app.AlertDialog
import android.app.Fragment
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView

import com.crashlytics.android.Crashlytics
import com.dd.CircularProgressButton
import com.novoda.notils.caster.Views
import com.parse.FunctionCallback
import com.parse.ParseException
import com.parse.ParseFacebookUtils
import com.parse.ParseUser
import com.parse.SaveCallback
import com.starstorm.beer.R
import com.starstorm.beer.activity.LoginActivity
import com.starstorm.beer.service.ParseAuthService
import com.starstorm.beer.service.ParseUserService
import com.starstorm.beer.util.FacebookHelper
import com.starstorm.beer.util.Toaster

import java.util.Arrays

public class YouFragment : Fragment() {
    private val authService = ParseAuthService
    private val userService = ParseUserService

    private var usernameText: TextView? = null
    private var emailField: EditText? = null
    private var linkFacebookButton: CircularProgressButton? = null
    private var facebookLoginNote: TextView? = null
    private var saveUserButton: CircularProgressButton? = null

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle item selection
        when (item.getItemId()) {
            R.id.action_change_username -> {
                showUsernameChanger()
                return true
            }
            R.id.action_logout -> {
                logOut()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showUsernameChanger() {
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
                    override fun done(responseString: String?, e: ParseException?) {
                        progressDialog.dismiss()
                        if (e == null) {
                            ParseUser.getCurrentUser().setUsername(newUsername)
                            showLoggedInDetails()
                            FacebookHelper.getFacebookIdInBackground()
                            dialog.dismiss()
                        } else {
                            Log.w(TAG, e.getMessage())
                            if (responseString != null) {
                                Log.w(TAG, responseString)
                                if (responseString == "username_taken") {
                                    Toaster.showShort(getActivity(), "That username is already taken")
                                }
                            } else {
                                Crashlytics.logException(e)
                            }
                        }
                    }
                })
            }
        })
        alert.setNegativeButton("Cancel", null)
        alert.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.you, menu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_you, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameText = Views.findById<TextView>(view, R.id.username_text)
        emailField = Views.findById<EditText>(view, R.id.email_field)
        linkFacebookButton = Views.findById<CircularProgressButton>(view, R.id.link_facebook_button)
        facebookLoginNote = Views.findById<TextView>(view, R.id.facebook_login_note)
        saveUserButton = Views.findById<CircularProgressButton>(view, R.id.save_user_button)

        linkFacebookButton!!.setIndeterminateProgressMode(true)
        saveUserButton!!.setIndeterminateProgressMode(true)

        showLoggedInDetails()
    }

    private fun logOut() {
        try {
            authService.logOut()
            Toaster.showShort(getActivity(), "Logged out")
            val intent = Intent(getActivity(), javaClass<LoginActivity>())
            getActivity().startActivity(intent)
            getActivity().finish()
        } catch (e: Exception) {
            Toaster.showShort(getActivity(), "Logout failed: " + e.getMessage())
            Crashlytics.logException(e)
        }

    }

    private fun showLoggedInDetails() {
        val currentUser = ParseUser.getCurrentUser()
        usernameText!!.setText(currentUser.getUsername())
        emailField!!.setText(currentUser.getEmail())

        saveUserButton!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                saveUser()
            }
        })

        val isFacebookLinked = ParseFacebookUtils.isLinked(currentUser)

        if (isFacebookLinked) {
            facebookLoginNote!!.setVisibility(View.GONE)
            linkFacebookButton!!.setIdleText("Linked to Facebook")
            linkFacebookButton!!.setText("Linked to Facebook")
            linkFacebookButton!!.setOnClickListener(null)
        } else {
            facebookLoginNote!!.setVisibility(View.VISIBLE)
            linkFacebookButton!!.setIdleText("Link to Facebook")

            linkFacebookButton!!.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    linkFacebookButton!!.setProgress(1)
                    val permissions = Arrays.asList<String>("email")
                    ParseFacebookUtils.link(ParseUser.getCurrentUser(), permissions, getActivity(), object : SaveCallback {
                        override fun done(e: ParseException?) {
                            if (e == null) {
                                linkFacebookButton!!.setIdleText("Linked to Facebook")
                                linkFacebookButton!!.setText("Linked to Facebook")
                                FacebookHelper.getFacebookIdInBackground()
                            } else {
                                Log.e(TAG, e.getMessage())
                                Crashlytics.logException(e)
                            }
                            linkFacebookButton!!.setProgress(0)
                        }
                    })
                }
            })
        }
    }

    private fun saveUser() {
        saveUserButton!!.setProgress(1)
        ParseUser.getCurrentUser().setEmail(emailField!!.getText().toString())

        ParseUser.getCurrentUser().saveInBackground(object : SaveCallback {
            override fun done(e: ParseException?) {
                if (e == null) {
                    saveUserButton!!.setProgress(0)
                    Toaster.showShort(getActivity(), "Your settings have been updated")
                } else {
                    Log.e(TAG, e.getMessage())
                    Crashlytics.logException(e)
                    Toaster.showShort(getActivity(), "Save error: " + e.getCode())
                    saveUserButton!!.setProgress(-1)
                }
            }
        })
    }

    class object {

        private val TAG = javaClass<YouFragment>().getSimpleName()

        public fun newInstance(): YouFragment {
            return YouFragment()
        }
    }
}
