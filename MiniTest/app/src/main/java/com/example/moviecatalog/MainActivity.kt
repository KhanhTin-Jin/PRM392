package com.example.moviecatalog

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.moviecatalog.data.MovieRepository
import com.example.moviecatalog.databinding.ActivityMainBinding
import com.example.moviecatalog.ui.MovieAdapter
import com.example.moviecatalog.ui.SpaceItemDecoration

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val adapter = MovieAdapter()

    private val spanList by lazy { resources.getInteger(R.integer.span_list) }
    private val spanGrid by lazy { resources.getInteger(R.integer.span_grid) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // 1) Setup toolbar as action bar
        setSupportActionBar(b.topAppBar)
        supportActionBar?.title = "Movie Catalog"

        // 2) Setup RecyclerView
        b.rvMovies.setHasFixedSize(true)
        b.rvMovies.layoutManager = GridLayoutManager(this, spanList)
        b.rvMovies.adapter = adapter

        // 3) Load data
        val data = MovieRepository.getMovies()
        adapter.submit(data)
        Log.d("MovieCatalog", "Items loaded: ${data.size}, spanList: $spanList, spanGrid: $spanGrid")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        Log.d("MovieCatalog", "Menu created")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_list -> {
                b.rvMovies.layoutManager = GridLayoutManager(this, spanList)
                Log.d("MovieCatalog", "Switched to List View (span: $spanList)")
                true
            }
            R.id.action_grid -> {
                b.rvMovies.layoutManager = GridLayoutManager(this, spanGrid)
                Log.d("MovieCatalog", "Switched to Grid View (span: $spanGrid)")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}


