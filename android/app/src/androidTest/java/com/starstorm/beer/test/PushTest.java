package com.starstorm.beer.test;

import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import com.parse.FunctionCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.starstorm.beer.BeerApplication;
import com.starstorm.beer.service.AuthService;
import com.starstorm.beer.service.ParseAuthService;
import com.starstorm.beer.service.ParseSignalService;
import com.starstorm.beer.service.SignalService;

import java.util.ArrayList;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class PushTest extends ApplicationTestCase<BeerApplication> {

    private static final String TAG = PushTest.class.getSimpleName();
    private final AuthService authService = ParseAuthService.INSTANCE;
    private final SignalService signalService = ParseSignalService.INSTANCE;

    public PushTest() {
        super(BeerApplication.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        createApplication();
        final String username = ""; // Enter your test user's username
        final String password = ""; // Enter your test user's password
        ParseUser.logIn(username, password);
        Log.d(TAG, "setUp complete");
    }

    @MediumTest
    public void testSignalPush() throws Exception {
        ArrayList<String> recipients = new ArrayList<>();
        final String testRecipientUsername = ""; // Enter your test user's username
        recipients.add(testRecipientUsername);
        signalService.fireSignal(recipients, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                Log.d(TAG, "Sent");
                assertNull(e);
            }
        });
    }

    @Override
    public void tearDown() throws Exception {
        authService.logOut();
        Log.d(TAG, "tearDown complete");
    }
}