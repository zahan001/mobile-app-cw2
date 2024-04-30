package com.example.cw2

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.cw2.ui.theme.CW2Theme
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.withContext



class SearchForClubsByLeague : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GUI()
        }
    }
}

/**
 * Composable function to display the GUI.
 */

@Composable
fun GUI() {
    var clubInfoDisplay by rememberSaveable { mutableStateOf("") }
    var leagueName by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // List of league name suggestions
    val leagueSuggestions = listOf(
        "English Premier League",
        "German Bundesliga",
        "Scottish Premier League",
        "Italian Serie A",
        "Spanish La Liga",
        "Dutch Eredivisie",
        "Belgian First Division A",
        "Turkish Super Lig",
        "Danish Superliga",
        "Portuguese Primeira Liga",
        "American Major League Soccer",
        "Swedish Allsvenskan",
        "Australian A-League",
        "Norwegian Eliteserien",
        "Chinese Super League",
        "Mexican Primera League",
        "Brazilian Serie A",
        "Russian Football Premier League",
    )
    var expanded by remember { mutableStateOf(false) } // State to track dropdown menu visibility


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Text field to enter the football league name
        TextField(
            value = leagueName,
            onValueChange = { leagueName = it },
            label = { Text("Enter Football League Name") },

        )
        // Row containing buttons of Retrieve clubs and Save Clubs to Database
        Row {
            Button(onClick = {  // Button to retrieve clubs
                scope.launch {
                    clubInfoDisplay = fetchClubs(leagueName)
                }
            }) {
                Text("Retrieve Clubs")
            }

            // Retrieve the context outside of the coroutine scope
            val context = LocalContext.current

            Button(onClick = { // Button to save clubs to the database
                //val context = LocalContext.current
                scope.launch {
                    saveClubsToDatabase(context, clubInfoDisplay)
                }

            }) {
                Text("Save Clubs to Database")
            }
        }
        // Text to display club information
        Text(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            text = clubInfoDisplay
        )
    }
}

suspend fun fetchClubs(leagueName: String): String {
    if (leagueName.isEmpty()) { // Check if the league name is empty
        return "Please enter a football league name."
    }

    val formattedLeagueName = URLEncoder.encode(leagueName, "UTF-8") // Format the league name for the URL
    val url_string = "https://www.thesportsdb.com/api/v1/json/3/search_all_teams.php?l=$formattedLeagueName"
    val url = URL(url_string)
    val con: HttpURLConnection = url.openConnection() as HttpURLConnection
    var clubInfo = StringBuilder()

    try { // Perform network operations in a background thread
        withContext(Dispatchers.IO) {
            val bf = BufferedReader(InputStreamReader(con.inputStream))
            val response = bf.use { it.readText() }
            val jsonObject = JSONObject(response)

            val teamsArray = jsonObject.getJSONArray("teams")

            // Iterate through the array of teams
            for (i in 0 until teamsArray.length()) {
                val team = teamsArray.getJSONObject(i)
                // Append club information to the StringBuilder
                clubInfo.append("Club Name: ${team.getString("strTeam")}\n")
                clubInfo.append("Short Name: ${team.getString("strTeamShort")}\n")
                clubInfo.append("Alternate Names: ${team.getString("strAlternate")}\n")
                clubInfo.append("Formed Year: ${team.getString("intFormedYear")}\n")
                clubInfo.append("League: ${team.getString("strLeague")}\n")
                clubInfo.append("Stadium: ${team.getString("strStadium")}\n")
                clubInfo.append("Keywords: ${team.getString("strKeywords")}\n")
                clubInfo.append("Stadium Location: ${team.getString("strStadiumLocation")}\n")
                clubInfo.append("Stadium Capacity: ${team.getString("intStadiumCapacity")}\n")
                clubInfo.append("Website: ${team.getString("strWebsite")}\n")
                clubInfo.append("Team Jersey: ${team.getString("strTeamJersey")}\n")
                clubInfo.append("Team Logo: ${team.getString("strTeamLogo")}\n")
                clubInfo.append("\n")
            }
        }
    } catch (e: JSONException) { // Catch the specific exception for invalid league name
        return "Please enter a valid football league name." // Prompt user to input correct league name with a message

    } catch (e: Exception) {
        e.printStackTrace()
        return "Error occurred: ${e.message}"
    } finally {
        con.disconnect()
    }

    return clubInfo.toString()
}


