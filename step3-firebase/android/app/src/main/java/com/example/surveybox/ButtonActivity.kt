/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.surveybox

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.android.things.pio.Gpio
import com.google.firebase.FirebaseApp

/**
 * This is the activity that manages button presses and connectivity to Firebase
 */
class ButtonActivity : Activity() {
    private val TAG = ButtonActivity::class.java.simpleName

    private lateinit var databaseManager: DatabaseManager

    private lateinit var buttons: MutableList<Button>
    private lateinit var statusLed: Gpio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Starting ButtonActivity")
        Log.i(TAG, "Registering gpio drivers")
        initializeGPIO()
        initializeFirebase()
    }

    private fun initializeGPIO() {
        Log.i(TAG, "Configuring GPIO pins")

        // Configure the GPIO Pins for the leds
        Log.i(TAG, "Configuring LED pins")
        statusLed = RainbowHat.openLedRed()

        // Configure the buttons to emit key events on GPIO state changes
        buttons = mutableListOf(RainbowHat.openButtonA(),
                RainbowHat.openButtonB(),
                RainbowHat.openButtonC())
        for ((i, button) in buttons.withIndex()) {
            Log.i(TAG, "Registering button ${Integer.toString(i)}")

            // Create GPIO connection.
            button.setDebounceDelay(10)
            button.setOnButtonEventListener { _, pressed ->
                run {
                    if (!pressed) {
                        databaseManager.addButtonPress(i)
                        blinkStatusLed()
                    }
                }
            }
        }
    }

    private fun initializeFirebase() {
        FirebaseApp.initializeApp(this)
        databaseManager = DatabaseManager()
    }

    private fun blinkStatusLed() {
        statusLed.value = true
        Handler().postDelayed({
            statusLed.value = false
        }, 400)
    }

    override fun onDestroy() {
        super.onDestroy()
        for (button in buttons) {
            button.close()
            buttons.remove(button)
        }
        statusLed.close()
    }
}
