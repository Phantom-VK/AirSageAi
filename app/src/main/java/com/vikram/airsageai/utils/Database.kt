package com.vikram.airsageai.utils

import com.google.firebase.Firebase
import com.google.firebase.database.database

class Database {

    private val database = Firebase.database
    private val myRef = database.getReference("message")

    fun sendMessage(message: String) {
        myRef.setValue(message)
    }
}