/*fun parseJSON(response: String): String {
    // this contains the full JSON returned by the Web Service
    val jsonObject = JSONObject(response)
    val teamsArray = jsonObject.getJSONArray("teams")

    val allClubs = StringBuilder()

    for (i in 0 until teamsArray.length()) {
        val club = teamsArray.getJSONObject(i)
        allClubs.append("Club Name: ${club.getString("strTeam")}\n")
        allClubs.append("Short Name: ${club.getString("strTeamShort")}\n")
        allClubs.append("Alternate Names: ${club.getString("strAlternate")}\n")
        allClubs.append("Formed Year: ${club.getString("intFormedYear")}\n")
        allClubs.append("Stadium: ${club.getString("strStadium")}\n")
        allClubs.append("Stadium Location: ${club.getString("strStadiumLocation")}\n")
        allClubs.append("Stadium Capacity: ${club.getString("intStadiumCapacity")}\n")
        allClubs.append("Website: ${club.getString("strWebsite")}\n\n")
    }

    return allClubs.toString()
}*/

@Entity(tableName = "clubs")
data class Club(
    @PrimaryKey val id: String,
    val name: String,
    val shortName: String,
    val alternateNames: String,
    val formedYear: String,
    val league: String,
    val stadium: String,
    val keywords: String,
    val stadiumLocation: String,
    val stadiumCapacity: String,
    val website: String,
    val teamJersey: String,
    val teamLogo: String
)

@Dao
interface ClubDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(club: Club)
}

@Database(entities = [Club::class], version = 1)
abstract class ClubDatabase : RoomDatabase() {
    abstract fun clubDao(): ClubDao
}

suspend fun saveClubsToDatabase(context: Context, leagueName: String) {
    if (leagueName.isEmpty()) {
        Log.e("SaveClubsToDatabase", "League name is empty.")
        return
    }

    val formattedLeagueName = URLEncoder.encode(leagueName, "UTF-8")
    val url = URL("https://www.thesportsdb.com/api/v1/json/1/search_all_teams.php?l=$formattedLeagueName")
    val con: HttpURLConnection = url.openConnection() as HttpURLConnection

    try {
        withContext(Dispatchers.IO) {
            val inputStream = con.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(response)

            val teamsArray = jsonObject.getJSONArray("teams")

            val database = Room.databaseBuilder(
                context.applicationContext,
                ClubDatabase::class.java, "club-database"
            ).build()

            val clubDao = database.clubDao()

            for (i in 0 until teamsArray.length()) {
                val team = teamsArray.getJSONObject(i)
                val club = Club(
                    id = team.getString("idTeam"),
                    name = team.getString("strTeam"),
                    shortName = team.getString("strTeamShort"),
                    alternateNames = team.getString("strAlternate"),
                    formedYear = team.getString("intFormedYear"),
                    league = team.getString("strLeague"),
                    stadium = team.getString("strStadium"),
                    keywords = team.getString("strKeywords"),
                    stadiumLocation = team.getString("strStadiumLocation"),
                    stadiumCapacity = team.getString("intStadiumCapacity"),
                    website = team.getString("strWebsite"),
                    teamJersey = team.getString("strTeamJersey"),
                    teamLogo = team.getString("strTeamLogo")
                )
                clubDao.insert(club)
            }
        }
    } catch (e: JSONException) {
        Log.e("SaveClubsToDatabase", "Error parsing JSON: ${e.message}")
    } catch (e: Exception) {
        Log.e("SaveClubsToDatabase", "Error occurred: ${e.message}")
    } finally {
        con.disconnect()
    }
}