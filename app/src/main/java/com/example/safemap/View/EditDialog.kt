package com.example.safemap.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.example.safemap.model.User
import com.example.safemap.model.UserAddresses
import com.example.safemap.viewmodel.AuthoriseViewModel

@Composable
fun EditDialog(onDismiss: () -> Unit,
               userInfo : User,
               addressInfo : UserAddresses,
               onConfirm: () -> Unit, authorisationModel: AuthoriseViewModel
)
{

    val scrollState = rememberScrollState()
    val userFields = listOf(
        "First Name" to userInfo.firstName,
        "Last Name" to userInfo.lastName,
        "Email" to userInfo.email,
        "Telephone" to userInfo.telephone
    )

    val addressFields = listOf(
        "Address Line 1" to addressInfo.addressLine1,
        "Address Line 2" to addressInfo.addressLine2.takeIf { it.isNotEmpty() },
        "Town/City" to addressInfo.townCity,
        "County" to addressInfo.county,
        "Country" to addressInfo.country,
        "Post Code" to addressInfo.postCode
    ).filter { it.second != null }

    Dialog(onDismissRequest = onDismiss,
        content ={
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth().verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Edit Profile",
                        color = Color(0xff26662a),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    (userFields + addressFields).forEach { (label, value) ->
                        var newValue by remember { mutableStateOf(value) }
                        OutlinedTextField(
                            value = newValue!!,
                            onValueChange = { newValue = it },
                            label = { Text(label, color = Color(0xff26662a)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xff26662a),
                                focusedLabelColor = Color(0xff26662a),
                                unfocusedTextColor = Color.Black,
                                focusedTextColor = Color.Black,
                            )
                        )
                        userFields.forEach {
                            if (it.first == label) {
                                when (it.first) {
                                    "First Name" -> userInfo.firstName = newValue!!
                                    "Last Name" -> userInfo.lastName = newValue!!
                                    "Email" -> userInfo.email = newValue!!
                                    "Telephone" -> userInfo.telephone = newValue!!
                                }
                            }
                        }
                        addressFields.forEach {
                            if (it.first == label) {
                                when (it.first) {
                                    "Address Line 1" -> addressInfo.addressLine1 = newValue!!
                                    "Address Line 2" -> addressInfo.addressLine2 = newValue!!
                                    "Town/City" -> addressInfo.townCity = newValue!!
                                    "County" -> addressInfo.county = newValue!!
                                    "Country" -> addressInfo.country = newValue!!
                                    "Post Code" -> addressInfo.postCode = newValue!!
                                }
                            }
                        }
                    }
                    Button(onClick = {
                        onConfirm(); authorisationModel.updateProfile(userInfo, addressInfo)},
                        colors = ButtonDefaults.buttonColors(Color(0xff26662a)))
                    {
                        Text(text = "Save Changes", color = Color.White)
                    }
                }
            }
        })
}