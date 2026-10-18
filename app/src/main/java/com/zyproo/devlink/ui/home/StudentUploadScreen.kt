package com.zyproo.devlink.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
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
fun StudentUploadScreen(
    onUploadProject: (String, String, String, String?, String?, List<String>?) -> Unit,
    onUploadCertificate: (String, String, String, String, FileType) -> Unit,
    fileViewModel: FileViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val fileState by fileViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DevLinkBackground)
    ) {
        // Tab Segment Row
        Surface(
            color = Color.White,
            border = BorderStroke(1.dp, DevLinkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0 to "🚀 Project Submission",
                    1 to "📜 Certificate Upload"
                ).forEach { (tabIndex, label) ->
                    val isSelected = selectedTab == tabIndex
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = tabIndex },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) DevLinkBlue else Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else DevLinkTextPrimary,
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DevLinkBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (selectedTab == 0) {
                        var name by remember { mutableStateOf("") }
                        var desc by remember { mutableStateOf("") }
                        var tech by remember { mutableStateOf("") }
                        var github by remember { mutableStateOf("") }
                        var demo by remember { mutableStateOf("") }
                        val projectImages = remember { mutableStateListOf<String>() }

                        Column {
                            Text("Submit Project for Evaluation", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary)
                            Text("Share your build with teachers and recruiters.", fontSize = 12.sp, color = DevLinkTextSecondary)
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Project Title *") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = desc,
                            onValueChange = { desc = it },
                            label = { Text("Project Description *") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                        
                        OutlinedTextField(
                            value = tech,
                            onValueChange = { tech = it },
                            label = { Text("Tech Stack (comma separated) *") },
                            placeholder = { Text("e.g. Kotlin, Jetpack Compose, Spring Boot") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = github,
                            onValueChange = { github = it },
                            label = { Text("GitHub Repository URL *") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = demo,
                            onValueChange = { demo = it },
                            label = { Text("Live Demo URL (Optional)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        val projectImagePicker = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetMultipleContents()
                        ) { uris: List<Uri> ->
                            if (uris.isNotEmpty()) {
                                val remaining = 5 - projectImages.size
                                if (remaining > 0) {
                                    val toUpload = uris.take(remaining)
                                    fileViewModel.uploadMultipleFiles(toUpload) { uploadedUrl ->
                                        if (projectImages.size < 5) {
                                            projectImages.add(uploadedUrl)
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Project Screenshots Section (Max 5 Capacity Visual Preview)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(BorderStroke(1.dp, DevLinkBorder), RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = DevLinkBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Project Screenshots",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DevLinkTextPrimary
                                    )
                                }

                                Surface(
                                    color = if (projectImages.size >= 5) Color(0xFFFEF2F2) else DevLinkBlueContainer,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = "${projectImages.size} / 5 Max",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (projectImages.size >= 5) Color(0xFFDC2626) else DevLinkBlue,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (projectImages.size < 5) {
                                OutlinedButton(
                                    onClick = { projectImagePicker.launch("image/*") },
                                    enabled = !fileState.isUploading,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, DevLinkBlue)
                                ) {
                                    if (fileState.isUploading) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = DevLinkBlue)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Uploading Screenshots...", fontSize = 13.sp, color = DevLinkBlue)
                                    } else {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Screenshots", tint = DevLinkBlue, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Select Screenshots (${5 - projectImages.size} remaining)", fontSize = 13.sp, color = DevLinkBlue, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            } else {
                                Text(
                                    text = "Maximum capacity of 5 screenshots reached.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFDC2626),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Selected Images Preview with Vertical Scroll
                            if (projectImages.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 280.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    projectImages.forEachIndexed { index, rawUrl ->
                                        val fullUrl = rawUrl.toFullMediaUrl()
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 180.dp, max = 260.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF0F172A)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!fullUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = fullUrl,
                                                    contentDescription = "Screenshot ${index + 1}",
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }

                                            // Number Badge
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.65f),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = "Screenshot ${index + 1} of ${projectImages.size}",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }

                                            // Remove Image Button
                                            IconButton(
                                                onClick = { projectImages.removeAt(index) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .size(28.dp)
                                                    .background(Color.Red.copy(alpha = 0.85f), androidx.compose.foundation.shape.CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove screenshot",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (fileState.error != null) {
                            Text(text = fileState.error.orEmpty(), color = DevLinkError, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                onUploadProject(
                                    name,
                                    desc,
                                    tech,
                                    github.ifBlank { null },
                                    demo.ifBlank { null },
                                    projectImages.toList().ifEmpty { null }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue),
                            enabled = name.isNotBlank() && desc.isNotBlank() && tech.isNotBlank() && github.isNotBlank()
                        ) {
                            Text("Upload & Request Verification", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        var title by remember { mutableStateOf("") }
                        var org by remember { mutableStateOf("") }
                        var purpose by remember { mutableStateOf("") }
                        var url by remember { mutableStateOf("") }
                        var type by remember { mutableStateOf(FileType.PDF) }

                        Column {
                            Text("Upload Certificate for Verification", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary)
                            Text("Submit course & hackathon certificates.", fontSize = 12.sp, color = DevLinkTextSecondary)
                        }

                        // Certificate Format Selector (PDF vs Image)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Certificate Format *", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DevLinkTextPrimary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf(
                                    FileType.PDF to "📄 PDF Document",
                                    FileType.IMAGE to "🖼️ Image (PNG / JPG)"
                                ).forEach { (fType, label) ->
                                    val isSelected = type == fType
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                if (type != fType) {
                                                    type = fType
                                                    url = "" // Reset selected file on type switch
                                                }
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) DevLinkBlue else Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, if (isSelected) DevLinkBlue else DevLinkBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else DevLinkTextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Certificate Title *") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = org,
                            onValueChange = { org = it },
                            label = { Text("Issuing Organization *") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = purpose,
                            onValueChange = { purpose = it },
                            label = { Text("Purpose / Summary *") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        val certFilePicker = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri: Uri? ->
                            uri?.let {
                                fileViewModel.uploadFile(it) { uploadedUrl ->
                                    url = uploadedUrl
                                }
                            }
                        }

                        // Certificate File Attachment Box (No Raw URL TextField)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(BorderStroke(1.dp, DevLinkBorder), RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (type == FileType.PDF) "PDF Certificate File *" else "Image Certificate File *",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevLinkTextPrimary
                            )

                            if (url.isBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        val mimeType = if (type == FileType.PDF) "application/pdf" else "image/*"
                                        certFilePicker.launch(mimeType)
                                    },
                                    enabled = !fileState.isUploading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, DevLinkBlue)
                                ) {
                                    if (fileState.isUploading) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = DevLinkBlue)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Uploading ${type.name}...", fontSize = 13.sp, color = DevLinkBlue, fontWeight = FontWeight.SemiBold)
                                    } else {
                                        Icon(
                                            imageVector = if (type == FileType.PDF) Icons.Default.Description else Icons.Default.AddPhotoAlternate,
                                            contentDescription = null,
                                            tint = DevLinkBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (type == FileType.PDF) "Select PDF Document" else "Select Certificate Image",
                                            fontSize = 14.sp,
                                            color = DevLinkBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                // Attached File Preview Card
                                if (type == FileType.IMAGE) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 180.dp, max = 260.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF0F172A)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val fullUrl = url.toFullMediaUrl()
                                        if (!fullUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = fullUrl,
                                                contentDescription = "Certificate Image",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Surface(
                                            color = Color.Black.copy(alpha = 0.65f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = "IMAGE CERTIFICATE ATTACHED",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { url = "" },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                                .size(28.dp)
                                                .background(Color.Red.copy(alpha = 0.85f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove file",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                } else {
                                    // PDF Card
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Surface(
                                                    color = Color(0xFFFEF2F2),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.size(44.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Description,
                                                            contentDescription = "PDF File",
                                                            tint = Color(0xFFDC2626),
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                }

                                                Column {
                                                    Text(
                                                        text = if (title.isNotBlank()) "$title.pdf" else "Certificate_Document.pdf",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = DevLinkTextPrimary,
                                                        maxLines = 1
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Surface(
                                                        color = Color(0xFFDCFCE7),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "PDF DOCUMENT READY",
                                                            color = Color(0xFF15803D),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            IconButton(
                                                onClick = { url = "" },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(Color.Red.copy(alpha = 0.85f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove PDF",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (fileState.error != null) {
                            Text(text = fileState.error.orEmpty(), color = DevLinkError, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { onUploadCertificate(title, org, purpose, url, type) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue),
                            enabled = title.isNotBlank() && org.isNotBlank() && purpose.isNotBlank() && url.isNotBlank()
                        ) {
                            Text("Upload Certificate", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
