package com.example.safemap.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.safemap.model.User
import com.example.safemap.model.UserAddresses
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
        AccountContent(currentUserData, currentAddressData, onNavigateToMedicalInfo,
            authorisationModel)
    }
}

@Composable
fun AccountContent(userData: User, addressData: UserAddresses, onNavigateToMedicalInfo: () -> Unit,
                   authorisationModel: AuthoriseViewModel)
{
    var showDialog by remember { mutableStateOf(false) }
    val pad = 20.dp
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

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
        , horizontalAlignment = Alignment.CenterHorizontally)
    {
        (userFields + addressFields).forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(pad)
                )
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(pad)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically)
        {
            Button(modifier = Modifier, onClick = {
                onNavigateToMedicalInfo()
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xff26662a))) {
                Text(text = "Medical Information", color = Color.White)
            }
            Button(modifier = Modifier, onClick = {
                showDialog = true
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xff26662a)))
            {
                Text(text = "Edit", color = Color.White)
                Icon(Icons.Default.Edit,
                    contentDescription = "Edit", tint = Color.White)
            }
        }
    }
    if (showDialog) {
        EditDialog(
            onDismiss = { showDialog = false },
            onConfirm = { showDialog = false },
            userInfo = userData,
            addressInfo = addressData,
            authorisationModel = authorisationModel)
    }
}
