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

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Handles connection to Firebase
 */
class DatabaseManager {
    private val TAG = DatabaseManager::class.java.simpleName
    private val DEFAULT_NAME = "default"

    private var currentDeviceInfo: DeviceInfo? = null
    private var currentEvent = "default"
    private var currentRoom = "default"

    private val database: FirebaseDatabase
        get() = FirebaseDatabase.getInstance()

    init {
        database.setPersistenceEnabled(true)
        getFirebaseEvent()
    }


    private fun getFirebaseEvent() {
        val deviceData = database.reference.child("devices").child("default")
        deviceData.keepSynced(true)
        Log.d(TAG, "Try to get current event")

        deviceData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "getCurrentEvent:success")

                currentDeviceInfo = dataSnapshot.getValue(DeviceInfo::class.java)

                currentEvent = currentDeviceInfo?.current_event ?: "default"
                currentRoom = currentDeviceInfo?.current_room ?: "default"

                if (currentEvent == "none") {
                    currentEvent = "default"
                }

                if (currentRoom == "none") {
                    currentRoom = "default"
                }

                Log.d(TAG, "current event is: $currentEvent")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "DeviceDataListener:failure")
                currentEvent = "default"
            }
        })
    }

    fun addButtonPress(button: Int) {
        database.reference
                .child("data")
                .child(currentEvent)
                .push()
                .setValue(ButtonPress("default", currentRoom, button))
    }
}
