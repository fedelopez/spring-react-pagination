package com.adevinta.maweb101

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@SpringBootApplication
class MaWeb101Application(val movieRepository: MovieRepository) : CommandLineRunner {
    override fun run(vararg args: String?) {
        movieRepository
            .findAll()
            .take(100)
            .forEach(::println)
    }
}

fun main(args: Array<String>) {
    runApplication<MaWeb101Application>(*args)
}

@RestController
class MovieController(val movieRepository: MovieRepository) {

    @GetMapping("/api/movies")
    fun getMovies(@RequestParam(required = false, defaultValue = "0") page: Int): ResponsePage {
        val resultSize = 25
        val totalMovies = movieRepository.count()
        val pageRequest = PageRequest.of(page, resultSize, Sort.by("movieTitle"))
        return ResponsePage(totalMovies, movieRepository.findAll(pageRequest).toList())
    }

    data class ResponsePage(val totalMovies: Long, val movies: List<Movie>)
}

@Repository
interface MovieRepository : PagingAndSortingRepository<Movie, Long>

data class Movie(
    @Id
    val id: Long,
    val color: String? = null,
    val directorName: String? = null,
    val numCriticForReviews: String? = null,
    val duration: Int? = null,
    val directorFacebookLikes: Int? = null,
    val actorThreeFacebookLikes: Int? = null,
    val actorTwoName: String? = null,
    val actorOneFacebookLikes: Int? = null,
    val gross: Int? = null,
    val genres: String? = null,
    val actorOneName: String? = null,
    val movieTitle: String? = null,
    val numVotedUsers: Int? = null,
    val castTotalFacebookLikes: Int? = null,
    val actorThreeName: String? = null,
    val facenumberInPoster: Int? = null,
    val plotKeywords: String? = null,
    val movieImdbLink: String? = null,
    val numUserForReviews: Int? = null,
    val language: String? = null,
    val country: String? = null,
    val contentRating: String? = null,
    val budget: Long? = null,
    val titleYear: Int? = null,
    val actorTwoFacebookLikes: Int? = null,
    val imdbScore: BigDecimal? = null,
    val aspectRatio: String? = null,
    val movieFacebookLikes: Int? = null,
)
