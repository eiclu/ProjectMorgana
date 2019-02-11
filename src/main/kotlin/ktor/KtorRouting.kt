package ktor

import chatInterface
import databaseHelper
import web.generatePage
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.authenticate
import io.ktor.http.Parameters
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import jdaInstance
import types.Channel
import types.Course
import web.loginWithDiscordOauth
import web.loginWithToken

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
                        "user" to user
                    ), user))
                }

                post {
                    val params = call.receive<Parameters>()
                    getUser()?.userId?.let { userId ->
                        @Suppress("UNCHECKED_CAST")
                        databaseHelper.updateUserInfo(userId, mapOf(
                            "currentSemester" to params["currentSemester"]?.toIntOrNull(),
                            "mayor" to params["mayor"],
                            "fullName" to params["fullName"]
                        ).filterValues { it != null } as Map<String, Any>)
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
                        log.trace("Param: $param")
                        channels.find { it.idLong == param.key.toLongOrNull() }?.let { channel ->
                            log.trace("Channel: ${channel.name}")
                            param.value.firstOrNull()?.let {
                                log.info("Value: $it")
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
                    call.respond(generatePage("/login", "templates/sites/login.ftl", mapOf(), getUser()))
                }
                get("/token/{token}") { loginWithToken(call.parameters["token"]) }
                authenticate("discordOauth") { get("/oauth") { loginWithDiscordOauth() } }
            }

            get("/logout") {
                call.sessions.clear<WebSession>()
                redirect("/")
            }
        }
    }
}

class NoValueException(e: String): Exception(e)