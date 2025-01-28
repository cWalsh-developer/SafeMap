package com.example.safemap.Model

import android.content.ContentValues.TAG
import android.util.Log
import com.example.safemap.Model.Result.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private var userData: User = User()
    private var addressData: UserAddresses = UserAddresses()

    suspend fun signUp(email: String, password: String, firstName: String, lastName: String, telephone: String,
                       addressLine1: String, addressLine2: String,
                       townCity: String, county: String, country: String, postcode: String): Result<Boolean> =
        try
        {
            auth.createUserWithEmailAndPassword(email, password).await()
            val user = User(firstName, lastName, email, telephone)
            val address = UserAddresses(addressLine1, addressLine2, townCity, county, country, postcode)
            saveUserToFirestore(user)
            saveAddressToFirestore(address)
            Success(true)
        }catch (e: Exception)
        {
            Error(e)
        }

    suspend fun signIn(email: String, password: String): Result<Boolean> =
        try
        {
            auth.signInWithEmailAndPassword(email, password).await()
            Success(true)
        }catch (e: Exception)
        {
            Error(e)
        }

    private suspend fun saveUserToFirestore(user: User)
    {
        firestore.collection("users").document(auth.currentUser!!.uid).set(user).await()
    }

    private suspend fun saveAddressToFirestore(address: UserAddresses)
    {
        try {
            firestore.collection("users").document(auth.currentUser!!.uid).collection("addresses").add(address).await()
        }catch (Exception: Exception)
        {
            Log.d("Error", Exception.toString())
        }
    }

    suspend fun loadUser(): User
    {
        if(auth.currentUser !=null)
        {
            userData = firestore.collection("users").document(auth.currentUser!!.uid).get().await().toObject(User::class.java)?: User()
        }
        return userData
    }

    suspend fun loadAddress(): UserAddresses
    {
        if(auth.currentUser !=null)
        {
            firestore.collection("users").document(auth.currentUser!!.uid)
                .collection("addresses").get().addOnSuccessListener { result ->
                    for (document in result)
                    {
                        addressData = document.toObject(UserAddresses::class.java)
                    }
                }
        }
        return addressData
    }

    }
