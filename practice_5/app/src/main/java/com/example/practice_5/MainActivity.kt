@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.practice_5

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.example.practice_5.ui.theme.Practice_5Theme
import kotlinx.coroutines.launch
import androidx.navigation.compose.*

data class Follower(
    val id: Int,
    val name: String,
    val role: String,
    val avatar: Int,
    var isFollowing: Boolean
)
data class Story(val id: Int, val avatar: Int, val name: String)

class ProfileViewModel : ViewModel() {
    var name by mutableStateOf("Aldiyar Dinislam"); private set
    var bio by mutableStateOf("Student • Front-end & Kotlin enthusiast"); private set

    private val _followers = mutableStateListOf(
        Follower(1, "Aruzhan", "Student", R.drawable.avatar, true),
        Follower(2, "Dias", "Designer", R.drawable.avatar, false),
        Follower(3, "Ayan", "Developer", R.drawable.avatar, true),
        Follower(4, "Alina", "Musician", R.drawable.avatar, false),
        Follower(5, "Miras", "Photographer", R.drawable.avatar, true)
    )
    val followers: List<Follower> get() = _followers

    fun updateName(newName: String) { name = newName }
    fun updateBio(newBio: String) { bio = newBio }

    fun toggleFollow(id: Int) {
        val idx = _followers.indexOfFirst { it.id == id }
        if (idx >= 0) _followers[idx] = _followers[idx].copy(isFollowing = !_followers[idx].isFollowing)
    }

    fun addFollower(name: String, role: String, avatarRes: Int = R.drawable.avatar) {
        val nextId = (if (_followers.isEmpty()) 1 else (_followers.maxOf { it.id } + 1))
        _followers.add(Follower(nextId, name, role, avatarRes, false))
    }

    fun removeFollower(id: Int): Follower? {
        val idx = _followers.indexOfFirst { it.id == id }
        return if (idx >= 0) _followers.removeAt(idx) else null
    }

    fun insertFollowerAt(follower: Follower, index: Int? = null) {
        if (index == null || index < 0 || index > _followers.size) _followers.add(follower)
        else _followers.add(index, follower)
    }
}

class MainActivity : ComponentActivity() {
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Practice_5Theme {
                val vm = profileViewModel
                val snackbarHostState = remember { SnackbarHostState() }
                val navController = rememberNavController()

                Scaffold(
                    topBar = { SimpleTopBar(title = "Practice_5") },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(innerPadding)
                    ) {
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") {
                                HomeScreen(onOpenProfile = { navController.navigate("profile") })
                            }
                            composable("profile") {
                                ProfileScreen(
                                    viewModel = vm,
                                    snackbarHostState = snackbarHostState,
                                    onEditProfile = { navController.navigate("edit") }
                                )
                            }
                            composable("edit") {
                                EditProfileScreen(
                                    viewModel = vm,
                                    snackbarHostState = snackbarHostState,
                                    onDone = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleTopBar(title: String) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.height(56.dp), contentAlignment = Alignment.CenterStart) {
            Text(text = title, modifier = Modifier.padding(start = 16.dp), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun HomeScreen(onOpenProfile: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onOpenProfile) { Text("Open Profile") }
    }
}

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, snackbarHostState: SnackbarHostState, onEditProfile: () -> Unit) {
    val scope = rememberCoroutineScope()
    var removedFollower by remember { mutableStateOf<Follower?>(null) }
    val stories = remember {
        listOf(
            Story(1, R.drawable.avatar, "Aruzhan"),
            Story(2, R.drawable.avatar, "Dias"),
            Story(3, R.drawable.avatar, "Ayan"),
            Story(4, R.drawable.avatar, "Alina"),
            Story(5, R.drawable.avatar, "Miras")
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(6.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(viewModel.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(viewModel.bio, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${viewModel.followers.size}", fontWeight = FontWeight.Bold)
                        Text("Followers", fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onEditProfile) { Text("Edit") }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(stories, key = { it.id }) { story ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(story.avatar), contentDescription = null, modifier = Modifier.size(64.dp).clip(CircleShape))
                    Text(story.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Followers", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))

                AddFollowerRow { name, role ->
                    viewModel.addFollower(name, role)
                    scope.launch { snackbarHostState.showSnackbar("$name added") }
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                    items(viewModel.followers, key = { it.id }) { follower ->
                        Box(modifier = Modifier.fillMaxWidth().pointerInput(follower.id) {
                            detectDragGestures(
                                onDragEnd = {
                                    removedFollower = follower
                                    viewModel.removeFollower(follower.id)
                                    scope.launch {
                                        val res = snackbarHostState.showSnackbar(message = "${follower.name} removed", actionLabel = "Undo")
                                        if (res == SnackbarResult.ActionPerformed && removedFollower != null) {
                                            viewModel.insertFollowerAt(removedFollower!!)
                                            removedFollower = null
                                        } else removedFollower = null
                                    }
                                },
                                onDrag = { change, _ -> change.consume() }
                            )
                        }) {
                            FollowerItem(follower = follower) { id -> viewModel.toggleFollow(id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddFollowerRow(onAdd: (String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("Name") }, singleLine = true, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(value = role, onValueChange = { role = it }, placeholder = { Text("Role") }, singleLine = true, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        Button(onClick = {
            if (name.isNotBlank()) {
                onAdd(name.trim(), if (role.isBlank()) "Friend" else role.trim())
                name = ""
                role = ""
            }
        }) { Text("Add") }
    }
}

@Composable
fun FollowerItem(follower: Follower, onFollowClick: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(follower.avatar), contentDescription = null, modifier = Modifier.size(48.dp).clip(CircleShape))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(follower.name, fontSize = 16.sp)
                    Text(follower.role, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            val targetColor = if (follower.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
            val animatedColor by animateColorAsState(targetColor, animationSpec = tween(350))

            Button(onClick = { onFollowClick(follower.id) }, colors = ButtonDefaults.buttonColors(containerColor = animatedColor)) {
                Text(if (follower.isFollowing) "Unfollow" else "Follow", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun EditProfileScreen(viewModel: ProfileViewModel, snackbarHostState: SnackbarHostState, onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf(viewModel.name) }
    var bio by rememberSaveable { mutableStateOf(viewModel.bio) }

    LaunchedEffect(name) { viewModel.updateName(name) }
    LaunchedEffect(bio) { viewModel.updateBio(bio) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Edit Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { scope.launch { snackbarHostState.showSnackbar("Profile updated") }; onDone() }) { Text("Save") }
            OutlinedButton(onClick = { onDone() }) { Text("Cancel") }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun PreviewProfile() {
    Practice_5Theme {
        val vm = ProfileViewModel()
        ProfileScreen(viewModel = vm, snackbarHostState = SnackbarHostState(), onEditProfile = {})
    }
}
