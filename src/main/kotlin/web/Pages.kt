package web

data class Page(
    val title: String,
    val url: String?,
    val iconUrl: String? = null,
    val permissions: Boolean = false
)

val pages = setOf(
    Page("Info", "/", "/static/info.svg"),
    Page("Courses", "/courses", "/static/list.svg"),
    //Page("Profile", "/profile", "/static/user.svg"),
    Page("Administration", "/admin", "/static/cog.svg", true)
)