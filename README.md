# Web 101: Pagination in Spring Boot / React

## Pre-requisites

- Java 11 installed
- Node installed (preferably current LTS)
- IntelliJ or any other preferred IDE

## Setting up the Spring Boot backend

### Download template Spring Boot project

Go to the Spring Initializr website with the following URL:

```bash
https://start.spring.io/#!type=gradle-project&language=kotlin&platformVersion=2.4.3.RELEASE&packaging=jar&jvmVersion=11&groupId=com.adevinta&artifactId=ma-web-101&name=ma-web-101&description=Web%20Development%20Demo%20project%20for%20Spring%20Boot%20and%20React&packageName=com.adevinta.ma-web-101&dependencies=web,data-jdbc,h2
```

This project features a Spring Backend with:

- Gradle
- Java 11
- Kotlin
- Spring Web
- Spring Data JDBC
- H2 DB as persistent store

Unzip the file and open the project in IntelliJ.

### DB Setup and Seeding

Show database `schema.sql` file with movie records. The goal is to be able to injest it automatically when the backend
starts.

Place it under the `resources` folder, having this name Spring will pick it up automatically.

Copy these entries to connect to the DB and paste them on the `application.properties file

```properties
spring.datasource.url=jdbc:h2:mem:moviedb;IGNORECASE=TRUE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.initialization-mode=always
spring.jpa.hibernate.ddl-auto=none
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```

### Spring Repo Setup

Create data class mapping the table records, for this demo we only need movieTitle and imdbScore, but this is the full
data class:

```kotlin
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
```

Let's add the counterpart pageable repository, to allow pagination of results from the frontend:

```kotlin
@Repository
interface MovieRepository : PagingAndSortingRepository<Movie, Long>
```

Now we can check that we are able to read from the repository when the app starts, for that we will use
a `CommandLineRunner`:

````kotlin
@SpringBootApplication
class MaWeb101Application(val movieRepository: MovieRepository) : CommandLineRunner {
    override fun run(vararg args: String?) {
        movieRepository
            .findAll()
            .take(100)
            .forEach(::println)
    }
}
````

Let's add a controller with a `GET` endpoint to return a response object a list of with 25 movies per page and the total
movies:

```kotlin
@RestController
class MovieController(val movieRepository: MovieRepository) {

    @GetMapping("/api/movies")
    fun getMovies(@RequestParam(required = false, defaultValue = "0") page: Int): PageResponse {
        val resultSize = 25
        val totalMovies = movieRepository.count()
        val pageRequest = PageRequest.of(page, resultSize, Sort.by("movieTitle"))
        return PageResponse(totalMovies, movieRepository.findAll(pageRequest).toList())
    }

    data class PageResponse(val totalMovies: Long, val movies: List<Movie>)
}
```

When calling the endpoint with a client, we can pass a page number to obtain 25 movies and the total movies:

```bash
curl "http://localhost:8080/api/movies?page=1" | jq
```

## Setting up the front end

### Serving static content from Spring Boot

Navigate to the `resources/static` folder.

Right click on it and select to create a new HTML page. Make some updates to it.

Start the server and on your browser go to `http://localhost:8080, the page is served automatically by Spring!

Now you can delete the page.

### Scaffolding the web app

Go to the `src` folder and scaffold a new React app on a folder named `web`:

```bash
npx create-react-app web
```

- Show how to start the app, make a quick update, stress the fact this is a dev server, not for deployment but for fast
  feedback loops
- Show React app in project and go through quick overview of React app anatomy
- Show how the app gets injected in the `public/index.html` root div
- Show how to build the app using `npm run build`

## Automate package deployment to Spring Boot folder

Update the `package.json` with the following scripts:

```
    "copy-web": "cp -a build/. ../main/resources/static",
    "build": "react-scripts build && npm run copy-web",
```

- Now stop the dev server and start the Spring Boot server, the React App gets served!

Stress the fact:

- this task should be done in the CI/CD pipeline or for local testing
- contents in `src/resources` should not be committed

Stress the fact the proxy is for local environment only!

### Creating our first component, say hi to JSX!

```jsx
import React, {Component} from 'react'

class MovieList extends Component {
    componentDidMount() {
        console.log('Mounted!')
    }

    render() {
        return <h1>Coming Soon!</h1>
    }
}

export default MovieList;
```

- Explain what is JSX
- Explain `componentDidMount` and `render` methods in the React lifecycle

Now we can use our component from the main App:

```jsx
import './App.css'
import MovieList from './MovieList'

function App() {
    return (
        <div className="app">
            <MovieList/>
        </div>
    );
}

export default App;
```

### CSSing

Replace the contents of the file `App.css` with the following:

```css
.app {
    width: 75%;
    height: 100%;
    margin: 0 auto;
}

.movie-row {
    display: flex;
    margin-bottom: 0.5rem;
}

.movie-title {
    margin-right: 2rem;
    flex-grow: 2;
}

.movie-score {
    flex-grow: 1;
    text-align: end;
}

.page-container {
    display: flex;
    justify-content: space-evenly;
    margin-top: 2rem;
}

.page-total {
    flex-grow: 2;
    text-align: center
}
```

