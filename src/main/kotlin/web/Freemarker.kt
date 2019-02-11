package web

import freemarker.template.Configuration
import io.ktor.freemarker.FreeMarkerContent
import types.User
import java.io.StringWriter

val freemarker = Configuration(Configuration.VERSION_2_3_28).apply {
    setClassForTemplateLoading(this::class.java, "/")
}

fun generatePage(url: String, path: String, params: Map<String, Any> = emptyMap(), user: User? = null): FreeMarkerContent {
    val attributes = mapOf(
        "title" to (pages.find { it.url == path }?.title ?: "Matterleast"),
        "content" to generateHtml(path, params),
        "navigation" to generateNavigation(url, user),
        "user" to user
    )
    return FreeMarkerContent("templates/skeleton.ftl", attributes)
}

fun generateHtml(path: String, params: Map<String, Any>): String = try {
    val out = StringWriter()
    freemarker.getTemplate(path).process(params, out)
    out.toString()
} catch (e: Exception) {
    e.toString() //TODO: Remove for Production
}

fun generateNavigation(url: String, user: User?): Set<Page> = when {
    user == null -> pages.filter { !it.permissions }
    !user.isAdmin -> pages.filter { !it.permissions }
    else -> pages
}.map {
    if (url == it.url) it.copy(url = null)
    else it
}.toSet()