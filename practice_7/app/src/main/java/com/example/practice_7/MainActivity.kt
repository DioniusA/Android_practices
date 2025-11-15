@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.practice_7

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.practice_7.ui.theme.Practice_7Theme
import com.example.practice_7.data.UserRepository
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import retrofit2.http.GET

data class Follower(val id: Int, val name: String, val role: String, val avatar: Int, var isFollowing: Boolean)
data class Story(val id: Int, val avatar: Int, val name: String)
data class Post(val id: Int, val author: String, val content: String, val avatar: Int, var likes: Int, var comments: Int, var isLiked: Boolean)

@Entity(tableName = "user_table")
data class UserEntity(@PrimaryKey val id: Int = 1, val name: String, val bio: String)

@Dao
interface UserDao {
    @Query("SELECT * FROM user_table LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
}

@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

data class ApiUser(val id: Int, val name: String, val username: String)

interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<ApiUser>
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    var name by mutableStateOf("Aldiyar Dinislam")
    var bio by mutableStateOf("Student • Front-end & Kotlin enthusiast")
    private val _followers = mutableStateListOf<Follower>()
    val followers: List<Follower> get() = _followers

    private val baseFollowers = listOf(
        Follower(1, "Aruzhan", "Student", R.drawable.avatar, true),
        Follower(2, "Dias", "Designer", R.drawable.avatar, false),
        Follower(3, "Ayan", "Developer", R.drawable.avatar, true),
        Follower(4, "Alina", "Musician", R.drawable.avatar, false),
        Follower(5, "Miras", "Photographer", R.drawable.avatar, true)
    )

    fun updateName(newName: String) { name = newName }
    fun updateBio(newBio: String) { bio = newBio }
    fun toggleFollow(id: Int) {
        val idx = _followers.indexOfFirst { it.id == id }
        if (idx >= 0) _followers[idx] = _followers[idx].copy(isFollowing = !_followers[idx].isFollowing)
    }

    fun addFollower(name: String, role: String, avatarRes: Int = R.drawable.avatar) {
        val nextId = (_followers.maxOfOrNull { it.id } ?: 0) + 1
        _followers.add(Follower(nextId, name, role, avatarRes, false))
    }

    fun removeFollower(id: Int): Follower? {
        val idx = _followers.indexOfFirst { it.id == id }
        return if (idx >= 0) _followers.removeAt(idx) else null
    }

    fun insertFollowerAt(follower: Follower, index: Int? = null) {
        if (index == null || index !in 0.._followers.size) _followers.add(follower) else _followers.add(index, follower)
    }

