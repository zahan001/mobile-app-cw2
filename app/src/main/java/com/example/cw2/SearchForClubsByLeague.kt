package com.example.cw2

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.room.Query
import kotlinx.coroutines.withContext



class SearchForClubsByLeague : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the database
        val database = AppDatabase.getDatabase(applicationContext)

        setContent {
            GUI(database)
        }
    }
}

/**
 * Composable function to display the GUI.
 */

@Composable
fun GUI(database: AppDatabase) {
    var clubInfoDisplay by rememberSaveable { mutableStateOf("") }
    var leagueName by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = leagueName,
            onValueChange = { leagueName = it },
            label = { Text("Enter Football League Name") },
        )
        Row {
            Button(onClick = {
                scope.launch {
                    clubInfoDisplay = fetchClubs(leagueName)
                }
            }) {
                Text("Retrieve Clubs")
            }

            val context = LocalContext.current

            Button(onClick = {
                scope.launch {
                    val clubsList = parseClubInfo(clubInfoDisplay)
                    if (clubsList.isNotEmpty()){
                        withContext(Dispatchers.IO){
                            // Insert all clubs to the database
                            database.clubDao().insertAll(clubsList)
                        }
                        Toast.makeText(context, "Clubs added to DB", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No clubs to add", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text("Save Clubs to Database")
            }
        }
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
    @Query("SELECT * FROM clubs")
    suspend fun getAll(): List<Club>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(club: Club)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(clubs: List<Club>)

    @Query("SELECT * FROM clubs WHERE id = :id")
    suspend fun getClubById(id: String): Club?
}


/*@Database(entities = [Club::class], version = 1)
abstract class ClubDatabase : RoomDatabase() {
    abstract fun clubDao(): ClubDao

    companion object{
        @Volatile
        private var INSTANCE: ClubDatabase? = null

        fun getDatabase(context: Context): ClubDatabase{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClubDatabase::class.java,
                    "clubs_by_league.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}*/

suspend fun parseClubInfo(clubInfo: String): List<Club> {
    val clubsList = mutableListOf<Club>()

    // Split the clubInfo string into individual club information
    val clubsInfoArray = clubInfo.split("\n\n")

    for (clubInfoEntry in clubsInfoArray) {
        // Split each club information into lines
        val lines = clubInfoEntry.split("\n")

        // Extract club details from each line
        var id = ""
        var name = ""
        var shortName = ""
        var alternateNames = ""
        var formedYear = ""
        var league = ""
        var stadium = ""
        var keywords = ""
        var stadiumLocation = ""
        var stadiumCapacity = ""
        var website = ""
        var teamJersey = ""
        var teamLogo = ""

        for (line in lines) {
            val parts = line.split(": ")
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim()

                when (key) {
                    "Club Name" -> name = value
                    "Short Name" -> shortName = value
                    "Alternate Names" -> alternateNames = value
                    "Formed Year" -> formedYear = value
                    "League" -> league = value
                    "Stadium" -> stadium = value
                    "Keywords" -> keywords = value
                    "Stadium Location" -> stadiumLocation = value
                    "Stadium Capacity" -> stadiumCapacity = value
                    "Website" -> website = value
                    "Team Jersey" -> teamJersey = value
                    "Team Logo" -> teamLogo = value
                }
            }
        }

        // Generate a unique ID for the club (you can use UUID.randomUUID().toString())
        id = "$name-$league"

        // Create a Club object and add it to the list
        val club = Club(
            id,
            name,
            shortName,
            alternateNames,
            formedYear,
            league,
            stadium,
            keywords,
            stadiumLocation,
            stadiumCapacity,
            website,
            teamJersey,
            teamLogo
        )
        clubsList.add(club)
    }

    return clubsList
}