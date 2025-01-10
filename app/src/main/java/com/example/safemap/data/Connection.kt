package com.example.safemap.data

import com.google.firebase.firestore.FirebaseFirestore

object Connection
{
    private val instance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun provideFirestore(): FirebaseFirestore {
        return instance
    }

}