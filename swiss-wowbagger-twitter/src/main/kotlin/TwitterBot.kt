import com.fasterxml.jackson.databind.node.ObjectNode
import guru.nidi.wowbagger.*
import guru.nidi.wowbagger.Wowbagger.name
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import twitter4j.*
import twitter4j.conf.ConfigurationBuilder
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
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
                // Creates a random tweet. Called by Google Cloud Scheduler (Cron Job).
                twitter.updateStatus(tweet(listOf()))

                call.respond(HttpStatusCode.NoContent)
            }

            post("/webhook") {
                // Callback from Twitter if something happens

                val body = call.receive(ObjectNode::class)

                if (body.has("tweet_create_events")) {
                    val status = body["tweet_create_events"].first()

                    val isReply= status["in_reply_to_status_id"]?.isNull == false
                    val isWrittenByOtherUser = status["user"]["id"].longValue() != twitter.id

                    if (isReply && isWrittenByOtherUser) {
                        val words = status["text"].textValue().split(Regex("\\W+")).filter { it.length in 3..8 }
                        val nearEntries = words.map { name(it) }.filter { it.second <= 1 }
                        val nearNames = nearEntries.sortedBy { it.second }.map { it.first.entry.name }.toSet().toList()

                        val tweetAuthorScreenName = status["user"]["screen_name"].textValue()

                        StatusUpdate("@$tweetAuthorScreenName " + tweet(nearNames)).let {
                            it.inReplyToStatusId = status["id"].longValue()
                            twitter.updateStatus(it)
                        }
                    }
                } else {
                    log.info("nothing to do")
                }

                call.respond(HttpStatusCode.NoContent)
            }

            get("/webhook") {
                // Allows Twitter to verify our webhook.
                // https://developer.twitter.com/en/docs/twitter-api/premium/account-activity-api/guides/securing-webhooks

                val crcToken = call.parameters["crc_token"]
                if (crcToken != null) {
                    log.debug("CRC verification requested for $crcToken")

                    val sha256Hmac = Mac.getInstance("HmacSHA256")
                    sha256Hmac.init(SecretKeySpec(consumerSecret.toByteArray(), "HmacSHA256"))
                    val responseToken = Base64.getEncoder().encodeToString(sha256Hmac.doFinal(crcToken.toByteArray()))

                    data class CrcResponseToken(val response_token: String)

                    call.respond(CrcResponseToken("sha256=$responseToken"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, "crc_token is required")
                }
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
