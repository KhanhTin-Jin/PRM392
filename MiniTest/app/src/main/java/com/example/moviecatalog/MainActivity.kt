package com.example.moviecatalog

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        // 1) Dùng toolbar làm action bar
        setSupportActionBar(b.topAppBar)
        supportActionBar?.title = "Movie Catalog"

        b.topAppBar.menu.clear()
        b.topAppBar.inflateMenu(R.menu.menu_main)
        b.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_list -> { b.rvMovies.layoutManager =
                    GridLayoutManager(this, resources.getInteger(R.integer.span_list)); true }
                R.id.action_grid -> { b.rvMovies.layoutManager =
                    GridLayoutManager(this, resources.getInteger(R.integer.span_grid)); true }
                else -> false
            }
        }

        // 2) Inflate menu BẰNG CODE (tránh lỗi ID 0)
        b.topAppBar.menu.clear()
        b.topAppBar.inflateMenu(R.menu.menu_main)

        // 3) RecyclerView
        b.rvMovies.setHasFixedSize(true)
        b.rvMovies.layoutManager = GridLayoutManager(this, spanList)
        b.rvMovies.adapter = adapter

        // 4) Nạp data
        val data = MovieRepository.getMovies()
        adapter.submit(data)
        Log.d("MovieCatalog", "Items loaded: ${data.size}")

        // 5) Switch layout
        b.topAppBar.setOnMenuItemClickListener { mi ->
            when (mi.itemId) {
                R.id.action_list -> { b.rvMovies.layoutManager = GridLayoutManager(this, spanList); true }
                R.id.action_grid -> { b.rvMovies.layoutManager = GridLayoutManager(this, spanGrid); true }
                else -> false
            }
        }
    }
}


