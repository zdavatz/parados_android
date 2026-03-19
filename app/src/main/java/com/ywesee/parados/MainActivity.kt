package com.ywesee.parados

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var repository: GameRepository
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "PARADOS"

        repository = GameRepository(this)
        repository.ensureGamesInstalled()

        val recyclerView = findViewById<RecyclerView>(R.id.games_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = GameAdapter(GameInfo.allGames()) { filename ->
            launchGame(filename)
        }
    }

    private fun launchGame(filename: String) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("filename", filename)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_update -> {
                updateGames()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateGames() {
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE

        scope.launch {
            val updated = withContext(Dispatchers.IO) {
                repository.updateFromGithub()
            }
            progressBar.visibility = View.GONE
            Toast.makeText(
                this@MainActivity,
                if (updated > 0) "$updated Spiele aktualisiert" else "Keine Updates verfügbar",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
