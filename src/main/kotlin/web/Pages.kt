package web

data class Page(
    val title: String,
    val url: String?,
    val iconUrl: String? = null,
    val permissions: List<Permissions> = listOf(Permissions.GUEST)
)

val pages = setOf(
    Page("Info", "/", "/static/info.svg", listOf(Permissions.GUEST, Permissions.MEMBER)),
    Page("Login", "/login", "/static/log-in.svg"),
    Page("Courses", "/courses", "/static/list.svg", listOf(Permissions.MEMBER)),
    Page("Profile", "/profile", "/static/user.svg", listOf(Permissions.MEMBER)),
    Page("Administration", "/admin", "/static/cog.svg", listOf(Permissions.ADMIN))
)

enum class Permissions {
    GUEST, MEMBER, ADMIN
}