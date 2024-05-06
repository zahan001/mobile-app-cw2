// Student Name: Sahan Madhawa Jayaweera | UoW Id: w2002471/20024712 | IIT No: 20220255

// Link to the Demo Video : https://drive.google.com/file/d/1m2xjUZPzakDJ_Gsa1rIETvERAI9Qonm7/view?usp=sharing

package com.example.cw2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.cw2.ui.theme.CW2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {
                    val navigate = Intent(this@MainActivity, AddToDB::class.java)
                    startActivity(navigate) // using intent to navigate to the Add to DB button
                }) {
                    Text(text = "Add Leagues to DB", fontSize = 18.sp)
                }

                Button(onClick = {
                    val navigate = Intent(this@MainActivity, SearchForClubsByLeague::class.java)
                    startActivity(navigate) // using intent to navigate to the search clubs by league
                }) {
                    Text(text = "Search for clubs by league", fontSize = 18.sp)
                }

                Button(onClick = {
                    val navigate = Intent(this@MainActivity, SearchForClubs::class.java)
                    startActivity(navigate) // using intent to navigate to the search for clubs
                }) {
                    Text(text = "Search for clubs", fontSize = 18.sp)
                }

            }
        }
    }
}