    fun loadLocalOnce() {
        viewModelScope.launch {
            try {
                userRepository.getUser()?.let { user ->
                    name = user.name
                    bio = user.bio
                } ?: userRepository.insertUser(UserEntity(name = name, bio = bio))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveUser() {
        viewModelScope.launch {
            try {
                userRepository.insertUser(UserEntity(name = name, bio = bio))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshFromApi(onFinish: () -> Unit) {
        viewModelScope.launch {
            try {
                val apiUsers = userRepository.getUsersFromApi()

                _followers.clear()
                _followers.addAll(baseFollowers)
                apiUsers.take(10).forEach {
                    val nextId = (_followers.maxOfOrNull { f -> f.id } ?: 0) + 1
                    _followers.add(Follower(nextId, it.name, "Friend", R.drawable.avatar, false))
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onFinish()
            }
        }
    }
}

@HiltViewModel
class FeedsViewModel @Inject constructor() : ViewModel() {
    private val _posts = mutableStateListOf<Post>()
    val posts: List<Post> get() = _posts

    init {
        _posts.addAll(
            listOf(
                Post(1, "Aruzhan", "Beautiful sunset today! 🌅", R.drawable.avatar, 42, 5, false),
                Post(2, "Dias", "Working on a new design project. Can't wait to share it!", R.drawable.avatar, 28, 3, true),
                Post(3, "Ayan", "Just released a new feature. Check it out!", R.drawable.avatar, 67, 12, false),
                Post(4, "Alina", "New song coming soon! 🎵", R.drawable.avatar, 89, 15, true),
                Post(5, "Miras", "Captured this amazing moment during my photo walk.", R.drawable.avatar, 134, 23, false)
            )
        )
    }

    fun toggleLike(postId: Int) {
        val idx = _posts.indexOfFirst { it.id == postId }
        if (idx >= 0) {
            val post = _posts[idx]
            _posts[idx] = post.copy(
                isLiked = !post.isLiked,
                likes = if (post.isLiked) post.likes - 1 else post.likes + 1
            )
        }
    }

    fun addComment(postId: Int) {
        val idx = _posts.indexOfFirst { it.id == postId }
        if (idx >= 0) {
            val post = _posts[idx]
            _posts[idx] = post.copy(comments = post.comments + 1)
        }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Practice_7Theme {
                val snackbarHostState = remember { SnackbarHostState() }
                val navController = rememberNavController()
                val profileViewModel: ProfileViewModel = hiltViewModel()
                val feedsViewModel: FeedsViewModel = hiltViewModel()
                
                LaunchedEffect(Unit) {
                    profileViewModel.loadLocalOnce()
                    profileViewModel.refreshFromApi { }
                }
                Scaffold(
                    topBar = { SimpleTopBar("Practice_7") },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(innerPadding)
                    ) {
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") { 
                                HomeScreen(
                                    onOpenProfile = { navController.navigate("profile") },
                                    onOpenFeeds = { navController.navigate("feeds") }
                                )
                            }
                            composable("profile") {
                                ProfileScreen(profileViewModel, snackbarHostState) { navController.navigate("edit") }
                            }
                            composable("edit") {
                                EditProfileScreen(profileViewModel, snackbarHostState) { navController.popBackStack() }
                            }
                            composable("feeds") {
                                FeedsScreen(feedsViewModel, snackbarHostState)
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
    Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.height(56.dp), contentAlignment = Alignment.CenterStart) {
            Text(title, Modifier.padding(start = 16.dp), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun HomeScreen(onOpenProfile: () -> Unit, onOpenFeeds: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onOpenProfile) { Text("Open Profile") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onOpenFeeds) { Text("Open Feeds") }
    }
}

@Composable
fun SwipeToDeleteItem(
    follower: Follower,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isHorizontal by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(follower.id) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val startX = down.position.x
                    val startY = down.position.y
                    isHorizontal = false
                    
                    try {
                        drag(down.id) { change ->
                            val dx = change.position.x - startX
                            val dy = change.position.y - startY

                            if (!isHorizontal) {
                                if (kotlin.math.abs(dy) > kotlin.math.abs(dx)) {
                                    return@drag
                                }
                                if (kotlin.math.abs(dx) > 8.dp.toPx()) {
                                    isHorizontal = true
                                }
                            }

                            if (isHorizontal) {
                                change.consume()
                                offsetX = dx
                            }
                        }
                    } catch (e: Exception) {
                        // Drag cancelled
                    }

                    if (isHorizontal) {
                        val threshold = 150f
                        if (kotlin.math.abs(offsetX) > threshold) {
                            onDelete()
                        } else {
                            offsetX = 0f
                        }
                    }
                }
            }
    ) {
        Box(modifier = Modifier.offset { IntOffset(offsetX.toInt(), 0) }) {
            content()
        }
    }
}



@Composable
fun ProfileScreen(viewModel: ProfileViewModel, snackbarHostState: SnackbarHostState, onEditProfile: () -> Unit) {
    val scope = rememberCoroutineScope()
    var removedFollower by remember { mutableStateOf<Follower?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val stories = remember {
        listOf(
            Story(1, R.drawable.avatar, "Aruzhan"),
            Story(2, R.drawable.avatar, "Dias"),
            Story(3, R.drawable.avatar, "Ayan"),
            Story(4, R.drawable.avatar, "Alina"),
            Story(5, R.drawable.avatar, "Miras")
        )
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(6.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(viewModel.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(viewModel.bio, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${viewModel.followers.size}", fontWeight = FontWeight.Bold)
                        Text("Followers", fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onEditProfile, Modifier.fillMaxWidth()) { Text("Edit") }
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = {
                        if (!isRefreshing) {
                            isRefreshing = true
                            viewModel.refreshFromApi {
                                isRefreshing = false
                                scope.launch { snackbarHostState.showSnackbar("Refreshed from API") }
                            }
                        }
                    },
                    Modifier.fillMaxWidth()
                ) { Text("Refresh from API") }
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(stories, key = { it.id }) { story ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(story.avatar), null, Modifier.size(64.dp).clip(CircleShape))
                    Text(story.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        val listState = rememberLazyListState()
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text("Followers", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    AddFollowerRow { name, role ->
                        viewModel.addFollower(name, role)
                        scope.launch { snackbarHostState.showSnackbar("$name added") }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                items(viewModel.followers, key = { it.id }) { follower ->
                    SwipeToDeleteItem(
                        follower = follower,
                        onDelete = {
                            removedFollower = follower
                            viewModel.removeFollower(follower.id)

                            scope.launch {
                                val res = snackbarHostState.showSnackbar(
                                    "${follower.name} removed",
                                    "Undo"
                                )

                                if (res == SnackbarResult.ActionPerformed && removedFollower != null) {
                                    viewModel.insertFollowerAt(removedFollower!!)
                                }

                                removedFollower = null
                            }
                        }
                    ) {
                        FollowerItem(follower) { viewModel.toggleFollow(it) }
                    }
                }
            }

            val scrollFraction by remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val totalItems = layoutInfo.totalItemsCount

                    if (totalItems == 0) return@derivedStateOf 0f

                    val firstIndex = listState.firstVisibleItemIndex
                    val firstOffset = listState.firstVisibleItemScrollOffset
                    val viewportHeight = layoutInfo.viewportSize.height

                    val averageItemSize = if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                        layoutInfo.visibleItemsInfo.sumOf { it.size } / layoutInfo.visibleItemsInfo.size
                    } else {
                        1
                    }

                    val totalHeight = totalItems * averageItemSize - viewportHeight

                    ((firstIndex * averageItemSize + firstOffset).toFloat() / totalHeight)
                        .coerceIn(0f, 1f)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .align(Alignment.CenterEnd)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .offset { IntOffset(0, (scrollFraction * (listState.layoutInfo.viewportSize.height - 40)).toInt()) }
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

@Composable
fun AddFollowerRow(onAdd: (String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Name") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = role,
            onValueChange = { role = it },
            placeholder = { Text("Role") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
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
fun EditProfileScreen(viewModel: ProfileViewModel, snackbarHostState: SnackbarHostState, onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf(viewModel.name) }
    var bio by rememberSaveable { mutableStateOf(viewModel.bio) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Edit Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                viewModel.updateName(name)
                viewModel.updateBio(bio)
                viewModel.saveUser()
                scope.launch { snackbarHostState.showSnackbar("Profile updated") }
                onDone()
            }) { Text("Save") }
            OutlinedButton(onClick = { onDone() }) { Text("Cancel") }
        }
    }
}

@Composable
fun FollowerItem(follower: Follower, onFollowClick: (Int) -> Unit) {
    val animatedColor by animateColorAsState(
        targetValue = if (follower.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
        animationSpec = tween(350)
    )

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
            Button(onClick = { onFollowClick(follower.id) }, colors = ButtonDefaults.buttonColors(containerColor = animatedColor)) {
                Text(if (follower.isFollowing) "Unfollow" else "Follow", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun FeedsScreen(viewModel: FeedsViewModel, snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Feeds", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }
        items(viewModel.posts, key = { it.id }) { post ->
            val wasLiked = post.isLiked
            PostItem(
                post = post,
                onLikeClick = {
                    viewModel.toggleLike(post.id)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (wasLiked) "Unliked" else "Liked"
                        )
                    }
                },
                onCommentClick = {
                    viewModel.addComment(post.id)
                    scope.launch {
                        snackbarHostState.showSnackbar("Comment added")
                    }
                }
            )
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    val animatedLikeColor by animateColorAsState(
        targetValue = if (post.isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(post.avatar),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Text(post.author, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Text(post.content, fontSize = 14.sp, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onLikeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = animatedLikeColor.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            "❤ ${post.likes}",
                            fontSize = 14.sp,
                            color = animatedLikeColor
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onCommentClick,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("💬 ${post.comments}", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
