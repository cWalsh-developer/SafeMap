package com.example.safemap.View

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.safemap.Model.Result
import com.example.safemap.viewmodel.AuthoriseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authoriseViewModel: AuthoriseViewModel,
    onSignInSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
) {
    var textPressAction by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    val result by authoriseViewModel.authorisationResult.observeAsState()
    var password by remember {
        mutableStateOf("")
    }


    BackHandler {
        //Do Nothing
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login",
            modifier = Modifier.padding(bottom = 16.dp),
            color = Color(0xff26662a),
            style = MaterialTheme.typography.headlineLarge
        )

        OutlinedTextField(
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xff26662a),
                focusedLabelColor = Color(0xff26662a),
            ),
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        OutlinedTextField(
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xff26662a),
                focusedLabelColor = Color(0xff26662a),
            ),
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            visualTransformation = PasswordVisualTransformation()
        )
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xff26662a),
                contentColor = Color.White
            ),
            onClick = {
                authoriseViewModel.signIn(email, password)
                textPressAction = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Don't have an account? Sign up.",
            modifier = Modifier
                .imePadding()
                .clickable { onNavigateToSignUp() }
        )
    }
    when (result) {
        is Result.Success -> {
            onSignInSuccess()
        }

        is Result.Error -> {
            Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
        }

        is Result.Loading -> {
            CircularProgressIndicator(color = Color(0xff26662a))
        }
        is Result.LoggedOut -> {
        }
        null -> {
            if (textPressAction) {
                Toast.makeText(context, "No Data Found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
