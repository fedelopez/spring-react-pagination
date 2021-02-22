import React, {Component} from 'react';

class MovieList extends Component {
    state = {
        movies: [],
        currentPage: 0,
        totalMovies: 0,
    }

    componentDidMount() {
        this.fetchMovies(1)
    }

    render() {
        return (
            <div>
                <h1>Top Rated Movies</h1>
                {this.movieList()}
                {this.pageBar()}
            </div>
        )
    }

    movieList = () => {
        return this.state.movies.map((movie, key) =>
            <div className='movie-row' key={key}>
                <div className='movie-title'>{movie.movieTitle}</div>
                <div className='movie-score'>{movie.imdbScore}</div>
            </div>
        )
    }

    pageBar = () => (
        <div className='page-container'>
            <a href='#' onClick={this.handlePrevious}>Previous</a>
            <div className='page-total'>Total: {this.state.totalMovies},
                page {this.state.currentPage} of {this.totalPages()}</div>
            <a href='#' onClick={this.handleNext}>Next</a>
        </div>
    )

    totalPages = () => Math.floor(this.state.totalMovies / 25)

    handlePrevious = (e) => {
        e.preventDefault()
        const newPage = Math.max(0, this.state.currentPage - 1)
        this.fetchMovies(newPage)
    }

    handleNext = (e) => {
        e.preventDefault()
        const newPage = Math.min(this.totalPages(), this.state.currentPage + 1)
        this.fetchMovies(newPage)
    }

    fetchMovies = (newPage) => fetch(`/api/movies?page=${newPage}`)
        .then(res => res.json())
        .then(result => {
            this.setState({...this.state, currentPage: newPage, totalMovies: result.totalMovies, movies: result.movies})
        })
}

export default MovieList;