package com.example.safemap.data

import com.example.safemap.data.Result.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
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
        firestore.collection("addresses").add(address).await()
    }
}
