package com.zyproo.devlink.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
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
import com.zyproo.devlink.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
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

    // Countdown for "Resend OTP"
    LaunchedEffect(secondsRemaining) {
        if (secondsRemaining > 0) {
            delay(1000.milliseconds)
            secondsRemaining -= 1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DevLinkTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DevLinkBackground)
            )
        },
        containerColor = DevLinkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Email illustration badge
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Verify Email",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DevLinkTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter the 6-digit code sent to",
                style = MaterialTheme.typography.bodyMedium,
                color = DevLinkTextSecondary
            )
            Text(
                text = uiState.email.ifBlank { email },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = DevLinkTextPrimary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Security Code Header row with Clear button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Verification Code",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DevLinkTextSecondary
                )

                if (uiState.otp.replace(" ", "").isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearOtp() },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Clear code",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6-Cell individual OTP Code Input
            OtpCodeInput(
                otp = uiState.otp,
                onOtpChange = viewModel::onOtpChange
            )

            // Error display alert
            if (uiState.verifyError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.verifyError.orEmpty(),
                            color = Color(0xFFB91C1C),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resend code countdown / action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = DevLinkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (secondsRemaining > 0) {
                            "Resend code in ${formatTime(secondsRemaining)}"
                        } else {
                            "Didn't receive the code?"
                        },
                        fontSize = 13.sp,
                        color = DevLinkTextSecondary
                    )
                }

                if (secondsRemaining == 0) {
                    TextButton(
                        onClick = {
                            viewModel.resendOtp()
                            secondsRemaining = (uiState.otpExpiryMinutes ?: 5) * 60
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Resend OTP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DevLinkBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Verify button
            val isCodeComplete = uiState.otp.replace(" ", "").length == 6
            Button(
                onClick = viewModel::verifyOtp,
                enabled = !uiState.isVerifyLoading && isCodeComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DevLinkBlue,
                    disabledContainerColor = Color(0xFFCBD5E1),
                    disabledContentColor = Color.White
                )
            ) {
                if (uiState.isVerifyLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Verify Code",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onBack) {
                    Text(
                        text = "Wrong email? Back to login",
                        color = DevLinkTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * 6-Cell individual OTP Code Input
 * Each cell can be individually selected and deleted.
 * Features:
 * - Sleek charcoal/slate focus border (no harsh blue border).
 * - Direct cell tapping to modify or delete a specific index.
 * - Backspace deletion of specific cells.
 * - Multi-digit paste support.
 */
@Composable
private fun OtpCodeInput(
    otp: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Convert OTP string into a 6-element list where ' ' represents an empty cell
    val charList = remember(otp) {
        val list = MutableList(6) { ' ' }
        for (i in 0 until minOf(6, otp.length)) {
            list[i] = otp[i]
        }
        list
    }

    // Auto-focus first empty cell on appearance
    LaunchedEffect(Unit) {
        val firstEmpty = charList.indexOfFirst { !it.isDigit() }
        val target = if (firstEmpty != -1) firstEmpty else 0
        try {
            focusRequesters[target].requestFocus()
        } catch (_: Exception) {}
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
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
                    .weight(1f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isFocused) Color.White else Color(0xFFF8FAFC))
                    .border(
                        width = if (isFocused) 2.dp else 1.2.dp,
                        color = when {
                            isFocused -> Color(0xFF334155) // Sleek slate-700 when selected
                            hasDigit -> Color(0xFF94A3B8)  // Neutral slate-400 for filled cells
                            else -> Color(0xFFE2E8F0)      // Soft slate-200 for empty cells
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
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
                            // User deleted this specific cell
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
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF0F172A)
                    ),
                    cursorBrush = SolidColor(Color(0xFF334155))
                )

                // Bottom cursor indicator when focused and empty
                if (isFocused && !hasDigit) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .width(16.dp)
                            .height(2.dp)
                            .background(Color(0xFF64748B), RoundedCornerShape(1.dp))
                    )
                }
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
