package com.example.cw2
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch



class AddToDB : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the database
        val database = AppDatabase.getDatabase(applicationContext)

        /* Example usage: Inserting a league into the database
        lifecycleScope.launch {
            val leagueDao = database.leagueDao()
            val league = League("1", "English Premier League", "Soccer", "Premier League, EPL")
            leagueDao.insertLeague(league)
        }*/

        // List of hardcoded leagues
        val hardcodedLeagues = listOf(
            League("4328", "English Premier League", "Soccer", "Premier League, EPL"),
            League("4329", "English League Championship", "Soccer", "Championship"),
            League("4330", "Scottish Premier League", "Soccer", "Scottish Premiership, SPFL"),
            League("4331", "German Bundesliga", "Soccer", "Bundesliga, Fußball-Bundesliga"),
            League("4332", "Italian Serie A", "Soccer", "Serie A"),
            League("4334", "French Ligue 1", "Soccer", "Ligue 1 Conforama"),
            League("4335", "Spanish La Liga", "Soccer", "LaLiga Santander, La Liga"),
            League("4336", "Greek Superleague Greece", "Soccer", ""),
            League("4337", "Dutch Eredivisie", "Soccer", "Eredivisie"),
            League("4338", "Belgian First Division A", "Soccer", "Jupiler Pro League"),
            League("4339", "Turkish Super Lig", "Soccer", "Super Lig"),
            League("4340", "Danish Superliga", "Soccer", ""),
            League("4344", "Portuguese Primeira Liga", "Soccer", "Liga NOS"),
            League("4346", "American Major League Soccer", "Soccer", "MLS, Major League Soccer"),
            League("4347", "Swedish Allsvenskan", "Soccer", "Fotbollsallsvenskan"),
            League("4350", "Mexican Primera League", "Soccer", "Liga MX"),
            League("4351", "Brazilian Serie A", "Soccer", ""),
            League("4354", "Ukrainian Premier League", "Soccer", ""),
            League("4355", "Russian Football Premier League", "Soccer", "Чемпионат России по футболу"),
            League("4356", "Australian A-League", "Soccer", "A-League"),
            League("4358", "Norwegian Eliteserien", "Soccer", "Eliteserien"),
            League("4359", "Chinese Super League", "Soccer", "")
        )
        // Get the LeagueDao instance
        val leagueDao = database.leagueDao()

        // Insert the leagues into the local database
        lifecycleScope.launch {
            leagueDao.insertAll(hardcodedLeagues)
        }

        setContent {
            Text(text = "Hello, Added data to local database!",
                color = Color.Green)
        }
    }
}