package com.zyproo.devlink.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.data.remote.dto.*
import com.zyproo.devlink.ui.file.FileViewModel
import com.zyproo.devlink.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profile: ProfileResponse,
    isSaving: Boolean = false,
    saveError: String? = null,
    onBackClick: () -> Unit,
    onSaveClick: (
        username: String,
        name: String,
        course: Course?,
        branch: Branch?,
        admissionYear: Int?,
        semester: Int?,
        bio: String,
        contact: String,
        photoUrl: String?,
        employeeId: Long?,
        department: String?,
        designation: String?,
        companyName: String?,
        companyLogo: String?,
        socialLinks: List<SocialLinkRequest>,
        about: String?,
        website: String?
    ) -> Unit
) {
    val isRecruiter = profile.role == "RECRUITER"

    // State variables
    var bio by remember { mutableStateOf(profile.bio ?: "") }
    var photoUrl by remember { mutableStateOf(profile.photoUrl) }
    var localSelectedUri by remember { mutableStateOf<Uri?>(null) }
    
    // Recruiter specific states
    var companyName by remember { mutableStateOf(profile.companyName ?: "") }
    var companyLogo by remember { mutableStateOf(profile.companyLogo) }
    var designation by remember { mutableStateOf(profile.designation ?: "") }
    var contact by remember { mutableStateOf(profile.contact ?: "") }
    var about by remember { mutableStateOf(profile.about ?: "") }
    var website by remember { mutableStateOf(profile.website ?: "") }

    val initialSocialLinks = profile.socialLinks?.map { SocialLinkRequest(it.platform, it.url) } ?: emptyList()
    var socialLinksList by remember { mutableStateOf(initialSocialLinks) }

    var showAddLinkDialog by remember { mutableStateOf(false) }
    val fileViewModel: FileViewModel = hiltViewModel()
    val fileState by fileViewModel.uiState.collectAsState()

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            localSelectedUri = it
            fileViewModel.uploadFile(it) { uploadedUrl ->
                if (isRecruiter) {
                    companyLogo = uploadedUrl
                } else {
                    photoUrl = uploadedUrl
                }
            }
        }
    }

    val performSave = {
        if (isRecruiter) {
            onSaveClick(
                profile.username,
                "",
                null,
                null,
                null,
                null,
                "",
                contact,
                null,
                null,
                null,
                designation,
                companyName,
                companyLogo,
                emptyList(),
                about,
                website
            )
        } else {
            onSaveClick(
                profile.username,
                profile.name ?: "",
                profile.course,
                profile.branch,
                profile.admissionYear,
                profile.semester,
                bio,
                profile.contact ?: "",
                photoUrl,
                profile.employeeId,
                profile.department,
                profile.designation,
                profile.companyName,
                profile.companyLogo,
                socialLinksList,
                null,
                null
            )
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color(0xFFF1F5F9),
        focusedTextColor = DevLinkTextPrimary,
        unfocusedTextColor = DevLinkTextPrimary,
        disabledTextColor = Color(0xFF334155),
        focusedLabelColor = DevLinkBlue,
        unfocusedLabelColor = DevLinkTextSecondary,
        disabledLabelColor = Color(0xFF475569),
        focusedBorderColor = DevLinkBlue,
        unfocusedBorderColor = Color(0xFFCBD5E1),
        disabledBorderColor = Color(0xFFCBD5E1)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = DevLinkTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = DevLinkTextPrimary)
                    }
                },
                actions = {
                    Button(
                        onClick = performSave,
                        enabled = !isSaving && !fileState.isUploading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DevLinkBlue,
                            disabledContainerColor = DevLinkBlue.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (isSaving || fileState.isUploading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (fileState.isUploading) "Uploading..." else "Saving...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF6F7FB))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error alerts if any
                if (!saveError.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(saveError, color = Color(0xFF991B1B), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (!fileState.error.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Image upload failed: ${fileState.error}", color = Color(0xFF991B1B), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Avatar Upload Section
                val rawAvatarUrl = if (isRecruiter) companyLogo else photoUrl
                val fullAvatarUrl = rawAvatarUrl?.toFullMediaUrl()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clickable { avatarPicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        // Circular Avatar Image Container (Clipped to Circle)
                        Box(
                            modifier = Modifier
                                .size(104.dp)
                                .clip(CircleShape)
                                .background(DevLinkBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            val currentAvatarModel = localSelectedUri ?: fullAvatarUrl
                            val avatarName = if (isRecruiter) companyName else (profile.name ?: profile.username)
                            com.zyproo.devlink.ui.common.UserAvatar(
                                model = currentAvatarModel,
                                name = avatarName,
                                modifier = Modifier.fillMaxSize(),
                                fontSize = 32.sp
                            )

                            if (fileState.isUploading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
                                }
                            }
                        }

                        // Camera Overlay Button Badge (Unclipped, Layered on top with shadow and border)
                        Surface(
                            shape = CircleShape,
                            color = DevLinkBlue,
                            border = BorderStroke(2.5.dp, Color.White),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(34.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isRecruiter) "Tap logo to choose from gallery" else "Tap avatar to choose photo from gallery",
                        fontSize = 12.sp,
                        color = DevLinkBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isRecruiter) {
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Company Name") },
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Designation") },
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = contact,
                        onValueChange = { contact = it },
                        label = { Text("Contact Phone") },
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = website,
                        onValueChange = { website = it },
                        label = { Text("Company Website") },
                        placeholder = { Text("https://techcorp.com") },
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = about,
                        onValueChange = { about = it },
                        label = { Text("About Company") },
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                } else {
                    // Read-Only student details
                    OutlinedTextField(
                        value = profile.name ?: "",
                        onValueChange = {},
                        label = { Text("Full Name") },
                        enabled = false,
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = "@${profile.username}",
                        onValueChange = {},
                        label = { Text("Username") },
                        enabled = false,
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = profile.course?.name ?: "",
                            onValueChange = {},
                            label = { Text("Course") },
                            enabled = false,
                            colors = fieldColors,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = profile.branch?.name ?: "",
                            onValueChange = {},
                            label = { Text("Branch") },
                            enabled = false,
                            colors = fieldColors,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Description / Bio
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Description (Bio)") },
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    // Social Links Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Social Links (${socialLinksList.size} / 5 Max)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DevLinkTextPrimary
                                )
                                if (socialLinksList.size < 5) {
                                    TextButton(onClick = { showAddLinkDialog = true }) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = DevLinkBlue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ Add Link", color = DevLinkBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }

                            if (socialLinksList.isEmpty()) {
                                Text(
                                    text = "No social links added yet. Click '+ Add Link' to showcase your GitHub, LinkedIn, etc.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                socialLinksList.forEachIndexed { index, link ->
                                    val (icon, name) = getPlatformMeta(link.platform)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            SocialPlatformIcon(link.platform, modifier = Modifier.size(24.dp))
                                            Column {
                                                Text(
                                                    text = name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = DevLinkTextPrimary
                                                )
                                                Text(
                                                    text = link.url,
                                                    fontSize = 11.sp,
                                                    color = DevLinkTextSecondary,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                socialLinksList = socialLinksList.filterIndexed { i, _ -> i != index }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete link",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    if (index < socialLinksList.size - 1) {
                                        HorizontalDivider(color = Color(0xFFF1F5F9))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Dialog overlay for Add Social Link with Logo/Icon Selection UI
            if (showAddLinkDialog) {
                var selectedPlatform by remember { mutableStateOf(SocialPlatform.GITHUB) }
                var urlInput by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showAddLinkDialog = false },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Color.White,
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Add Social Link", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary)
                            IconButton(onClick = { showAddLinkDialog = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("Select Platform:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary)

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    SocialPlatform.GITHUB,
                                    SocialPlatform.LINKEDIN,
                                    SocialPlatform.LEETCODE,
                                    SocialPlatform.INSTAGRAM,
                                    SocialPlatform.TWITTER,
                                    SocialPlatform.PORTFOLIO,
                                    SocialPlatform.YOUTUBE,
                                    SocialPlatform.CODEFORCES
                                ).forEach { platform ->
                                    val (icon, name) = getPlatformMeta(platform)
                                    val isSelected = selectedPlatform == platform
                                    Surface(
                                        modifier = Modifier.clickable { selectedPlatform = platform },
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (isSelected) DevLinkBlue else Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, if (isSelected) DevLinkBlue else Color(0xFFCBD5E1))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            SocialPlatformIcon(platform, modifier = Modifier.size(18.dp))
                                            Text(
                                                text = name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else DevLinkTextPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            val (selectedIcon, selectedName) = getPlatformMeta(selectedPlatform)

                            OutlinedTextField(
                                value = urlInput,
                                onValueChange = { urlInput = it },
                                label = { Text("Profile URL for $selectedName *") },
                                placeholder = { Text("https://${selectedName.lowercase()}.com/yourhandle") },
                                leadingIcon = { Text(selectedIcon, fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp)) },
                                colors = fieldColors,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (urlInput.isNotBlank() && socialLinksList.size < 5) {
                                    socialLinksList = socialLinksList + SocialLinkRequest(selectedPlatform, urlInput)
                                    showAddLinkDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue),
                            shape = RoundedCornerShape(8.dp),
                            enabled = urlInput.isNotBlank()
                        ) {
                            Text("Add Link", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddLinkDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}
