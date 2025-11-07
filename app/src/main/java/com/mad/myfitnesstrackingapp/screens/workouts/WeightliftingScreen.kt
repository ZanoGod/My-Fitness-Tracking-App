package com.mad.myfitnesstrackingapp.screens.workouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mad.myfitnesstrackingapp.R
import com.mad.myfitnesstrackingapp.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import com.mad.myfitnesstrackingapp.screens.TopToastController
import com.mad.myfitnesstrackingapp.screens.TopToastHost
import com.mad.myfitnesstrackingapp.screens.TopToastType


private data class LiftSet(val weightKg: String, val reps: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightliftingScreen(navController: NavController) {
    // coroutine scope
    val coroutineScope = rememberCoroutineScope()

    // Input states
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }

    // List of sets (mutable state list)
    val sets = remember { mutableStateListOf<LiftSet>() }

    // derived metrics
    val totalSets = sets.size
    val totalVolume = remember(sets) {
        sets.sumOf {
            val w = it.weightKg.toDoubleOrNull() ?: 0.0
            val r = it.reps.toIntOrNull() ?: 0
            (w * r)
        }
    }

    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Weightlifting",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Show top toast and then return
                    TopToastController.show("Workout saved", TopToastType.SUCCESS)
                    coroutineScope.launch {
                        // small delay so user sees the toast
                        delay(700)
                        navController.popBackStack()
                    }
                },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp),
            ) {
                Text("Save Workout")
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GradientTop, GradientBottom)))
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // TopToast host placed under status bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarTopPadding + 8.dp)
                    .align(Alignment.TopCenter)
            ) {
                TopToastHost()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header card with icon + quick stats
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TextFieldBackground.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_weight_24px),
                                contentDescription = "Weight Icon",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Weightlifting",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                "$totalSets sets • Volume ${"%.0f".format(totalVolume)} kg",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        TextButton(onClick = {
                            sets.clear()
                            TopToastController.show("Sets cleared", TopToastType.INFO)
                        }, colors = ButtonDefaults.textButtonColors(contentColor = AccentBlue)) {
                            Text("Clear")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Input card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TextFieldBackground.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Weight input
                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it },
                                placeholder = { Text("Weight(kg)", color = Color.White.copy(alpha = 0.6f)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(color = Color.White),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )

                            // Reps input
                            OutlinedTextField(
                                value = reps,
                                onValueChange = { reps = it },
                                placeholder = { Text("Reps", color = Color.White.copy(alpha = 0.6f)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(color = Color.White),
                                modifier = Modifier.width(80.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Add Set button
                            Button(
                                onClick = {
                                    val wOk = weight.isNotBlank() && weight.toDoubleOrNull() != null
                                    val rOk = reps.isNotBlank() && reps.toIntOrNull() != null
                                    if (wOk && rOk) {
                                        sets.add(LiftSet(weightKg = weight.trim(), reps = reps.trim()))
                                        weight = ""
                                        reps = ""
                                        TopToastController.show("Set added", TopToastType.SUCCESS)
                                    } else {
                                        TopToastController.show("Enter numeric weight and reps", TopToastType.INFO)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Add", color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sets list
                Text("Sets", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                if (sets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        Text("No sets yet — add your first set", color = Color.White.copy(alpha = 0.7f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(sets) { index, set ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = TextFieldBackground.copy(alpha = 0.45f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Set ${index + 1}", color = Color.White, fontWeight = FontWeight.SemiBold)
                                        Text("${set.weightKg} kg × ${set.reps} reps", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                                    }

                                    IconButton(onClick = {
                                        sets.removeAt(index)
                                        TopToastController.show("Set removed", TopToastType.INFO)
                                    }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete set", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick summary row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total sets: $totalSets", color = Color.White.copy(alpha = 0.9f))
                    Text("Volume: ${"%.0f".format(totalVolume)} kg", color = Color.White.copy(alpha = 0.9f))
                }

                Spacer(modifier = Modifier.height(72.dp)) // provide room for FAB
            }
        }
    }
}
