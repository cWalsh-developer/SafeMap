package com.example.safemap.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.safemap.R
import com.example.safemap.viewmodel.AuthoriseViewModel


@Composable
fun AccountView(
    authorisationModel: AuthoriseViewModel,
    onNavigateToMedicalInfo: () -> Unit
) {
    val pad = 40.dp
    val rowPad = 70.dp
    val userData = authorisationModel.loadUser()
    val addressData = authorisationModel.loadAddress()

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = rowPad),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column {

            Text(
                text = "First Name:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            Text(
                text = "Last Name:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            Text(
                text = "Email:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            Text(
                text = "Telephone:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            Text(
                text = "Address Line 1:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            if (addressData.addressLine2 != "") {
                Text(
                    text = "Address Line 2:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = pad)
                )
            }
            Text(
                text = "Town/City:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            Text(
                text = "County:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            Text(
                text = "Country:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            Text(
                text = "Post Code:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
        }
        Column {
            Text(
                text = userData.firstName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            Text(
                text = userData.lastName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            Text(
                text = userData.email,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            Text(
                text = userData.telephone,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            Text(
                text = addressData.addressLine1,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            if (addressData.addressLine2 != "") {
                Text(
                    text = addressData.addressLine2,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = pad)
                )
            }
            Text(
                text = addressData.townCity,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            Text(
                text = addressData.county,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            Text(
                text = addressData.country,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
            Text(
                text = addressData.postCode,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = pad)
            )
        }
        Column{
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "fNameEdit",
                modifier = Modifier.wrapContentSize().padding(bottom = pad)
            )
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "lNameEdit",
                modifier = Modifier.wrapContentSize().padding(bottom = pad)
            )
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "emailEdit",
                modifier = Modifier.wrapContentSize().padding(bottom = pad)
            )
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "telephoneEdit",
                modifier = Modifier.wrapContentSize().padding(bottom = pad)
            )
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "addressLine1Edit",
                modifier = Modifier.wrapContentSize().padding(bottom = pad)
            )
            if (addressData.addressLine2 != "") {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = "addressLine2Edit",
                    modifier = Modifier.wrapContentSize().padding(bottom = pad)
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "townCityEdit",
                modifier = Modifier.wrapContentSize().padding(bottom = pad)
            )
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "countyEdit",
                modifier = Modifier.wrapContentSize().padding(bottom = pad)
            )
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "countryEdit",
                modifier = Modifier.wrapContentSize().padding(bottom = pad)
            )
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "postCodeEdit",
                modifier = Modifier.wrapContentSize().padding(bottom = pad)
            )
        }
    }
}