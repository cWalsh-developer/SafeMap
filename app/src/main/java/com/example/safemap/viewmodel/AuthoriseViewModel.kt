package com.example.safemap.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemap.Model.Connection
import com.example.safemap.Model.Result
import com.example.safemap.Model.Result.Error
import com.example.safemap.Model.Result.Success
import com.example.safemap.Model.User
import com.example.safemap.Model.UserAddresses
import com.example.safemap.Model.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AuthoriseViewModel : ViewModel() {
    private val userRepository: UserRepository = UserRepository(
        FirebaseAuth.getInstance(),
        Connection.provideFirestore()
    )

    private var userData: User = User()
    private var addressData: UserAddresses = UserAddresses()

    private val _authorisationResultHolder = MutableLiveData<Result<Boolean>>()
    val authorisationResult: LiveData<Result<Boolean>> get() = _authorisationResultHolder

    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        telephone: String,
        addressLine1: String,
        addressLine2: String,
        townCity: String,
        county: String,
        country: String,
        postcode: String
    ) {
        viewModelScope.launch {
            _authorisationResultHolder.value = userRepository.signUp(
                email, password, firstName, lastName, telephone,
                addressLine1, addressLine2, townCity, county, country, postcode
            )
        }
    }

    fun signIn(email: String, password: String) {
        _authorisationResultHolder.value = Result.Loading
        viewModelScope.launch {
            try {
                val result = userRepository.signIn(email, password)
                _authorisationResultHolder.postValue(result)
            } catch (e: Exception) {
                _authorisationResultHolder.postValue(Error(e))
            }
        }

    }

    fun checkStatus(): Result<Boolean> =
        if (FirebaseAuth.getInstance().currentUser != null) {
            Success(true)
        } else {
            Error(Exception("User not logged in"))
        }

    fun signOut(result: Result.LoggedOut) {
        FirebaseAuth.getInstance().signOut()
        _authorisationResultHolder.value = result
    }

    fun loadUser(): User {
        viewModelScope.launch {
            userData = userRepository.loadUser()
        }
        return userData
    }

    fun loadAddress(): UserAddresses {
        viewModelScope.launch {
            addressData = userRepository.loadAddress()
        }
        return addressData

    }
}