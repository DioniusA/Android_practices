@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.practice_6

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import com.example.practice_6.ui.theme.Practice_6Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class Follower(val id: Int, val name: String, val role: String, val avatar: Int, var isFollowing: Boolean)
data class Story(val id: Int, val avatar: Int, val name: String)

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

class ProfileViewModel(private val dao: UserDao) : ViewModel() {
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
        viewModelScope.launch(Dispatchers.IO) {
            dao.getUser()?.let { user ->
                withContext(Dispatchers.Main) {
                    name = user.name
                    bio = user.bio
                }
            } ?: dao.insertUser(UserEntity(name = name, bio = bio))
        }
    }

    fun saveUser() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertUser(UserEntity(name = name, bio = bio))
        }
    }

    fun refreshFromApi(onFinish: () -> Unit) {
        viewModelScope.launch {
            try {
                val api = Retrofit.Builder()
                    .baseUrl("https://jsonplaceholder.typicode.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)

                val apiUsers = withContext(Dispatchers.IO) { api.getUsers() }

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

class ProfileViewModelFactory(private val dao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(dao) as T
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "profile_db").build()
        val dao = db.userDao()
        val profileViewModel: ProfileViewModel by viewModels { ProfileViewModelFactory(dao) }

        setContent {
            Practice_6Theme {
                val snackbarHostState = remember { SnackbarHostState() }
                val navController = rememberNavController()
                LaunchedEffect(Unit) {
                    profileViewModel.loadLocalOnce()
                    profileViewModel.refreshFromApi { }
                }
                Scaffold(
                    topBar = { SimpleTopBar("Practice_6") },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(innerPadding)
                    ) {
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") { HomeScreen { navController.navigate("profile") } }
                            composable("profile") {
                                ProfileScreen(profileViewModel, snackbarHostState) { navController.navigate("edit") }
                            }
                            composable("edit") {
                                EditProfileScreen(profileViewModel, snackbarHostState) { navController.popBackStack() }
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
fun HomeScreen(onOpenProfile: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
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
                modifier = Modifier.fillMaxSize().padding(end = 8.dp)
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
                    Box(Modifier.fillMaxWidth().pointerInput(follower.id) {
                        detectDragGestures(
                            onDragEnd = {
                                removedFollower = follower
                                viewModel.removeFollower(follower.id)
                                scope.launch {
                                    val res = snackbarHostState.showSnackbar("${follower.name} removed", "Undo")
                                    if (res == SnackbarResult.ActionPerformed && removedFollower != null) {
                                        viewModel.insertFollowerAt(removedFollower!!)
                                    }
                                    removedFollower = null
                                }
                            },
                            onDrag = { change, _ -> change.consumeAllChanges() }
                        )
                    }) { FollowerItem(follower) { viewModel.toggleFollow(it) } }
                }
            }

            val scrollFraction by remember {
                derivedStateOf {
                    val info = listState.layoutInfo
                    if (info.totalItemsCount == 0) return@derivedStateOf 0f
                    val first = listState.firstVisibleItemIndex
                    val offset = listState.firstVisibleItemScrollOffset
                    val totalHeight = info.totalItemsCount * 100 // примерная высота всех элементов
                    ((first * 100 + offset).toFloat() / totalHeight).coerceIn(0f, 1f)
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
