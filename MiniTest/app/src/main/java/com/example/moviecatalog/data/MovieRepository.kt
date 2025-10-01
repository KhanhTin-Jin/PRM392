package com.example.moviecatalog.data

import com.example.moviecatalog.model.Movie

object MovieRepository {
    fun getMovies() = listOf(
        Movie("Inception", 2010, "https://picsum.photos/seed/inception/600/900"),
        Movie("Interstellar", 2014, "https://picsum.photos/seed/interstellar/600/900"),
        Movie("The Dark Knight", 2008, "https://picsum.photos/seed/darkknight/600/900"),
        Movie("Avatar", 2009, "https://picsum.photos/seed/avatar/600/900"),
        Movie("The Matrix", 1999, "https://picsum.photos/seed/matrix/600/900"),
        Movie("Parasite", 2019, "https://picsum.photos/seed/parasite/600/900"),
        Movie("Joker", 2019, "https://picsum.photos/seed/joker/600/900"),
        Movie("La La Land", 2016, "https://picsum.photos/seed/lalaland/600/900"),
        Movie("Titanic", 1997, "https://picsum.photos/seed/titanic/600/900"),
        Movie("Whiplash", 2014, "https://picsum.photos/seed/whiplash/600/900")
    )
}
