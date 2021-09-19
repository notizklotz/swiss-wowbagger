/*
 * Copyright © 2018 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.wowbagger.telegram

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.telegram.telegrambots.meta.api.objects.Update

private val botToken: String = System.getenv("WOWBAGGER_BOT_TOKEN") ?: System.getenv("TOKEN")
private val botUsername: String = System.getenv("WOWBAGGER_BOT_USER") ?: System.getenv("USER")
private val webookUpdateHandler = WebhookUpdateHandler(TelegramApiClient(botToken), botUsername)

fun main() {
    embeddedServer(CIO, port = System.getenv("PORT")?.toInt() ?: 8080) {
        install(ContentNegotiation) {
            jackson()
        }

        routing {
            post("/$botToken/webhook") {
                val update = call.receive<Update>()

                webookUpdateHandler.onWebhookUpdateReceived(update)

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }.start(wait = true)
}
