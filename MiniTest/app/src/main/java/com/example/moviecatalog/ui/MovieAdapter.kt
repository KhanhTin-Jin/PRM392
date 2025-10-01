package com.example.moviecatalog.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moviecatalog.R
import com.example.moviecatalog.databinding.ItemMovieBinding
import com.example.moviecatalog.model.Movie

class MovieAdapter(
    private val items: MutableList<Movie> = mutableListOf()
) : RecyclerView.Adapter<MovieAdapter.VH>() {

    fun submit(list: List<Movie>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    inner class VH(val b: ItemMovieBinding): RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]
        h.b.tvTitle.text = item.title
        h.b.tvYear.text = item.year.toString()

        Glide.with(h.b.imgPoster)
            .load(item.posterUrl)
            .placeholder(R.drawable.ic_movie_24)
            .error(R.drawable.ic_movie_24)
            .timeout(60000) // 60 seconds timeout
            .centerCrop()
            .into(h.b.imgPoster)

        h.b.root.setOnClickListener {
            Toast.makeText(h.b.root.context, item.title, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = items.size
}

