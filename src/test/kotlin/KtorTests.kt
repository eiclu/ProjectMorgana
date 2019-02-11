import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import database.DatabaseHelper
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.util.KtorExperimentalAPI
import ktor.WebSession
import ktor.module
import kotlin.test.Test
import kotlin.test.assertEquals

@KtorExperimentalAPI
class KtorTests {
    private val databaseHelper = DatabaseHelper()

    @Test
    fun ktorWorking() = withApp {
        with(handleRequest(HttpMethod.Get, "/")) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun tokenLoginWorking() = withApp {
        val testUserId = 0L
        val token = databaseHelper.generateTokenForUser(testUserId)
        with(handleRequest(HttpMethod.Get, "/login/token/$token")) {
            assertEquals(WebSession(testUserId), sessions.get<WebSession>())
        }
    }

    /*@Test
    fun canModifySubjectSubscriptions() = withApp {
        val testUser = User(0L)

        databaseHelper.addUser(testUser)
        databaseHelper.addCourse("Test Subject", null, "Test Course", "TC")

        val testCourse = databaseHelper.getCourses().find { it.course == "Test Course" } ?: throw Exception()

        with(handleRequest(HttpMethod.Post, "/courses") {
            addHeader("content-type", "application/x-www-form-urlencoded")
            setBody("${testCourse.courseId}=on")
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
        }

        val users = databaseHelper.getUsersForCourse(testCourse.courseId)
        println(users)

        databaseHelper.removeUser(testUser.userId)
        databaseHelper.removeCourse(testCourse.courseId)

        assertEquals(testUser.userId, users.firstOrNull()?.userId)
    }*/

    private fun withApp(testFunction: TestApplicationEngine.() -> Unit) = withTestApplication({ module() }, testFunction)
}