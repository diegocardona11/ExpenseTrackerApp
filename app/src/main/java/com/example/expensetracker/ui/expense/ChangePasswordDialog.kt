package com.example.expensetracker.ui.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.expensetracker.viewmodel.AuthViewModel

// Dialog that lets the user change their password
@Composable
fun ChangePasswordDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    // Stores what the user types
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Change Password") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Current password input
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                // New password input
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Confirm new password input
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Show error message if something goes wrong
                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Show success message if password was changed
                if (authViewModel.passwordChangeSuccess) {
                    Text(
                        text = "Password changed successfully!",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Show error from viewmodel if there is one
                if (authViewModel.errorMessage.isNotBlank()) {
                    Text(
                        text = authViewModel.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Check if any fields are empty
                if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                    errorMessage = "Please fill in all fields"
                    // Check if new passwords match
                } else if (newPassword != confirmPassword) {
                    errorMessage = "New passwords don't match"
                    // Check if new password is long enough
                } else if (newPassword.length < 6) {
                    errorMessage = "Password must be at least 6 characters"
                } else {
                    errorMessage = ""
                    authViewModel.changePassword(oldPassword, newPassword)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                // Reset everything when closing
                authViewModel.errorMessage = ""
                authViewModel.passwordChangeSuccess = false
                onDismiss()
            }) {
                Text("Cancel")
            }
        }
    )
}