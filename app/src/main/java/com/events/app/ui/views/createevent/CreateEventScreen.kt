package com.events.app.ui.views.createevent

import com.events.app.BuildConfig
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
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
import coil.compose.SubcomposeAsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    viewModel: CreateEventViewModel,
    onNext: () -> Unit = {}
) {
    val title by viewModel.title.collectAsState()
    val announcement by viewModel.announcement.collectAsState()
    val description by viewModel.description.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val selectedPlaceholder by viewModel.selectedPlaceholder.collectAsState()
    val placeholders by viewModel.placeholders.collectAsState()

    var showPlaceholderSheet by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.selectedImageUri.value = uri
            viewModel.selectedPlaceholder.value = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { viewModel.title.value = it },
            label = { Text("Название мероприятия") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = announcement,
            onValueChange = { viewModel.announcement.value = it },
            label = { Text("Анонс (краткое описание)") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { viewModel.description.value = it },
            label = { Text("Полное описание") },
            minLines = 4,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Превью
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { showPlaceholderSheet = true },
            contentAlignment = Alignment.Center
        ) {
            when {
                selectedImageUri != null -> {
                    SubcomposeAsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                selectedPlaceholder != null -> {
                    SubcomposeAsyncImage(
                        model = "${BuildConfig.BASE_URL.trimEnd('/')}/api/v/1/files/events-placeholders/$selectedPlaceholder",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Нажмите, чтобы выбрать изображение",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text("С устройства") }
            OutlinedButton(
                onClick = { showPlaceholderSheet = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Плейсхолдер") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = title.isNotBlank() && announcement.isNotBlank() && description.isNotBlank()
        ) {
            Text("Продолжить", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showPlaceholderSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPlaceholderSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(
                    text = "Выберите плейсхолдер",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (placeholders.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp)
                    ) {
                        items(placeholders) { key ->
                            val isSelected = selectedPlaceholder == key
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1.5f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                                        else BorderStroke(0.dp, Color.Transparent),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        viewModel.selectedPlaceholder.value = key
                                        viewModel.selectedImageUri.value = null
                                        showPlaceholderSheet = false
                                    }
                            ) {
                                SubcomposeAsyncImage(
                                    model = "${BuildConfig.BASE_URL.trimEnd('/')}/api/v/1/files/events-placeholders/$key",
                                    contentDescription = key,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    loading = {
                                        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}