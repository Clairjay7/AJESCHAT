package com.example.ajeschat.ui.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ajeschat.data.ProfileData
import com.example.ajeschat.data.ProfileRepository
import com.example.ajeschat.session.SessionHolder
import com.example.ajeschat.session.SessionStore
import kotlinx.coroutines.launch

@Composable
fun ProfileTab(
    repository: ProfileRepository,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val appCtx = LocalContext.current.applicationContext
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var profile by remember { mutableStateOf<ProfileData?>(null) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var markRemovePhoto by remember { mutableStateOf(false) }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingPhotoUri = uri
            markRemovePhoto = false
        }
    }

    fun requestLoad() {
        loading = true
        error = null
        info = null
    }

    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        repository.getProfile()
            .onSuccess {
                profile = it
                name = it.name.orEmpty()
                email = it.email.orEmpty()
                contact = it.contact_number.orEmpty()
                bio = it.bio.orEmpty()
            }
            .onFailure {
                error = it.message ?: "Failed to load profile."
            }
        loading = false
    }

    if (loading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Profile Settings", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Everyone signed in can update their own profile and password here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Role: ${profile?.role ?: "-"}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = profile?.username.orEmpty(),
            onValueChange = { },
            readOnly = true,
            enabled = true,
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val imageModel: Any? = when {
                pendingPhotoUri != null -> pendingPhotoUri
                markRemovePhoto -> null
                else -> profile?.profile_photo_url
            }
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column {
                TextButton(onClick = { pickImage.launch("image/*") }) {
                    Text(if (pendingPhotoUri != null) "Change photo" else "Choose photo")
                }
                TextButton(
                    onClick = {
                        markRemovePhoto = true
                        pendingPhotoUri = null
                    },
                    enabled = profile?.profile_photo_url != null || pendingPhotoUri != null
                ) {
                    Text("Remove photo")
                }
            }
        }
        Text(
            "JPG, PNG, or WEBP (max 2MB). Save to apply.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()
        Text("Personal information", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; error = null },
            label = { Text("Full name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contact,
            onValueChange = { contact = it; error = null },
            label = { Text("Contact number") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it; error = null },
            label = { Text("Bio / About") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(Modifier.height(4.dp))
        HorizontalDivider()
        Spacer(Modifier.height(4.dp))
        Text("Security", style = MaterialTheme.typography.titleMedium)
        Text(
            "Leave new password empty to keep your current password.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = oldPassword,
            onValueChange = { oldPassword = it; error = null },
            label = { Text("Current password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (oldVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { oldVisible = !oldVisible }) {
                    Icon(
                        if (oldVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (oldVisible) "Hide password" else "Show password"
                    )
                }
            }
        )
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it; error = null },
            label = { Text("New password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { newVisible = !newVisible }) {
                    Icon(
                        if (newVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (newVisible) "Hide password" else "Show password"
                    )
                }
            }
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; error = null },
            label = { Text("Confirm new password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                    Icon(
                        if (confirmVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (confirmVisible) "Hide password" else "Show password"
                    )
                }
            }
        )

        if (error != null) {
            Text(error ?: "", color = MaterialTheme.colorScheme.error)
        }
        if (info != null) {
            Text(info ?: "", color = MaterialTheme.colorScheme.primary)
        }

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank()) return@Button
                val changing = newPassword.isNotBlank() || confirmPassword.isNotBlank()
                if (changing && oldPassword.isBlank()) {
                    error = "Enter your current password to set a new one."
                    return@Button
                }
                scope.launch {
                    saving = true
                    error = null
                    info = null
                    val doRemove = markRemovePhoto && pendingPhotoUri == null
                    repository.updateProfile(
                        name = name,
                        email = email,
                        contactNumber = contact,
                        bio = bio,
                        oldPassword = oldPassword,
                        newPassword = newPassword,
                        confirmPassword = confirmPassword,
                        photoUri = pendingPhotoUri,
                        removePhoto = doRemove
                    )
                        .onSuccess { msg ->
                            info = msg
                            pendingPhotoUri = null
                            markRemovePhoto = false
                            if (changing) {
                                oldPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                            }
                            SessionHolder.session?.let { s ->
                                SessionHolder.updateSession(s.copy(name = name.trim()))
                                SessionStore(appCtx).save(SessionHolder.session!!)
                            }
                            requestLoad()
                        }
                        .onFailure {
                            error = it.message ?: "Failed to save."
                        }
                    saving = false
                }
            },
            enabled = !saving && name.isNotBlank() && email.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (saving) "Saving..." else "Save")
        }

        TextButton(onClick = { requestLoad() }) { Text("Reload") }
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onLogout, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Log out")
        }
    }
}
