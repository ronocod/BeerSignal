/*
 * Copyright 2012 Kevin Sawicki <kevinsawicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.starstorm.beer.util

import android.app.Activity
import android.text.TextUtils
import android.widget.Toast

import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT

// https://github.com/kevinsawicki/wishlist/blob/master/lib/src/main/java/com/github/kevinsawicki/wishlist/Toaster.java

/**
 * Helper to show {@link android.widget.Toast} notifications
 */
public class Toaster {
    class object {
        private fun show(activity: Activity?, message: String, duration: Int) {
            if (activity == null || TextUtils.isEmpty(message)) {
                return
            }
            val context = activity.getApplication()
            activity.runOnUiThread(Runnable {
                Toast.makeText(context, message, duration).show()
            })
        }

        /**
         * Show message in {@link android.widget.Toast} with {@link android.widget.Toast#LENGTH_LONG} duration
         *
         * @param activity
         * @param message
         */
        public fun showLong(activity: Activity, message: String) {
            show(activity, message, LENGTH_LONG)
        }

        /**
         * Show message in {@link android.widget.Toast} with {@link android.widget.Toast#LENGTH_SHORT} duration
         *
         * @param activity
         * @param message
         */
        public fun showShort(activity: Activity, message: String) {
            show(activity, message, LENGTH_SHORT)
        }
    }
}
