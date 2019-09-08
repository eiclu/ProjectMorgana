package ktor

import domain
import web.freemarker
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.oauth
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.features.StatusPages
import io.ktor.features.origin
import io.ktor.freemarker.FreeMarker
import io.ktor.response.respond
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.directorySessionStorage
import io.ktor.util.KtorExperimentalAPI
import web.loginProvider
import java.io.File

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    /*
     * Implements error message handling with status pages TODO: Probably remove after testing phase
     */
    install(StatusPages) {
        exception<Throwable> { cause ->
            application.log.warn(cause.message ?: cause.javaClass.name)
            call.respond("An error occured, please try again. If this persists, please contact @Looki (Luca) on the Discord Server")
        }
    }

    /*
     * Implements login with Discord Oauth
     * Also see DiscordOauth.kt
     */
    install(Authentication) {
        oauth("discordOauth") {
            client = HttpClient(Apache)
            providerLookup = { loginProvider }
            urlProvider = { "${request.origin.scheme}://$domain/login/oauth" }
        }
    }

    /*
     * Implements Server-Side Sessions
     */
    install(Sessions) {
        cookie<WebSession>("morganaSession", storage = directorySessionStorage(File(".sessions"), cached = false)) {
            cookie.path = "/"
        }
    }

    /*
     * Binds Ktor's implementation of Freemarker to the internal version used by the rest of the project
     */
    install(FreeMarker) { templateLoader = freemarker.templateLoader }

    /*
     * Setups all routes of the server in a dedicated file
     */
    setupRoutingTable()
}

