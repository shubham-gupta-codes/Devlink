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
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
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
 * Renders the OTP as 6 individual boxes without blue focus borders,
 * allowing specific index selection and deletion.
 */
@Composable
private fun OtpCodeInput(
    otp: String,
    onOtpChange: (String) -> Unit
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val keyboardController = LocalSoftwareKeyboardController.current

    val charList = remember(otp) {
        val list = MutableList(6) { ' ' }
        for (i in 0 until minOf(6, otp.length)) {
            list[i] = otp[i]
        }
        list
    }

    LaunchedEffect(Unit) {
        val firstEmpty = charList.indexOfFirst { !it.isDigit() }
        val target = if (firstEmpty != -1) firstEmpty else 0
        try {
            focusRequesters[target].requestFocus()
        } catch (_: Exception) {}
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (index in 0 until 6) {
            val char = charList[index]
            val hasDigit = char.isDigit()
            var isFocused by remember { mutableStateOf(false) }

            val digitStr = if (hasDigit) char.toString() else ""
            var textFieldValue by remember(char) {
                mutableStateOf(
                    TextFieldValue(
                        text = digitStr,
                        selection = TextRange(digitStr.length)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(
                        width = if (isFocused) 2.dp else 1.5.dp,
                        color = when {
                            isFocused -> Color(0xFF475569) // Neutral slate border when selected (no blue)
                            hasDigit -> Color(0xFF94A3B8)  // Neutral gray when filled
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                    .clickable {
                        focusRequesters[index].requestFocus()
                    },
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newTfv ->
                        val text = newTfv.text
                        if (text.isEmpty()) {
                            // Deleting this specific index
                            charList[index] = ' '
                            onOtpChange(charList.joinToString(""))
                            textFieldValue = TextFieldValue("", TextRange.Zero)
                        } else {
                            val digits = text.filter { it.isDigit() }
                            if (digits.length == 1) {
                                val d = digits[0]
                                charList[index] = d
                                onOtpChange(charList.joinToString(""))
                                textFieldValue = TextFieldValue(d.toString(), TextRange(1))
                                if (index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                } else {
                                    keyboardController?.hide()
                                }
                            } else if (digits.length > 2) {
                                // Multi-digit paste
                                var target = index
                                for (d in digits) {
                                    if (target < 6) {
                                        charList[target] = d
                                        target++
                                    }
                                }
                                onOtpChange(charList.joinToString(""))
                                if (target < 6) {
                                    focusRequesters[target].requestFocus()
                                } else {
                                    keyboardController?.hide()
                                }
                            } else if (digits.length == 2) {
                                // Overwriting existing digit in this cell
                                val newChar = if (digits.first() == char) digits.last() else digits.first()
                                charList[index] = newChar
                                onOtpChange(charList.joinToString(""))
                                textFieldValue = TextFieldValue(newChar.toString(), TextRange(1))
                                if (index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                } else {
                                    keyboardController?.hide()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                        .focusRequester(focusRequesters[index])
                        .onFocusChanged { isFocused = it.isFocused }
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace) {
                                if (charList[index] != ' ') {
                                    charList[index] = ' '
                                    onOtpChange(charList.joinToString(""))
                                    textFieldValue = TextFieldValue("", TextRange.Zero)
                                } else if (index > 0) {
                                    charList[index - 1] = ' '
                                    onOtpChange(charList.joinToString(""))
                                    focusRequesters[index - 1].requestFocus()
                                }
                                true
                            } else {
                                false
                            }
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = if (index == 5) ImeAction.Done else ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            if (index < 5) focusRequesters[index + 1].requestFocus()
                        },
                        onDone = {
                            keyboardController?.hide()
                        }
                    ),
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(Color(0xFF475569))
                )
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
