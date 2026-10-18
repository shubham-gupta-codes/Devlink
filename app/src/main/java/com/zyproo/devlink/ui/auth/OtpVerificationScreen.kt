package com.zyproo.devlink.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zyproo.devlink.ui.navigation.DevLinkDestinations
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun OtpVerificationScreen(
    email: String,
    onVerified: (route: String) -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    LaunchedEffect(email) {
        viewModel.setEmail(email)
    }
    val uiState by viewModel.uiState.collectAsState()
    var secondsRemaining by remember { mutableIntStateOf((uiState.otpExpiryMinutes ?: 5) * 60) }

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            if (event is AuthNavigationEvent.NavigateToHome) {
                onVerified(DevLinkDestinations.homeRouteFor(event.role))
            }
        }
    }

    // Countdown for "Resend OTP" — mirrors the mockup's "Resend OTP (00:45)" text.
    LaunchedEffect(secondsRemaining) {
        if (secondsRemaining > 0) {
            delay(1000.milliseconds)
            secondsRemaining -= 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Verify Email",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Enter the 6 digit code sent to ${uiState.email}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OtpCodeInput(
            otp = uiState.otp,
            onOtpChange = viewModel::onOtpChange
        )

        if (uiState.verifyError != null) {
            Text(
                text = uiState.verifyError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (secondsRemaining > 0) {
                    "Didn't receive the code? Resend OTP (${formatTime(secondsRemaining)})"
                } else {
                    "Didn't receive the code?"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (secondsRemaining == 0) {
                TextButton(onClick = {
                    viewModel.resendOtp()
                    secondsRemaining = (uiState.otpExpiryMinutes ?: 5) * 60
                }) {
                    Text("Resend")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = viewModel::verifyOtp,
            enabled = !uiState.isVerifyLoading && uiState.otp.replace(" ", "").length == 6,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (uiState.isVerifyLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Verify", fontWeight = FontWeight.SemiBold)
            }
        }

        TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
            Text("Back to login")
        }
    }
}

/**
 * 6-digit OTP input rendered with individual boxes.
 * - Backspace removes digits one by one from right to left reliably on all keyboards.
 * - Tapping any box selects that exact index to delete or replace it.
 * - Uses neutral slate borders when selected (no blue borders).
 * - No outer OutlinedTextField rectangle outline.
 */
@Composable
private fun OtpCodeInput(
    otp: String,
    onOtpChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = otp,
                selection = TextRange(otp.length)
            )
        )
    }

    LaunchedEffect(otp) {
        if (textFieldValue.text != otp) {
            textFieldValue = TextFieldValue(
                text = otp,
                selection = TextRange(otp.length)
            )
        }
    }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newTfv ->
            val filtered = newTfv.text.filter { it.isDigit() }.take(6)
            textFieldValue = newTfv.copy(text = filtered)
            onOtpChange(filtered)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { keyboardController?.hide() }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused },
        decorationBox = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(6) { index ->
                    val digit = otp.getOrNull(index)?.toString() ?: ""
                    val cursorEnd = textFieldValue.selection.end
                    val isCellActive = isFocused && (
                        if (textFieldValue.selection.collapsed) {
                            if (cursorEnd < 6) index == cursorEnd else index == 5
                        } else {
                            index >= textFieldValue.selection.start && index < textFieldValue.selection.end
                        }
                    )

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = if (isCellActive) 2.dp else 1.5.dp,
                                color = when {
                                    isCellActive -> Color(0xFF475569) // Neutral slate border when selected (no blue)
                                    digit.isNotEmpty() -> Color(0xFF94A3B8) // Neutral gray when filled
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                            .clickable {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                                if (index < otp.length) {
                                    // Highlight/select this specific digit so backspace deletes it or typing replaces it
                                    textFieldValue = textFieldValue.copy(
                                        selection = TextRange(index, index + 1)
                                    )
                                } else {
                                    // If empty, position cursor at the next available position
                                    textFieldValue = textFieldValue.copy(
                                        selection = TextRange(otp.length)
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    )
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
