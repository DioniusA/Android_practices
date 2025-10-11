package com.example.practice_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practice_2.ui.theme.Practice_2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Practice_2Theme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileCard()
                }
            }
        }
    }
}

@Composable
fun ProfileCard() {
    var isFollowing by rememberSaveable { mutableStateOf(false) }
    var followerCount by rememberSaveable { mutableIntStateOf(100) }
    val buttonColor by animateColorAsState(
        targetValue = if (isFollowing) Color.Gray else Color(0xFF1DA1F2),
        label = "followButtonColor"
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(Color.White, shape = RoundedCornerShape(16.dp))
            .padding(24.dp),
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
        Row(horizontalArrangement = Arrangement.Center) {
            Text("I'm a beginner backend developer", fontSize = 12.sp, color = Color.Gray)
        }
        Spacer(Modifier.height(8.dp))
        Text("Followers: $followerCount", fontSize = 14.sp, color = Color.Black)
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                isFollowing = !isFollowing
                followerCount += if (isFollowing) 1 else -1
            },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
        ) {
            Text(if (isFollowing) "Unfollow" else "Follow", color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCard() {
    Practice_2Theme {
        ProfileCard()
    }
}
