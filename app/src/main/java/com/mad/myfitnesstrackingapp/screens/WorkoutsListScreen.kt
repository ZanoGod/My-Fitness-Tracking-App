package com.mad.myfitnesstrackingapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mad.myfitnesstrackingapp.R
import com.mad.myfitnesstrackingapp.navigation.NavRoute
import com.mad.myfitnesstrackingapp.ui.theme.GradientBottom
import com.mad.myfitnesstrackingapp.ui.theme.GradientTop
import com.mad.myfitnesstrackingapp.util.ActivityType

private data class ActivityItem(
    val type: ActivityType,
    val iconRes: Int,
    val subtitle: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsListScreen(navController: NavController) {
    val items = listOf(
        ActivityItem(ActivityType.Running, R.drawable.ic_running, "Start a run and record GPS route"),
        ActivityItem(ActivityType.Walking, R.drawable.ic_walking, "Track your walking sessions"),
        ActivityItem(ActivityType.Cycling, R.drawable.ic_bike_24px, "Record your rides and distance"),
        ActivityItem(ActivityType.Weightlifting, R.drawable.ic_weight_24px, "Log your sets and reps")
    )

    val cardBg = Color.White.copy(alpha = 0.06f)
    val cardShape = RoundedCornerShape(18.dp)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GradientTop, GradientBottom)))
        ) {
            // ----- Top App Bar -----
            TopAppBar(
                title = {
                    Text(
                        text = "Workouts",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            // ----- Workout Cards -----
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(92.dp)
                            // Clip first → ripple respects the rounded shape
                            .clip(cardShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
//                                indication = rememberRipple(
//                                    bounded = true,
//                                    color = Color.White.copy(alpha = 0.2f),
//                                    radius = 400.dp
//                                )
                            ) {
                                when (item.type) {
                                    ActivityType.Running -> navController.navigate(NavRoute.RUNNING)
                                    ActivityType.Walking -> navController.navigate(NavRoute.WALKING)
                                    ActivityType.Cycling -> navController.navigate(NavRoute.CYCLING)
                                    ActivityType.Weightlifting -> navController.navigate(NavRoute.WEIGHTLIFTING)
                                }
                            },
                        shape = cardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = cardBg,
                            contentColor = Color.White
                        ),
                     // elevation = CardDefaults.cardElevation(defaultElevation = 40.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = item.type.displayName,
                                modifier = Modifier.size(44.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.type.displayName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    when (item.type) {
                                        ActivityType.Running -> navController.navigate(NavRoute.RUNNING)
                                        ActivityType.Walking -> navController.navigate(NavRoute.WALKING)
                                        ActivityType.Cycling -> navController.navigate(NavRoute.CYCLING)
                                        ActivityType.Weightlifting -> navController.navigate(NavRoute.WEIGHTLIFTING)
                                    }
                                },
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                            ) {
                                Text("Open", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
