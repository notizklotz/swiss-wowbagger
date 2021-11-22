import guru.nidi.wowbagger.*
import guru.nidi.wowbagger.Wowbagger.name
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import twitter4j.*
import twitter4j.conf.ConfigurationBuilder
import twitter4j.util.function.Consumer
import kotlin.random.Random

fun main() {
    val consumerKey: String = System.getenv("WOWBAGGER_BOT_CONSUMER_KEY") ?: System.getenv("CONSUMER_KEY")
    val consumerSecret: String = System.getenv("WOWBAGGER_BOT_CONSUMER_SECRET") ?: System.getenv("CONSUMER_SECRET")
    val accessToken: String = System.getenv("WOWBAGGER_BOT_ACCESS_TOKEN") ?: System.getenv("ACCESS_TOKEN")
    val accessSecret: String = System.getenv("WOWBAGGER_BOT_ACCESS_SECRET") ?: System.getenv("ACCESS_SECRET")

    val config = ConfigurationBuilder().run {
        setDebugEnabled(true)
        setOAuthConsumerKey(consumerKey)
        setOAuthConsumerSecret(consumerSecret)
        setOAuthAccessToken(accessToken)
        setOAuthAccessTokenSecret(accessSecret)
        build()
    }
    val twitter = TwitterFactory(config).instance

    embeddedServer(Netty, port = System.getenv("PORT")?.toInt() ?: 8080) {
        install(ContentNegotiation) {
            jackson()
        }

        routing {
            post("/random") {
                twitter.updateStatus(tweet(listOf()))

                call.respond(HttpStatusCode.NoContent)
            }

            post("/webhook") {
                call.respond(HttpStatusCode.NotImplemented)
//                val twitterStream = TwitterStreamFactory(config).instance
//                twitterStream.onStatus { run {} }
//                twitterStream.filter(FilterQuery(twitter.id)).onStatus(TwitterListener(twitter))
            }
        }
    }.start(wait = true)
}

fun tweet(names: List<String>): String {
    val seed = Random.nextLong() and 0x000fffffffffffffL
    randomSeed(seed)
    val speech = composeSpeech(names)
    val text = speech.copy(
        names = speech.names.map { it.hashed() },
        subject = speech.subject.hashed()
    ).connect().toText()
    val param = if (names.isEmpty()) "" else "?names=" + names.joinToString(",")
    return "$text https://nidi3.github.io/swiss-wowbagger/#$seed$param"
}

fun Entry<String>.hashed() = Entry("#${entry}", phonemes)

class TwitterListener(val twitter: Twitter) : Consumer<Status> {
    override fun accept(status: Status) {
        if (status.inReplyToStatusId > 0 && status.user.id != twitter.id) {
            val words = status.text.split(Regex("\\W+")).filter { it.length in 3..8 }
            val nearEntries = words.map { name(it) }.filter { it.second <= 1 }
            val nearNames = nearEntries.sortedBy { it.second }.map { it.first.entry.name }.toSet().toList()
            StatusUpdate("@${status.user.screenName} " + tweet(nearNames)).let {
                it.inReplyToStatusId = status.id
                twitter.updateStatus(it)
            }
        }
    }

}
