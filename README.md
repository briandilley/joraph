
![Build And Deploy Joraph](https://github.com/briandilley/joraph/workflows/Build%20And%20Deploy%20Joraph/badge.svg?branch=develop)
[![codecov](https://codecov.io/gh/briandilley/joraph/branch/develop/graph/badge.svg?token=IM4UTUDXNB)](https://codecov.io/gh/briandilley/joraph)

Joraph
======

Java library for loading and joining object graphs, JOin gRAPH. Joraph's only dependency is the JVM and Kotlin
runtime. It can be used with Kotlin and Java (8+) projects alike. It has been used in
[many high traffic production environments](#who-has-used-joraph-in-production) and is battle tested. Joraph
is not a data access library, it knows nothing about your RDBMS, cache, or other persistant storage. It is merely
a wrapper on top of these things to make it easy to load and join related objects (across many different storage
mechanisms) into an object graph. While the sample code in this documentation is Kotlin, care has been taking to
ensure that the library is just as easy to use in a Java environment.

# Maven / Gradle integration

Joraph is available from Maven Central:

maven
```xml
<dependency>
    <groupId>com.github.briandilley.joraph</groupId>
    <artifactId>joraph</artifactId>
    <version>1.0</version>
</dependency>
```

gradle
```groovy
dependencies {
    compile('com.github.briandilley.joraph:joraph:1.0')
}
```

# What's it useful for?

Joraph is useful in places where given a few entities that you've already loaded you need to load a graph of
dependent (and transitive dependencies) objects to build a response to an API call.  For instance, lets say you
have an endpoint `GET /api/videos/{id}` that loads the given video by id and returns the following payload:

```json
{
  "video": {
    "id": 69,
    "title": "My awesome video",
    "url": "https://www.example.com/videos/dg9se8gj.mp4",
    "musicTrack": {
      "id": 64584,
      "name": "California Love",
      "url": "https://www.example.com/tracks/34lkjfg.aac",
      "artist": {
        "id": 23462,
        "name": "Tupac Shakur",
        "description": "The most prolific rapper of all time"
      }
    },
    "createdBy": {
      "id": 6846,
      "display_name": "HipHop Lover",
      "username": "john_smith_loves_hiphop"
    }
  }
}
```

As you can see, the root object is a `video`. The video contains a `musicTrack` (meta data about the music in the video)
and a `createBy` (the user that created it). Further, the `musicTrack` contains an `artist` (meta data about the artist
who recorded the `musicTrack`).

A simple data model in an RDMS for this might look like this:

- `Video` (id, title, url, creator_id, music_track_id)
- `MusicTrack` (id, name, url, music_artist_id)
- `MusicArtist` (id, name, description)
- `User` (id, displayName, username)

A simple implementation for this endpoint might look something like this:

```kotlin
    val video = videoManager.getVideoById(id)
    val musicTrack = musicTrackManager.getMusicTrackById(video.music_track_id)
    val artist = artistManager.getArtistById(musicTrack.artist_id)
    val createBy = userManager.getUserById(video.createdBy)
    
    val responseObject = assembleVideoResponse(video, musicTrack, artist, createdBy)
```

While with joraph it would look like this:
```kotlin
    val video = videoManager.getVideoById(id)
    val objectGraph = joraphContext.query(Query()
            .withRootEntity(video))
            
    val responseObject = assembleVideoResponse(objectGraph, video)
```

Much more simple, but even better - imagine an endpoint `GET /api/videos/search?text=funny+memes` that returns
many video objects and all of the work entailed in loading the dependencies (and transitive dependencies), let
alone associating the correct dependencies with their parent objects.

The benefit of Joraph becomes even clearer when:
- your object graph contains many more objects
- the graph is deeper (more transitive dependencies)
- dependencies are optional
- multiple root object types
- your endpoints return lists of objects

# How to use it

After you've added the required dependencies to your gradle script or maven pom you can begin by setting up
your schema.  Borrowing from the schema outlined above:

The object model model as exposed by your business or data layer:
```kotlin
data class Video(
  val id: Long,
  val title: String,
  val url: String,
  val creatorId: Long,
  val musicTrackId: Long)

data class MusicTrack(
  val id: Long,
  val name : String,
  val url: String,
  val musicArtistId: Long)

data class MusicArtist(
  val id: Long,
  val name: String,
  val description: String)

data class User(
  val id: Long,
  val displayName: String,
  val username: String)
```

Create a schema for the object model and then add `EntityDescriptor`s to it that define your schema.
```kotlin
val schema = new Schema()

// add Video class
schema.addEntityDescriptor(Video::class.java)
  // who's primary key is the `id` property
  .withPrimaryKey(Video::id)
  // and has a foreign key to the `User` object from it's `creatorId` property
  .withForeignKey(User::class.java, Video::creatorId)
  // along with a foreign key to the `MusicTrack` object from it's `musicTrackId` property
  .withForeignKey(MusicTrack::class.java, Video::musicTrackId)

// add MusicTrack class
schema.addEntityDescriptor(MusicTrack::class.java)
  .withPrimaryKey(MusicTrack::id)
  .withForeignKey(MusicArtist::class.java, MusicTrack::musicArtistId)

// add MusicArtist class
schema.addEntityDescriptor(MusicArtist::class.java)
  .withPrimaryKey(MusicArtist::id)

// add User class
schema.addEntityDescriptor(User::class.java)
  .withPrimaryKey(User::id)
```

Every entity _must_ have one `PrimaryKey`, and may have 0 or more `ForeignKey`s. Joraph only loads entities
by their `PrimaryKey`, and does so in bulk (ie: multi-get). This makes it extremely easy to implement caching
of entities.

Now create the `JoraphContext`, you'll use this object to execute queries - you only need one instance of it
for your entire application to use:
```kotlin
val joraphContext = JoraphContext(
  // use the schema configured
  schema = schema,
  // Joraph loads entities in parallel when possible,
  // this defines how many threads are available to
  // joraph for doing so.
  parallelExecutorCount = 50)
```

Now you need to tell joraph how to load the entities that you expect it to load, do this with the
`EntityLoaderContext`:
```kotlin
val loaderContext: EntityLoaderContext = joraphContext.loaderContext

loaderContext.withLoader(Video::class.java) { ids: Collection<Long> ->
  videoManager.findVideosByIds(ids)
}

loaderContext.withLoader(MusicTrack::class.java) { ids: Collection<Long> ->
  musicTrackRestClient.getMusicTracks(ids)
}

loaderContext.withLoader(MusicArtist::class.java, MusicArtistDao::loadArtistsByIds)

loaderContext.withLoader(User::class) { ids: Collection<Long> ->
  cachedUserManager.getUsersByIds(ids)
}
```

A loader is merely a function (or lambda) that is given a `Collection` of ids and is expected to
return the objects for those ids. The ids can be of any type and the loader can load from anywhere.

Now your context is created and ready to be used:

search for some videos:
```kotlin
val videos: List<Video> = videoManager.searchVideos("Tupac")
```

and use Joraph to load the rest of the object graph for each video:
```kotlin
val objectGraph = joraphManager.query(Query()
  .withRootEntities(videos))
```

Now all of the videos and all of the dependent objects are inside of the `objectGraph`:

```kotlin
val video0 = objectGraph.get(Video::class.java, videos.get(0).id)
val video0CreatedByUser = objectGraph.get(User::class.java, videos.get(0).creatorId)
etc.
```

Your `ObjectGraph` is populated and ready to be used to create your response to the API call.  Follows is
a common pattern used to do just that:

```kotlin
class VideoResponse(
  val id: Long,
  val title: String,
  val url: String,
  val creator: UserResponse,
  val musicTrack: MusicTrackResponse) {
    
  companion object {
      
    fun from(objectGraph: ObjectGraph, video: Video?): VideoResponse? {
      if (video == null) { 
        return null
      }
      
      val creator = objectGraph.get(User::class.java, video.creatorId)
      val musicTrack = objectGraph.get(MusicTrack::class.java, video.musicTrackId)
      
      return VideoResponse(
        id          = video.id,
        title       = video.title,
        url         = video.url,
        creator     = UserResponse.from(objectGraph, creator),
        musicTrack  = MusicTrackResponse.from(objectGraph, musicTrack))
    }
  }
}

class MusicTrackResponse(
  val id: Long,
  val name: String,
  val url: String,
  val musicArtist: MusicArtistResponse) {

  companion object {

    fun from(objectGraph: ObjectGraph, musicTrack: MusicTrack?): MusicTrackResponse? {
      if (musicTrack == null) {
        return null
      }

      val musicArtist = objectGraph.get(MusicArtist::class.java, musicTrack.musicArtistId)

      return MusicTrackResponse(
        id          = musicTrack.id,
        name        = musicTrack.title,
        url         = musicTrack.url,
        musicArtist = MusicArtistResponse.from(objectGraph, musicArtist))
    }
  }
}

class MusicArtistResponse(
  val id: Long,
  val name: String,
  val description: String) {

  companion object {

    fun from(objectGraph: ObjectGraph, artist: MusicArtist?): MusicArtistResponse? {
      if (artist == null) {
        return null
      }

      return MusicArtistResponse(
        id          = artist.id,
        name        = artist.title,
        description = artist.description)
    }
  }
}

class UserResponse(
  val id: Long,
  val displayName: String,
  val username: String) {

  companion object {

    fun from(objectGraph: ObjectGraph, user: User?): UserResponse? {
      if (user == null) {
        return null
      }

      return UserResponse(
        id          = user.id,
        displayName = user.displayName,
        username    = user.username)
    }
  }
}
```

The pattern is pretty simple. Response objects each contain a static method called `from` that take an `ObjectGraph`
as the first parameter and the actual model object as it's second parameter.  It then populates itself using the
model object and referring to other response objects's `from` methods to hydrate child objects.

And an example usage of a search endpoint returning a list of `Video` objects.

```kotlin

    @GetMapping("/api/videos/search")
    fun searchVideos(@RequestParam("search") search: String): List<VideoResponse> {
      
      val videos = videoManager.searchVideos(search)
      
      val objectGraph = joraphContext.query(Query()
        .withRootEntities(videos))

      return videos
        .map { video -> VideoResponse.from(objectGraph, video)}
    }

```

# Future work

- View hydration layer. Something implementing the above described pattern that keeps you from having to create
  the response classes.

- GraphQL front-end. Joraph could be a great back-end to a GraphQL front-end.

- Documentation of all of the functionality (ie: conditional foreign keys, loader arguments, etc.)

# Who has used Joraph in Production?

- *[Flipagram](Flipagram.com)*: (now Vigo Video) #1 app in 81 countries, hundreds of millions of users, serving
  request throughput in excess of ~500k requests per minute and joining objects across a 24
  node postgresql shard, 50+ node Cassandra cluster, Redis, Memcached, and ElasticSearch.

- *[Blockfolio](http://www.blockfolio.com)*: #1 crypto portfolio tracking app, tens of millions
  of users,s ervice request throughput in excess of 50k requests per minute and joining objects
  across PostgreSQL, Cassandra, Redis, Memcached, InfluxDB, and more.
  
- *[QuickRide](http://www.goquickride.com)*: Dealership shuttle management software, thousands of users, joining
  objects from an RDBMS.

- Add an issue if you'd like to be added to this list, include a description of your usage.
