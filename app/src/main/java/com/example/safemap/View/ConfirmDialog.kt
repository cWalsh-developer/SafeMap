package com.example.safemap.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.safemap.model.User
import com.example.safemap.model.UserAddresses
import com.example.safemap.viewmodel.AuthoriseViewModel

@Composable
fun ConfirmDialog(onDismiss: () -> Unit,
                  userInfo : User,
                  addressInfo : UserAddresses,
                  authorisationModel: AuthoriseViewModel,
                  onConfirm: () -> Unit,)
{
    var passwordValue by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss,
        content ={
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Confirm Password",
                        color = Color(0xff26662a),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Follow the instructions in the email sent to ${userInfo.email} to verify your new email address",
                        color = Color.Black, fontSize = 12.sp)
                    Text(text = "Enter your password to confirm email change", color = Color.Black,
                        fontSize = 12.sp)

                    OutlinedTextField(value = passwordValue, onValueChange = {passwordValue = it},
                        label = { Text("Password", color = Color(0xff26662a)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xff26662a),
                            focusedLabelColor = Color(0xff26662a),
                            unfocusedTextColor = Color.Black,
                            focusedTextColor = Color.Black,
                        ))
                    Button(onClick = {
                        onConfirm(); authorisationModel.updateProfile(userInfo, addressInfo, passwordValue)},
                        colors = ButtonDefaults.buttonColors(Color(0xff26662a)))
                    {
                        Text(text = "Confirm Password", color = Color.White)
                    }
                }
            }
        })
}