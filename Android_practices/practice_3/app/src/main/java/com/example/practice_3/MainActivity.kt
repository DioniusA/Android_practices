@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.practice_3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practice_3.ui.theme.Practice_3Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Practice_3Theme {
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    topBar = { TopAppBar(title = { Text("Profile Screen") }) },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F5F5))
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileCard(snackbarHostState)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileCard(snackbarHostState: SnackbarHostState) {
    var isFollowing by rememberSaveable { mutableStateOf(false) }
    var followerCount by rememberSaveable { mutableIntStateOf(100) }
    var showUnfollowDialog by remember { mutableStateOf(false) }
    val buttonColor by animateColorAsState(targetValue = if (isFollowing) Color.Gray else Color(0xFF1DA1F2), label = "followButtonColor")
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.avatar),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(8.dp))
            Text("Aldiyar Dinislam", fontSize = 18.sp, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Text("I'm a beginner backend developer", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text("Followers: $followerCount", fontSize = 14.sp, color = Color.Black)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (!isFollowing) {
                            isFollowing = true
                            followerCount += 1
                            scope.launch { snackbarHostState.showSnackbar("You followed Aldiyar") }
                        } else {
                            showUnfollowDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    Text(if (isFollowing) "Unfollow" else "Follow", color = Color.White)
                }
                OutlinedButton(onClick = { scope.launch { snackbarHostState.showSnackbar("Message feature not available") } }) {
                    Text("Message")
                }
            }
        }
    }

    if (showUnfollowDialog) {
        AlertDialog(
            onDismissRequest = { showUnfollowDialog = false },
            title = { Text("Unfollow") },
            text = { Text("Are you sure you want to unfollow Aldiyar?") },
            confirmButton = {
                Button(onClick = {
                    isFollowing = false
                    followerCount = (followerCount - 1).coerceAtLeast(0)
                    showUnfollowDialog = false
                    scope.launch { snackbarHostState.showSnackbar("You unfollowed Aldiyar") }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showUnfollowDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCard() {
    Practice_3Theme {
        ProfileCard(SnackbarHostState())
    }
}
