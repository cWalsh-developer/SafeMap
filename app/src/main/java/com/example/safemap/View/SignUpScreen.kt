package com.example.safemap.View



import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.safemap.viewmodel.AuthoriseViewModel
import com.example.safemap.Model.Result

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    @Composable
    fun SignUpScreen(
    authoriseViewModel: AuthoriseViewModel,
    onNavigateToSignIn: () -> Unit,
    )
    {
        //(User)State variables
        var isError by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var telephone by remember { mutableStateOf("") }

        //(Address)State variables
        var addressLine1 by remember { mutableStateOf("") }
        var addressLine2 by remember { mutableStateOf("") }
        var townCity by remember { mutableStateOf("") }
        var county by remember { mutableStateOf("") }
        var country by remember { mutableStateOf("") }
        var postcode by remember { mutableStateOf("") }


        Column(
            modifier = Modifier
                .fillMaxSize().verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = "Sign Up",
                modifier = Modifier.padding(bottom = 16.dp),
                color = Color(0xff26662a),
                style = MaterialTheme.typography.headlineLarge)
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xff26662a),
                    focusedLabelColor = Color(0xff26662a), cursorColor = Color(0xff26662a)),
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xff26662a),
                    focusedLabelColor = Color(0xff26662a), cursorColor = Color(0xff26662a)),
                value = password,
                onValueChange = { password = it },
                supportingText = {Text(error, color = Color.Red)},
                trailingIcon = {if(isError) Icon(Icons.Filled.Info, "error", tint = Color.Red) },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                visualTransformation = PasswordVisualTransformation()
            )
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xff26662a),
                    focusedLabelColor = Color(0xff26662a), cursorColor = Color(0xff26662a)),
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xff26662a),
                    focusedLabelColor = Color(0xff26662a), cursorColor = Color(0xff26662a)),
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xff26662a),
                    focusedLabelColor = Color(0xff26662a), cursorColor = Color(0xff26662a)),
                value = telephone,
                onValueChange = { telephone = it },
                label = { Text("Mobile Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xff26662a),
                    focusedLabelColor = Color(0xff26662a), cursorColor = Color(0xff26662a)),
                value = addressLine1,
                onValueChange = { addressLine1 = it },
                label = { Text("Address Line One") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xff26662a),
                    focusedLabelColor = Color(0xff26662a), cursorColor = Color(0xff26662a)),
                value = addressLine2,
                onValueChange = { addressLine2 = it },
                label = { Text("Address Line Two") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xff26662a),
                    focusedLabelColor = Color(0xff26662a), cursorColor = Color(0xff26662a)),
                value = townCity,
                onValueChange = { townCity= it },
                label = { Text("Town/City") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xff26662a),
                    focusedLabelColor = Color(0xff26662a), cursorColor = Color(0xff26662a)),
                value = county,
                onValueChange = { county = it },
                label = { Text("county") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xff26662a),
                    focusedLabelColor = Color(0xff26662a), cursorColor = Color(0xff26662a)),
                value = country,
                onValueChange = { country = it },
                label = { Text("country") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xff26662a),
                    focusedLabelColor = Color(0xff26662a), cursorColor = Color(0xff26662a)),
                value = postcode,
                onValueChange = { postcode = it },
                label = { Text("Postcode/Zip") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            Button(
                colors = ButtonColors(
                    containerColor = Color(0xff26662a),
                    contentColor = Color.White,
                    disabledContainerColor = Color.DarkGray,
                    disabledContentColor = Color.LightGray
                ),
                onClick = {
                    if(password.length < 6){
                        error = "Password must be at least 6 characters long"
                        isError = true
                    }
                    else
                    {
                        authoriseViewModel.signUp(email, password, firstName, lastName, telephone,
                            addressLine1, addressLine2, townCity, county, country, postcode)
                        email = ""
                        password = ""
                        firstName = ""
                        lastName = ""
                        telephone = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Sign Up")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Already have an account? Sign in.",
                modifier = Modifier.imePadding().clickable {
                    authoriseViewModel.signOut(Result.LoggedOut)
                    onNavigateToSignIn()
                })
        }
    }