package web

data class Page(
    val title: String,
    val url: String?,
    val iconName: String? = null,
    val permissions: Boolean = false
)

val pages = setOf(
    Page("Info", "/", "info"),
    Page("Courses", "/courses", "list"),
    //Page("Profile", "/profile", "/static/user.svg"),
    Page("Administration", "/admin", "settings", true)
)