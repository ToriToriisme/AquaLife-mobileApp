package com.example.aqualife.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aqualife.data.local.dao.UserDao
import com.example.aqualife.data.local.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = firebaseAuth.currentUser
    )

    val userProfile: StateFlow<UserEntity?> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                userDao.getUser(user.uid)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    // Send email verification
                    user.sendEmailVerification().await()
                    
                    // Save user to local database
                    userDao.insertUser(
                        UserEntity(
                            uid = user.uid,
                            email = user.email ?: "",
                            displayName = displayName,
                            isEmailVerified = false
                        )
                    )
                }
            } catch (e: Exception) {
                _authError.value = e.message ?: "Registration failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    // Check if email is verified
                    if (!user.isEmailVerified) {
                        // Send verification email again
                        user.sendEmailVerification().await()
                        _authError.value = "Please verify your email. Verification email sent."
                        firebaseAuth.signOut()
                        return@launch
                    }
                    
                    // Update user in local database
                    userDao.insertUser(
                        UserEntity(
                            uid = user.uid,
                            email = user.email ?: "",
                            displayName = user.displayName ?: "",
                            isEmailVerified = user.isEmailVerified,
                            lastLogin = System.currentTimeMillis()
                        )
                    )
                }
            } catch (e: Exception) {
                _authError.value = e.message ?: "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            firebaseAuth.signOut()
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            val user = firebaseAuth.currentUser
            if (user != null && !user.isEmailVerified) {
                try {
                    user.sendEmailVerification().await()
                } catch (e: Exception) {
                    _authError.value = e.message
                }
            }
        }
    }
}