### Our first call to the backend, enter fetch and component state

Note the call below won't work with the React Server unless we proxy the calls (we will see this next).

````jsx
import React, {Component} from 'react'

class MovieList extends Component {
    state = {
        movies: [],
    }

    componentDidMount() {
        fetch('/api/movies')
            .then(result => result.json())
            .then(json => {
                this.setState({movies: json.movies})
            })
    }

    render() {
        const movieList = this.state.movies.map((movie, index) =>
            <div className='movie-row' key={index}>
                <div className='movie-title'>{movie.movieTitle}</div>
                <div className='movie-score'>{movie.imdbScore}</div>
            </div>
        )
        return (
            <div>
                <h1>Top Rated Movies</h1>
                {movieList}
            </div>
        )
    }
}

export default MovieList;
````

### Proxy the http calls to the Spring Boot server.

Open the `package.json` from the React app and add a proxy to the Spring Boot server:

```text
{
  "name": "web",
  "version": "0.1.0",
  "private": true,
  ...
  "proxy": "http://localhost:8080"
}
```

### Show the total number of movies and pages

```jsx
import React, {Component} from 'react';

class MovieList extends Component {
    state = {
        currentPage: 0,
        movies: [],
        totalMovies: 0,
    }

    componentDidMount() {
        fetch('/api/movies')
            .then(result => result.json())
            .then(json => {
                this.setState({currentPage: 0, totalMovies: json.totalMovies, movies: json.movies})
            })
    }

    render() {
        const movieList = this.state.movies.map((movie, key) =>
            <div className='movie-row' key={key}>
                <div className='movie-title'>{movie.movieTitle}</div>
                <div className='movie-score'>{movie.imdbScore}</div>
            </div>
        )
        return (
            <div>
                <h1>Top Rated Movies</h1>
                {movieList}
                <div className='page-container'>
                    <div className='page-total'>Total: {this.state.totalMovies},
                        page {this.state.currentPage} of {this.totalPages()}</div>
                </div>
            </div>
        )
    }

    totalPages = () => Math.floor(this.state.totalMovies / 25)
}

export default MovieList
```

### Let's start cleaning up

```jsx
import React, {Component} from 'react';

class MovieList extends Component {
    state = {
        movies: [],
        currentPage: 0,
        totalMovies: 0,
    }

    componentDidMount() {
        fetch('/api/movies')
            .then(result => result.json())
            .then(json => {
                this.setState({currentPage: 0, totalMovies: json.totalMovies, movies: json.movies})
            })
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
            <div className='page-total'>Total: {this.state.totalMovies},
                page {this.state.currentPage} of {this.totalPages()}</div>
        </div>
    )

    totalPages = () => Math.floor(this.state.totalMovies / 25)
}

export default MovieList
```

### Pagination components, Next and Previous

```jsx harmony
import React, {Component} from 'react'

class MovieList extends Component {
    state = {
        movies: [],
        currentPage: 0,
        totalMovies: 0,
    }

    componentDidMount() {
        fetch('/api/movies')
            .then(result => result.json())
            .then(json => {
                this.setState({currentPage: 0, totalMovies: json.totalMovies, movies: json.movies})
            })
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

    handlePrevious = (event) => {
        event.preventDefault()
        const newPage = Math.max(0, this.state.currentPage - 1)
        fetch(`/api/movies?page=${newPage}`)
            .then(res => res.json())
            .then(result => {
                this.setState({currentPage: newPage, totalMovies: result.totalMovies, movies: result.movies})
            })
    }

    handleNext = (event) => {
        event.preventDefault()
        const newPage = Math.min(this.totalPages(), this.state.currentPage + 1)
        fetch(`/api/movies?page=${newPage}`)
            .then(result => result.json())
            .then(json => {
                this.setState({currentPage: newPage, totalMovies: json.totalMovies, movies: json.movies})
            })
    }
}

export default MovieList
```

### Refactor fetch call

```jsx
import React, {Component} from 'react'

class MovieList extends Component {
    state = {
        movies: [],
        currentPage: 0,
        totalMovies: 0,
    }

    componentDidMount() {
        this.fetchMovies(0)
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

    handlePrevious = (event) => {
        event.preventDefault()
        const newPage = Math.max(0, this.state.currentPage - 1)
        this.fetchMovies(newPage)
    }

    handleNext = (event) => {
        event.preventDefault()
        const newPage = Math.min(this.totalPages(), this.state.currentPage + 1)
        this.fetchMovies(newPage)
    }

    fetchMovies = (newPage) => fetch(`/api/movies?page=${newPage}`)
        .then(result => result.json())
        .then(json => {
            this.setState({...this.state, currentPage: newPage, totalMovies: json.totalMovies, movies: json.movies})
        })
}

export default MovieList
```

## Other topics

- Routing client side
- Props and communicating between components