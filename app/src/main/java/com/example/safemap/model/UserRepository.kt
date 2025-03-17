package com.example.safemap.model


import android.util.Log
import com.example.safemap.model.Result.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserRepository(private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private var userData: User = User()

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

    suspend fun signIn(email: String, password: String): Result<Boolean> =
        try
        {
            auth.signInWithEmailAndPassword(email, password).await()
            Success(true)
        }catch (e: Exception)
        {
            Error(e)
        }

    suspend fun loadUser(): User
    {
        if(auth.currentUser !=null)
        {
            userData = firestore.collection("users").document(auth.currentUser!!.uid).get().await().toObject(User::class.java)?: User()
        }
        return userData
    }

    suspend fun updateProfile(user: User, address: UserAddresses, password: String?): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("No authenticated user found")


            // Check if email needs to be changed
            if (currentUser.email != user.email && !password.isNullOrEmpty()) {
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
                currentUser.reload().await()
                try {
                    currentUser.reauthenticate(credential).await() // Re-authenticate with password
                    currentUser.verifyBeforeUpdateEmail(user.email).await()  // Send verification email
                } catch (e: Exception) {
                    throw Exception("Failed to send email verification: ${e.message}")
                }
            }

            //Always update Firestore, even if email isn't changing
            firestore.collection("users").document(currentUser.uid).set(user).await()
            firestore.collection("users").document(currentUser.uid)
                .collection("addresses").document(getAddressDocumentId(currentUser.uid)!!).set(address).await()

            Success(true)
        } catch (e: Exception) {
            Log.d("Error", e.toString())
            Error(e)
        }
    }




    private suspend fun getAddressDocumentId(userId: String): String? {
        val addressID = firestore.collection("users").document(userId).collection("addresses").get().await()
        return addressID.documents[0].id
    }

    suspend fun loadAddress(): UserAddresses? {
        if (auth.currentUser != null) {
            return suspendCoroutine { continuation ->
                firestore.collection("users").document(auth.currentUser!!.uid)
                    .collection("addresses")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val address = document.toObject(UserAddresses::class.java)
                            continuation.resume(address)
                            return@addOnSuccessListener
                        }
                        continuation.resume(null)
                    }
                    .addOnFailureListener { exception ->
                        println("Failed to load address data: ${exception.message}")
                        continuation.resume(null)
                    }
            }
        }
        return null
    }


}
