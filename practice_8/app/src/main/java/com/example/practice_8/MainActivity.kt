@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.practice_8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.composed
import androidx.compose.ui.unit.times
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
import com.example.practice_8.ui.theme.Practice_8Theme
import com.example.practice_8.data.UserRepository
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.http.GET

data class Follower(val id: Int, val name: String, val role: String, val avatar: Int, var isFollowing: Boolean)
data class Story(val id: Int, val avatar: Int, val name: String)
data class Post(val id: Int, val author: String, val content: String, val avatar: Int, var likes: Int, var comments: Int, var isLiked: Boolean)
data class TimelineEvent(val id: Int, val title: String, val subtitle: String, val time: String)

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
    var isSyncing by mutableStateOf(false)
        private set

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

    fun refreshFromApi(onFinish: (Boolean) -> Unit = {}) {
        if (isSyncing) return
        viewModelScope.launch {
            isSyncing = true
            var success = true
            val combinedFollowers = baseFollowers.map { it.copy() }.toMutableList()
            var nextId = combinedFollowers.maxOfOrNull { f -> f.id } ?: 0
            try {
                val apiUsers = userRepository.getUsersFromApi()
                apiUsers.take(10).forEach {
                    nextId += 1
                    combinedFollowers.add(Follower(nextId, it.name, "Friend", R.drawable.avatar, false))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                success = false
            } finally {
                _followers.clear()
                _followers.addAll(combinedFollowers)
                isSyncing = false
                onFinish(success)
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
            Practice_8Theme {
                val snackbarHostState = remember { SnackbarHostState() }
                val navController = rememberNavController()
                val profileViewModel: ProfileViewModel = hiltViewModel()
                val feedsViewModel: FeedsViewModel = hiltViewModel()
                
                LaunchedEffect(Unit) {
                    profileViewModel.loadLocalOnce()
                    profileViewModel.refreshFromApi { }
                }
                Scaffold(
                    topBar = { SimpleTopBar("Practice_8") },
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
    val stories = remember {
        listOf(
            Story(1, R.drawable.avatar, "Aruzhan"),
            Story(2, R.drawable.avatar, "Dias"),
            Story(3, R.drawable.avatar, "Ayan"),
            Story(4, R.drawable.avatar, "Alina"),
            Story(5, R.drawable.avatar, "Miras")
        )
    }
    val timelineEvents = remember {
        listOf(
            TimelineEvent(1, "Synced profile", "Fetched remote followers", "Just now"),
            TimelineEvent(2, "UI polish", "Tweaked hero animation", "2h ago"),
            TimelineEvent(3, "Shared post", "48 likes on new update", "Yesterday"),
            TimelineEvent(4, "Met the team", "Sprint review highlights", "Tue")
        )
    }

    val followers = viewModel.followers
    val isSyncing = viewModel.isSyncing

    var showHeader by remember { mutableStateOf(false) }
    var statsVisible by remember { mutableStateOf(false) }
    val hasFollowers = followers.isNotEmpty()

    val screenScrollState = rememberLazyListState()
    val followerListState = rememberLazyListState()

    LaunchedEffect(hasFollowers) {
        if (hasFollowers && !showHeader) {
            delay(250)
            showHeader = true
            delay(200)
            statsVisible = true
        }
    }

    val avatarPulseTransition = rememberInfiniteTransition(label = "avatarPulse")
    val pulsingScale by avatarPulseTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarScalePulse"
    )
    val avatarScale by animateFloatAsState(
        targetValue = if (isSyncing) pulsingScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "avatarScaleState"
    )

    val onSyncClick = {
        if (!viewModel.isSyncing) {
            viewModel.refreshFromApi { success ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (success) "Profile synced ✨" else "Sync failed, try again"
                    )
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = screenScrollState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            if (!showHeader) {
                ProfileHeaderShimmer()
            } else {
                ProfileHeaderCard(
                    name = viewModel.name,
                    bio = viewModel.bio,
                    followers = followers.size,
                    avatarScale = avatarScale,
                    isSyncing = isSyncing,
                    statsVisible = statsVisible,
                    onEditProfile = onEditProfile,
                    onRefresh = onSyncClick
                )
            }
        }

        item {
            StoriesSection(stories = stories)
        }

        item {
            TimelineSection(
                events = timelineEvents,
                modifier = Modifier.fillMaxWidth()
            ) { event ->
                scope.launch { snackbarHostState.showSnackbar("Timeline: ${event.title}") }
            }
        }

        item {
            FollowersSection(
                viewModel = viewModel,
                followers = followers,
                listState = followerListState,
                snackbarHostState = snackbarHostState,
                scope = scope
            )
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    name: String,
    bio: String,
    followers: Int,
    avatarScale: Float,
    isSyncing: Boolean,
    statsVisible: Boolean,
    onEditProfile: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box {
                        Image(
                            painter = painterResource(R.drawable.avatar),
                            contentDescription = null,
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .graphicsLayer {
                                    scaleX = avatarScale
                                    scaleY = avatarScale
                                }
                        )
                        StatusPulseIndicator(
                            modifier = Modifier.align(Alignment.BottomEnd).offset(4.dp, 4.dp),
                            isOnline = true,
                            isSyncing = isSyncing
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(bio, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .widthIn(min = 72.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("$followers", fontWeight = FontWeight.Bold)
                    Text("Followers", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            AnimatedVisibility(visible = statsVisible, enter = fadeIn(tween(500))) {
                ProfileStatsRow(followers = followers)
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onEditProfile, modifier = Modifier.weight(1f)) {
                    Text("Edit profile")
                }
                OutlinedButton(
                    onClick = onRefresh,
                    enabled = !isSyncing,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isSyncing) "Syncing..." else "Refresh data")
                }
            }
        }
    }
}

@Composable
private fun ProfileStatsRow(followers: Int) {
    val stats = listOf(
        "Followers" to followers,
        "Following" to (followers / 2 + 48),
        "Posts" to 68
    )
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        stats.forEach { (label, value) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatusPulseIndicator(
    modifier: Modifier = Modifier,
    isOnline: Boolean,
    isSyncing: Boolean
) {
    val transition = rememberInfiniteTransition(label = "statusPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isSyncing) 500 else 900,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "statusScale"
    )
    val scale by animateFloatAsState(
        targetValue = if (isOnline) pulse else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "statusScaleAnim"
    )

    Box(modifier = modifier.size(18.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
        )
        Box(
            modifier = Modifier
                .size(12.dp * scale)
                .clip(CircleShape)
                .background(
                    if (isSyncing) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
                .alpha(if (isOnline) 0.95f else 0.4f)
        )
    }
}

@Composable
private fun ProfileHeaderShimmer() {
    val transition = rememberInfiniteTransition(label = "headerShimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "headerShift"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim / 2f)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(Modifier.height(10.dp))
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            repeat(2) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(brush)
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun StoriesSection(stories: List<Story>) {
    Column {
        Text("Stories", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(stories, key = { it.id }) { story ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(story.avatar),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).clip(CircleShape)
                    )
                    Text(story.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun FollowersSection(
    viewModel: ProfileViewModel,
    followers: List<Follower>,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    var removedFollower by remember { mutableStateOf<Follower?>(null) }

    Column {
        Text("Followers", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        AddFollowerRow { name, role ->
            viewModel.addFollower(name, role)
            scope.launch { snackbarHostState.showSnackbar("$name added") }
        }
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp, max = 520.dp)
        ) {
            if (followers.isEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        ShimmerFollowerPlaceholder()
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(followers, key = { it.id }) { follower ->
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
                        val viewportHeight = layoutInfo.viewportSize.height.takeIf { it > 0 } ?: 1

                        val averageItemSize = if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                            layoutInfo.visibleItemsInfo.sumOf { it.size } / layoutInfo.visibleItemsInfo.size
                        } else {
                            1
                        }

                        val totalHeight = (totalItems * averageItemSize - viewportHeight).coerceAtLeast(1)

                        ((firstIndex * averageItemSize + firstOffset).toFloat() / totalHeight)
                            .coerceIn(0f, 1f)
                    }
                }

                val indicatorTravel = (listState.layoutInfo.viewportSize.height - 40).coerceAtLeast(0)

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
                            .offset { IntOffset(0, (scrollFraction * indicatorTravel).toInt()) }
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineSection(
    events: List<TimelineEvent>,
    modifier: Modifier = Modifier,
    onEventClick: (TimelineEvent) -> Unit
) {
    Column(modifier) {
        Text("Timeline", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(events, key = { _, event -> event.id }) { index, event ->
                TimelineCard(event = event, index = index, onEventClick = onEventClick)
            }
        }
    }
}

@Composable
private fun TimelineCard(
    event: TimelineEvent,
    index: Int,
    onEventClick: (TimelineEvent) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 90L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(400)),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .width(220.dp)
                .bouncyClickable { onEventClick(event) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(event.time, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Text(event.title, fontWeight = FontWeight.SemiBold)
                Text(event.subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ShimmerFollowerPlaceholder() {
    val transition = rememberInfiniteTransition(label = "followerShimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "followerShift"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim / 2f)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(brush)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(8.dp))
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
        Spacer(Modifier.width(12.dp))
        Spacer(
            modifier = Modifier
                .width(78.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(brush)
        )
    }
}

private fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .pointerInput(enabled) {
            detectTapGestures(
                onPress = {
                    if (!enabled) return@detectTapGestures
                    scale.animateTo(
                        0.92f,
                        animationSpec = spring(
                            dampingRatio = 0.35f,
                            stiffness = 320f
                        )
                    )
                    val released = tryAwaitRelease()
                    scale.animateTo(
                        1f,
                        animationSpec = spring(
                            dampingRatio = 0.55f,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    if (released && enabled) {
                        onClick()
                    }
                }
            )
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
