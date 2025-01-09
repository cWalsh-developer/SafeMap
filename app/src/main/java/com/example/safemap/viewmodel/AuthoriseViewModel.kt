package com.example.safemap.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safemap.data.Injection
import com.example.safemap.data.Result
import com.example.safemap.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AuthoriseViewModel : ViewModel() {
    private val userRepository:UserRepository

    init {
    userRepository = UserRepository(FirebaseAuth.getInstance(),
        Injection.provideFirestore())
    }

    private val _authorisationResultHolder = MutableLiveData<Result<Boolean>>()
    val authorisationResult: LiveData<Result<Boolean>>  get() = _authorisationResultHolder

    fun signUp(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _authorisationResultHolder.value = userRepository.signUp(email, password, firstName, lastName)
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authorisationResultHolder.value = userRepository.signIn(email, password)
        }

    }


}