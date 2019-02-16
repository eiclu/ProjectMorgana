package web

import clientId
import clientSecret
import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.OAuthServerSettings
import io.ktor.auth.authentication
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpMethod
import io.ktor.response.respond
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.ktor.util.pipeline.PipelineContext
import ktor.WebSession
import ktor.redirect

val loginProvider = OAuthServerSettings.OAuth2ServerSettings(
    name = "discord",
    authorizeUrl = "https://discordapp.com/api/oauth2/authorize",
    accessTokenUrl = "https://discordapp.com/api/oauth2/token",
    requestMethod = HttpMethod.Post,
    clientId = clientId,
    clientSecret = clientSecret,
    defaultScopes = listOf("identify")
)

data class JsonDiscordUser(
    val username: String,
    val locale: String,
    val mfa_enabled: Boolean,
    val flags: Int,
    val avatar: Any?,
    val discriminator: String,
    val id: Long
)

suspend fun PipelineContext<Unit, ApplicationCall>.loginWithDiscordOauth() {
    val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
    if (principal == null) {
        application.log.warn("OAuth request didn't return a proper principal")
        call.respond("Login failed. Please try again later")
    } else {
        val json = HttpClient(Apache).get<String>("https://discordapp.com/api/v6/users/@me") {
            header("Authorization", "Bearer ${principal.accessToken}")
        }
        val user = Gson().fromJson(json, JsonDiscordUser::class.java)
        call.sessions.set(WebSession(user.id))
        application.log.info("${user.username}#${user.discriminator} logged in via Discord OAuth")
        redirect("/")
    }
}