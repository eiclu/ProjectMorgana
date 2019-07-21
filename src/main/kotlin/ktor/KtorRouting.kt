package ktor

import chatInterface
import databaseHelper
import io.ktor.application.*
import web.generatePage
import io.ktor.auth.authenticate
import io.ktor.http.Parameters
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import types.Channel
import types.Course
import types.Major
import web.TokenException
import web.loginWithDiscordOauth
import web.loginWithToken
import java.lang.RuntimeException

fun Application.setupRoutingTable() {
    routing {
        route("/") {
            static("static") {
                resources("css")
                resources("images")
                resources("js")
            }

            get {
                call.respond(generatePage("/", "templates/sites/index.ftl", user = getUser()))
            }

            route("/courses") {
                intercept(ApplicationCallPipeline.Features) {
                    if (getUser() == null) {
                        redirect("/login")
                    }
                }

                get {
                    val user = getUser()
                    val userCourses = databaseHelper.getCoursesForUser(user!!.userId)
                    val courses = databaseHelper.getCourses().map { course ->
                        if (userCourses.contains(course)) course.copy(enabled = true)
                        else course
                    }
                    call.respond(generatePage("/courses", "templates/sites/courses.ftl", mapOf(
                        "subjects" to courses.cascaded()
                    ), user))
                }

                post {
                    val params = call.receive<Parameters>()
                    val enabledCourses = params.names().mapNotNull { it.toLongOrNull() }
                    getUser()?.userId?.let { userId ->
                        databaseHelper.updateSubscribedCourses(userId, enabledCourses)
                        chatInterface.onUserUpdateChannels(userId)
                    }
                    redirect("/courses")
                }
            }

            route("/profile") {
                intercept(ApplicationCallPipeline.Features) {
                    if (getUser() == null) {
                        redirect("/login")
                    }
                }

                get {
                    val user = getUser()!!
                    call.respond(generatePage("/profile", "templates/sites/profile.ftl", mapOf(
                        "user" to user,
                        "majors" to Major.values()
                    ), user))
                }

                post {
                    val params = call.receive<Parameters>()
                    val currentSemester = params["currentSemester"] ?: throw RuntimeException("Malformed POST request")
                    val major = params["major"] ?: throw RuntimeException("Malformed POST request")
                    getUser()?.userId?.let { userId ->
                        databaseHelper.updateUserInfo(userId, mapOf("CurrentSemester" to currentSemester, "Major" to major))
                        chatInterface.onUserUpdateRole(userId)
                    }
                    redirect("/profile")
                }
            }

            route("/admin") {
                intercept(ApplicationCallPipeline.Features) {
                    if (getUser()?.isAdmin == false) {
                        redirect("/login")
                    }
                }

                get {
                    val user = getUser()!!
                    val courses = databaseHelper.getCourses()
                    call.respond(generatePage("/admin", "templates/sites/admin.ftl", mapOf(
                        "subjects" to courses.cascaded()
                    ), user))
                }

                post {
                    /*val toDeleteCourses = call.receive<Parameters>().names().mapNotNull { it.toLongOrNull() }
                    getUser()?.userId?.let { _ ->
                        databaseHelper.deleteCourses(toDeleteCourses)
                        chatInterface.onUserUpdateChannels(userId)
                    }*/
                    redirect("/admin")
                }

                post("/deleteCourses") {
                    val params = call.receive<Parameters>()
                    val coursesToDelete = params.names().mapNotNull { it.toLongOrNull() }
                    application.log.info("User ${getUser()!!.let { "${it.userName}#${it.userTag}" }} just deleted the Courses ${coursesToDelete.joinToString(", ")}")
                    coursesToDelete.forEach { course ->
                        databaseHelper.removeCourse(course)
                    }
                    chatInterface.onChannelsUpdated()
                    redirect("/admin")
                }

                post("/addCourse") {
                    val params = call.receive<Parameters>()
                    databaseHelper.addCourse(
                        params["subject"] ?: throw NoValueException("Subject must not be null"),
                        params["module"],
                        params["course"] ?: throw NoValueException("Course must not be null"),
                        params["shorthand"]
                    )
                    chatInterface.onChannelsUpdated()
                    redirect("/admin")
                }

                get("/courses/{courseId}") {
                    val course: Course = databaseHelper.getCourseById(call.parameters["courseId"]?.toLongOrNull() ?: return@get) ?: return@get
                    val channels: List<Channel> = databaseHelper
                        .getChannelsForCourse(course.courseId)
                        .mapNotNull { channel ->
                            chatInterface.discordHelper.guild.channels.find { it.idLong == channel.channelId }
                        }
                        .map {
                            Channel(it.idLong, it.name)
                        }
                    call.respond(generatePage("/admin/courses/courseId", "templates/sites/admin_details.ftl", mapOf(
                        "course" to course,
                        "channels" to channels
                    ), getUser()!!))
                }

                post("/courses/{courseId}/update-info") {
                    val course = databaseHelper.getCourseById(call.parameters["courseId"]?.toLongOrNull() ?: return@post) ?: return@post
                    val params = call.receive<Parameters>()
                    databaseHelper.updateCourseInfo(
                        course.courseId,
                        mapOf(
                            "subject" to params["subject"],
                            "module" to params["module"],
                            "course" to params["course"],
                            "shorthand" to params["shorthand"]
                        )
                    )
                    redirect("/admin/courses/${course.courseId}")
                }

                post("/courses/{courseId}/update-channels") {
                    val course = databaseHelper.getCourseById(call.parameters["courseId"]?.toLongOrNull() ?: return@post) ?: return@post
                    val params = call.receive<Parameters>().entries()
                    log.trace(params.joinToString { "[${it.key}] ${it.value.joinToString(",")}" })
                    val channels = chatInterface.discordHelper.guild.channels
                    params.forEach { param ->
                        channels.find { it.idLong == param.key.toLongOrNull() }?.let { channel ->
                            param.value.firstOrNull()?.let {
                                channel.manager.setName(it).queue()
                            }
                        }
                    }
                    redirect("/admin/courses/${course.courseId}")
                }

                post("/courses/{courseId}/add-channel") {
                    val course = databaseHelper.getCourseById(call.parameters["courseId"]?.toLongOrNull() ?: return@post) ?: return@post
                    chatInterface.discordHelper.addChannelForCourse(course)
                    redirect("/admin/courses/${course.courseId}")
                }
            }

            route("/login") {
                get {
                    call.respond(generatePage("/login", "templates/sites/login.ftl", mapOf()))
                }
                get("/token") {
                    call.respond(generatePage("/login/token", "templates/sites/token.ftl", mapOf(), getUser()))
                }
                get("/token/{token}") {
                    try {
                        loginWithToken(call.parameters["token"])
                    } catch (e: TokenException) {
                        log.info("Token authentication failed. Reason: ${e.localizedMessage}")
                        call.respond(e.localizedMessage)
                    }
                }
                authenticate("discordOauth") {
                    get("/oauth") {
                        loginWithDiscordOauth()
                    }
                }
            }

            get("/logout") {
                call.sessions.clear<WebSession>()
                redirect("/")
            }
        }
    }
}

class NoValueException(e: String): Exception(e)