package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirebaseChatRepository
import com.example.ui.theme.WhatsAppDarkBg
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppLightGreen
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    repository: FirebaseChatRepository,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isRegisterMode by remember { mutableStateOf(false) }

    // Email / Password inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Phone Auth inputs
    var phoneNumber by remember { mutableStateOf("") }
    var phonePin by remember { mutableStateOf("") }
    var phoneName by remember { mutableStateOf("") }

    // State
    var isLoading by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetLoading by remember { mutableStateOf(false) }

    val tabs = listOf("Email", "Phone Number", "Quick Demo")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // App Emblem
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F2C34)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "SecureChat Emblem",
                    tint = WhatsAppGreen,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SecureChat",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = WhatsAppTextPrimary,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "End-to-End Encrypted Messenger",
                fontSize = 13.sp,
                color = WhatsAppTextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Tabs for Auth methods
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = WhatsAppDarkSurface,
                contentColor = WhatsAppGreen,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = WhatsAppGreen
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) WhatsAppGreen else WhatsAppTextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.testTag("auth_tab_$index")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedTabIndex) {
                0 -> {
                    // EMAIL & PASSWORD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = if (isRegisterMode) "Create Account" else "Welcome Back",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isRegisterMode) "Register to receive your unique Secure Chat ID" else "Sign in to access your encrypted conversations",
                                fontSize = 12.sp,
                                color = WhatsAppTextSecondary
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            if (isRegisterMode) {
                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = { Text("Display Name") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = "Name", tint = WhatsAppGreen)
                                    },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = WhatsAppGreen,
                                        unfocusedBorderColor = Color(0xFF2A3942),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("register_name_input")
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = "Email", tint = WhatsAppGreen)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WhatsAppGreen,
                                    unfocusedBorderColor = Color(0xFF2A3942),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = "Password", tint = WhatsAppGreen)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle password",
                                            tint = WhatsAppTextSecondary
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WhatsAppGreen,
                                    unfocusedBorderColor = Color(0xFF2A3942),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("password_input")
                            )

                            if (!isRegisterMode) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            resetEmail = email
                                            showForgotPasswordDialog = true
                                        },
                                        modifier = Modifier.testTag("forgot_password_button")
                                    ) {
                                        Text(
                                            text = "Forgot Password?",
                                            color = WhatsAppLightGreen,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (email.isBlank() || password.isBlank()) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Please enter both email and password")
                                        }
                                        return@Button
                                    }
                                    isLoading = true
                                    coroutineScope.launch {
                                        val result = if (isRegisterMode) {
                                            repository.registerWithEmail(
                                                email = email.trim(),
                                                pass = password.trim(),
                                                name = fullName.trim().ifBlank { "User" }
                                            )
                                        } else {
                                            repository.loginWithEmail(
                                                email = email.trim(),
                                                pass = password.trim()
                                            )
                                        }
                                        isLoading = false
                                        result.onSuccess {
                                            onAuthSuccess()
                                        }.onFailure { err ->
                                            snackbarHostState.showSnackbar(err.localizedMessage ?: "Authentication failed")
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("auth_submit_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(
                                        text = if (isRegisterMode) "Register" else "Sign In",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0B141A)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isRegisterMode) "Already have an account?" else "Don't have an account?",
                                    color = WhatsAppTextSecondary,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isRegisterMode) "Sign In" else "Register",
                                    color = WhatsAppLightGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .clickable { isRegisterMode = !isRegisterMode }
                                        .padding(4.dp)
                                        .testTag("toggle_register_mode")
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // PHONE NUMBER AUTHENTICATION
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Phone Verification",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Verify your phone number with your secure profile",
                                fontSize = 12.sp,
                                color = WhatsAppTextSecondary
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = phoneName,
                                onValueChange = { phoneName = it },
                                label = { Text("Your Name") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = "Name", tint = WhatsAppGreen)
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WhatsAppGreen,
                                    unfocusedBorderColor = Color(0xFF2A3942),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_name_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Phone Number (with Country Code)") },
                                placeholder = { Text("+1 555 123 4567") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = "Phone", tint = WhatsAppGreen)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WhatsAppGreen,
                                    unfocusedBorderColor = Color(0xFF2A3942),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = phonePin,
                                onValueChange = { phonePin = it },
                                label = { Text("Secure PIN / Password (min 6 chars)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = "PIN", tint = WhatsAppGreen)
                                },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WhatsAppGreen,
                                    unfocusedBorderColor = Color(0xFF2A3942),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_pin_input")
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    val cleanPhone = phoneNumber.filter { it.isDigit() || it == '+' }
                                    if (cleanPhone.length < 8 || phonePin.length < 6) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Enter a valid phone number and 6+ char PIN")
                                        }
                                        return@Button
                                    }

                                    // Maps phone auth into Firebase email-friendly token
                                    val syntheticEmail = "phone_${cleanPhone.replace("+", "")}@securechat.net"
                                    isLoading = true
                                    coroutineScope.launch {
                                        val result = repository.quickLoginOrRegister(
                                            demoEmail = syntheticEmail,
                                            demoPass = phonePin,
                                            displayName = phoneName.ifBlank { "User ${cleanPhone.takeLast(4)}" },
                                            defaultChatId = "SC-" + cleanPhone.takeLast(6),
                                            defaultAvatar = "avatar_2"
                                        )
                                        isLoading = false
                                        result.onSuccess {
                                            onAuthSuccess()
                                        }.onFailure { err ->
                                            snackbarHostState.showSnackbar(err.localizedMessage ?: "Phone login failed")
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("phone_submit_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(
                                        text = "Verify & Continue",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0B141A)
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // FAST DEMO TESTING ACCOUNTS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Instant Test Profiles",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Test two-way live messaging and delivery ticks right away with pre-configured accounts:",
                                fontSize = 12.sp,
                                color = WhatsAppTextSecondary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val demoProfiles = listOf(
                                Triple("Hamza (Primary)", "hamza.secure@test.com", "SC-772101"),
                                Triple("Elena Vance", "elena.secure@test.com", "SC-883204"),
                                Triple("Marcus Reed", "marcus.secure@test.com", "SC-441920")
                            )

                            demoProfiles.forEachIndexed { idx, (name, demoEmail, chatId) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable {
                                            isLoading = true
                                            coroutineScope.launch {
                                                val res = repository.quickLoginOrRegister(
                                                    demoEmail = demoEmail,
                                                    demoPass = "Secure123!",
                                                    displayName = name,
                                                    defaultChatId = chatId,
                                                    defaultAvatar = "avatar_${idx + 1}"
                                                )
                                                isLoading = false
                                                res.onSuccess {
                                                    onAuthSuccess()
                                                }.onFailure { err ->
                                                    snackbarHostState.showSnackbar(err.localizedMessage ?: "Failed")
                                                }
                                            }
                                        }
                                        .testTag("demo_profile_card_$idx"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2C34))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = name,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "ID: $chatId",
                                                color = WhatsAppGreen,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                isLoading = true
                                                coroutineScope.launch {
                                                    val res = repository.quickLoginOrRegister(
                                                        demoEmail = demoEmail,
                                                        demoPass = "Secure123!",
                                                        displayName = name,
                                                        defaultChatId = chatId,
                                                        defaultAvatar = "avatar_${idx + 1}"
                                                    )
                                                    isLoading = false
                                                    res.onSuccess { onAuthSuccess() }
                                                        .onFailure { err ->
                                                            snackbarHostState.showSnackbar(err.localizedMessage ?: "Failed")
                                                        }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Switch", color = Color(0xFF0B141A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Forgot password dialog
        if (showForgotPasswordDialog) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Reset Password",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter your registered email address to receive a secure password reset link via Firebase Auth.",
                        fontSize = 13.sp,
                        color = WhatsAppTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WhatsAppGreen,
                            unfocusedBorderColor = Color(0xFF2A3942),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_email_input")
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showForgotPasswordDialog = false }) {
                            Text("Cancel", color = WhatsAppTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (resetEmail.isBlank()) return@Button
                                resetLoading = true
                                coroutineScope.launch {
                                    val res = repository.sendPasswordReset(resetEmail.trim())
                                    resetLoading = false
                                    showForgotPasswordDialog = false
                                    res.onSuccess {
                                        snackbarHostState.showSnackbar("Password reset email sent to $resetEmail")
                                    }.onFailure { err ->
                                        snackbarHostState.showSnackbar(err.localizedMessage ?: "Failed to send reset email")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            modifier = Modifier.testTag("send_reset_button")
                        ) {
                            if (resetLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Text("Send Link", color = Color(0xFF0B141A), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
