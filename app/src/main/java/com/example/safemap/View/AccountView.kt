package com.example.safemap.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.safemap.model.User
import com.example.safemap.model.UserAddresses
import com.example.safemap.R
import com.example.safemap.viewmodel.AuthoriseViewModel

@Composable
fun AccountView(
    authorisationModel: AuthoriseViewModel,
    onNavigateToMedicalInfo: () -> Unit
) {
    // Trigger data loading
    LaunchedEffect(Unit) {
        authorisationModel.loadUserData()
    }

    // Observe user and address data
    val currentUserData = authorisationModel.userData.collectAsState().value
    val currentAddressData = authorisationModel.addressData.collectAsState().value

    if (currentUserData == null || currentAddressData == null) {
        Text("Loading...", modifier = Modifier.fillMaxSize())
    } else {
        AccountContent(currentUserData, currentAddressData, onNavigateToMedicalInfo)
    }
}

@Composable
fun AccountContent(userData: User, addressData: UserAddresses, onNavigateToMedicalInfo: () -> Unit) {
    val pad = 40.dp
    val scrollState = rememberScrollState()

    // Labels and corresponding values for user data
    val userFields = listOf(
        "First Name" to userData.firstName,
        "Last Name" to userData.lastName,
        "Email" to userData.email,
        "Telephone" to userData.telephone
    )

    // Labels and corresponding values for address data
    val addressFields = listOf(
        "Address Line 1" to addressData.addressLine1,
        "Address Line 2" to addressData.addressLine2.takeIf { it.isNotEmpty() },
        "Town/City" to addressData.townCity,
        "County" to addressData.county,
        "Country" to addressData.country,
        "Post Code" to addressData.postCode
    ).filter { it.second != null } // Remove null values

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally)
    {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxHeight()
            ) {
                (userFields + addressFields).forEach { (label, _) ->
                    Text(
                        text = "$label:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = pad)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxHeight()
            ) {
                (userFields + addressFields).forEach { (_, value) ->
                    Text(
                        text = value?:"",  // Handle possible null values
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = pad)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxHeight()
            ) {
                repeat(userFields.size + addressFields.size) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Edit",
                        modifier = Modifier.wrapContentSize().padding(bottom = pad),
                        tint = Color.Black
                    )
                }
            }
        }
        Button(modifier = Modifier.wrapContentSize(), onClick = {
            onNavigateToMedicalInfo()
        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xff26662a))) {
            Text(text = "Medical Information", color = Color.White)
        }
    }
}
