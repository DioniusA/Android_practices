@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.practice_4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practice_4.ui.theme.Practice_4Theme
import kotlinx.coroutines.launch

data class Follower(
    val id: Int,
    val name: String,
    val role: String,
    val avatar: Int,
    var isFollowing: Boolean
)

data class Story(val id: Int, val avatar: Int, val name: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Practice_4Theme {
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
                        contentAlignment = Alignment.TopCenter
                    ) {
                        ProfileScreen(snackbarHostState)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()
    var followers by rememberSaveable {
        mutableStateOf(
            listOf(
                Follower(1, "Aruzhan", "Student", R.drawable.avatar, true),
                Follower(2, "Dias", "Designer", R.drawable.avatar, false),
                Follower(3, "Ayan", "Developer", R.drawable.avatar, true),
                Follower(4, "Alina", "Musician", R.drawable.avatar, false),
                Follower(5, "Miras", "Photographer", R.drawable.avatar, true)
            )
        )
    }
    val stories = remember {
        listOf(
            Story(1, R.drawable.avatar, "Aruzhan"),
            Story(2, R.drawable.avatar, "Dias"),
            Story(3, R.drawable.avatar, "Ayan"),
            Story(4, R.drawable.avatar, "Alina"),
            Story(5, R.drawable.avatar, "Miras")
        )
    }
    var removedFollower by remember { mutableStateOf<Follower?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stories) { story ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(story.avatar),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                    Text(story.name, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Followers", fontSize = 20.sp, color = Color.Black)
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(followers, key = { it.id }) { follower ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragEnd = {
                                            // Swipe to remove
                                            removedFollower = follower
                                            followers = followers.filter { it.id != follower.id }
                                            scope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "${follower.name} removed",
                                                    actionLabel = "Undo"
                                                )
                                                if (result == SnackbarResult.ActionPerformed && removedFollower != null) {
                                                    followers = followers + removedFollower!!
                                                    removedFollower = null
                                                }
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                        }
                                    )
                                }
                        ) {
                            FollowerItem(follower) { id ->
                                followers = followers.map {
                                    if (it.id == id) it.copy(isFollowing = !it.isFollowing) else it
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FollowerItem(follower: Follower, onFollowClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(follower.avatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(follower.name, fontSize = 16.sp, color = Color.Black)
                    Text(follower.role, fontSize = 13.sp, color = Color.Gray)
                }
            }
            Button(
                onClick = { onFollowClick(follower.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (follower.isFollowing) Color.Gray else Color(0xFF1DA1F2)
                )
            ) {
                Text(if (follower.isFollowing) "Unfollow" else "Follow", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    Practice_4Theme {
        ProfileScreen(SnackbarHostState())
    }
}
