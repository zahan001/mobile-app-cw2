package com.example.cw2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import com.example.cw2.ui.theme.CW2Theme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cw2.Club
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class SearchForClubs : ComponentActivity() {

    private lateinit var database: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the database
        database = AppDatabase.getDatabase(applicationContext)

        setContent {
            SearchScreen(database)
        }
    }
}


@Composable
fun SearchScreen(database: AppDatabase) {
    var searchText by rememberSaveable { mutableStateOf("") }
    var searchResult by rememberSaveable { mutableStateOf<List<Club>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var showError by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Enter Search Text") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    searchResult = searchClubs(database, searchText)
                    showError = searchResult.isEmpty()
                }
            }
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showError) {
            Text(
                "No results found.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            LazyColumn {
                items(searchResult) { club ->
                    ClubListItem(club = club, database = database)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ClubListItem(club: Club, database: AppDatabase) {
    var clubLogo by rememberSaveable { mutableStateOf<Bitmap?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) } // Track errors

    LaunchedEffect(club.name) { // Use club name as the key
        try {
            // Fetch club logo asynchronously
            val logoUrl = withContext(Dispatchers.IO) {
                database.clubDao().getClubLogoUrl(club.name)
            }

            if (logoUrl != null) {
                clubLogo = fetchBitmapFromUrl(logoUrl)
            }
        } catch (e: Exception) {
            // Handle errors
            error = "Error fetching logo: ${e.message}"
        }
    }

    // Display error message if there's an error
    error?.let { errorMessage ->
        Text(errorMessage, color = Color.Red, modifier = Modifier.padding(vertical = 8.dp))
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        clubLogo?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Club Logo",
                modifier = Modifier.size(24.dp) // Set the size to a thumbnail size
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(club.name, modifier = Modifier
            .weight(1f)
            .padding(vertical = 8.dp))
    }
}


suspend fun fetchBitmapFromUrl(url: String): Bitmap {
    return withContext(Dispatchers.IO) {
        val inputStream = URL(url).openStream()
        BitmapFactory.decodeStream(inputStream)
    }
}
/*
@Composable
fun SearchScreen(database: AppDatabase) {
    var searchText by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<List<Club>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Enter Search Text") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    searchResult = searchClubs(database, searchText)
                }
            }
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            searchResult.forEach { club ->
                Text(club.name)
            }
        }
    }
}*/

suspend fun searchClubs(database: AppDatabase, searchText: String): List<Club> {
    return withContext(Dispatchers.IO) {
        database.clubDao().searchClubs(searchText)
    }
}
