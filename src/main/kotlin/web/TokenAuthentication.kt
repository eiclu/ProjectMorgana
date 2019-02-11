package web

import chatInterface
import databaseHelper
import io.ktor.application.ApplicationCall
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.ktor.util.pipeline.PipelineContext
import jdaInstance
import ktor.WebSession
import ktor.redirect
import java.lang.Exception

suspend fun PipelineContext<Unit, ApplicationCall>.loginWithToken(token: String?) {
    if (token != null) {
        val userId = databaseHelper.popToken(token)
        if (userId != null) {
            call.sessions.clear<WebSession>()
            call.sessions.set(WebSession(userId))
            application.log.info("${chatInterface.discordHelper.guild.members.find { it.user.idLong == userId }?.user?.asTag} logged in via Discord OAuth")
            redirect("/courses")
        } else {
            throw TokenException("Invalid Token")
        }
    } else {
        throw TokenException("Please provide a token")
    }
}

class TokenException(e: String) : Exception(e)