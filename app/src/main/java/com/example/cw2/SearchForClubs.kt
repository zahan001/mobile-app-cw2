package com.example.cw2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
    //val context = LocalContext.current
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) } // Error message

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField( // Text field for entering search text
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Enter Search Text") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Button for start search
        Button(
            onClick = {
                if (searchText.isNotEmpty()) { // Check if search text is not empty
                    scope.launch {
                        searchResult = searchClubs(database, searchText)
                        showError = searchResult.isEmpty()
                        errorMessage = if (showError) "No results found."
                        else null
                    }
                } else {
                    // set error message if search text is empty
                    errorMessage = "Please insert a search term!"
                }
            }
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            // Display error message
            Text(
                it,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black
            )
        }

        if (showError) { // Show a message if there is no results found
            Text(
                "No results found.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else { // Display search results in a lazy column
            LazyColumn {
                items(searchResult) { club ->
                    ClubListItem(club = club, database = database)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// composable function to represent each item in the list of search results
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

    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(8.dp)
            .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(
                4.dp))
            .padding(8.dp)
            .fillMaxWidth()) {
        clubLogo?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Club Logo",
                modifier = Modifier.size(48.dp) // Set the size of logo to show
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(club.name, modifier = Modifier // displays the club name along with its logo in a row
            .weight(1f)
            .padding(vertical = 8.dp))
    }
}

// Function to fetching a bitmap from a given URL asynchronously
suspend fun fetchBitmapFromUrl(url: String): Bitmap {
    return withContext(Dispatchers.IO) {
        val inputStream = URL(url).openStream()
        BitmapFactory.decodeStream(inputStream) // opens an input stream from the URL and decodes it into a bitmap using BitmapFactory.
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

// Function to search the database for clubs based on the provided search text.
suspend fun searchClubs(database: AppDatabase, searchText: String): List<Club> {
    return withContext(Dispatchers.IO) { //  performs the database query in the IO dispatcher to avoid blocking the main thread
        database.clubDao().searchClubs(searchText)
    }
}
