package ktor

import databaseHelper
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.util.pipeline.PipelineContext
import jdaInstance
import types.Course
import types.User
import web.generatePage

val tagMatcher = """#[0-9]{4}""".toRegex()

/**
 * @return the user object corresponding to the logged in user
 */
fun PipelineContext<Unit, ApplicationCall>.getUser(): User? = call.sessions.get<WebSession>()?.let {
    val dbUser = databaseHelper.getUserById(it.userId) ?: return@let null
    val discordName = jdaInstance.getUserById(dbUser.userId)
    return@let dbUser.copy(
        userName = discordName.name,
        userTag = tagMatcher.find(discordName.asTag)?.value ?: "0000",
        userImageUrl = discordName.avatarUrl
    )
}

/**
 * @return a cascaded version of courses to fill the list
 */
fun Collection<Course>.cascaded(): Map<String, Map<String?, List<Course>>> = this
    .sortedBy { it.subject }
    .groupBy{ it.subject }
    .mapValues {
        it.value
            .sortedBy { it.module }
            .groupBy { it.module }
    }

/**
 * Redirects to the provided route after a delay
 * To be used instead of call.respondRedirect()
 */
suspend fun PipelineContext<Unit, ApplicationCall>.redirect(path: String, delayMilis: Int = 1000) {
    /*call.respond(generatePage("/redirect", "templates/redirect.ftl", mapOf(
        "location" to path,
        "delay" to delayMilis
    ), getUser()))*/
    call.respondRedirect(path)
}

/**
 * The Session Type used by Ktor
 */
data class WebSession(val userId: Long)