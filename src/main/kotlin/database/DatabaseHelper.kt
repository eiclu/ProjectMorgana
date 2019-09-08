package database

import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import types.Channel
import types.Course
import types.Token
import types.User
import java.lang.reflect.Array.setLong
import java.sql.*
import java.util.*

class DatabaseHelper() {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val dbConnection: Connection = DriverManager.getConnection("jdbc:${System.getenv("DB_URL")}", System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD"))

    fun popToken(token: String): Long? {
        val currentTime = System.currentTimeMillis() / 1000L

        //language=MySQL
        val dbResult = getDatabaseResult("SELECT UserId, CreationDate FROM Tokens WHERE Token=?", Token, token)
        val tokenResult = dbResult?.firstOrNull() ?: return null

        //If the token is from last week
        return if (currentTime - tokenResult.timeSeconds < 604800L) tokenResult.userId else {
            //language=MySQL
            updateDatabase("DELETE FROM Tokens WHERE Token=?", token)
            null
        }
    }

    @Language("MySQL")
    fun getUserById(id: Long): User? = getDatabaseResult("SELECT UserId, CurrentSemester, Major, IsAdmin FROM Users WHERE UserId=?", User, id)?.firstOrNull()

    @Language("MySQL")
    fun getCourses(): Set<Course> = getDatabaseResult("""
        SELECT Courses.CourseId, Subject, Module, Course, Shorthand, Count(Users.UserId) AS UserCount FROM Courses
        LEFT OUTER JOIN User_Course_Links UCL on Courses.CourseId = UCL.CourseId
        LEFT OUTER JOIN Users on UCL.UserId = Users.UserId
        GROUP BY Courses.CourseId
    """.trimIndent(), Course) ?: emptySet()

    fun getCoursesForUser(userId: Long): Set<Course> {
        //language=MySQL
        updateDatabase("DROP TABLE IF EXISTS TempCourses")
        //language=MySQL
        updateDatabase("""
            CREATE TEMPORARY TABLE TempCourses AS
              SELECT Courses.CourseId, Subject, Module, Course, Shorthand, Count(Users.UserId) AS UserCount FROM Courses
              LEFT OUTER JOIN User_Course_Links UCL on Courses.CourseId = UCL.CourseId
              LEFT OUTER JOIN Users on UCL.UserId = Users.UserId
              GROUP BY Courses.CourseId;
        """.trimIndent())
        @Language("MySQL")
        val getCoursesQuery = """
            SELECT TempCourses.CourseId, Subject, Module, Course, Shorthand, UserCount FROM TempCourses
            LEFT OUTER JOIN User_Course_Links UCL on TempCourses.CourseId = UCL.CourseId
            LEFT OUTER JOIN Users on UCL.UserId = Users.UserId
            WHERE Users.UserId = ?
        """.trimIndent()
        return getDatabaseResult(getCoursesQuery, Course, userId) ?: emptySet()
    }

    fun updateSubscribedCourses(userId: Long, enabledCourses: Collection<Long>) {
        //language=MySQL
        updateDatabase("DELETE FROM User_Course_Links WHERE UserId=?", userId)
        val enabledCoursesQuery = enabledCourses.joinToString(", ") { courseId -> "($userId, $courseId)" }
        //language=MySQL
        updateDatabase("INSERT INTO User_Course_Links (UserId, CourseId) VALUES $enabledCoursesQuery") //TODO: Convert to preparedStatements
    }

    fun updateUserInfo(id: Long, infoMap: Map<String, Any>) {
        val params = infoMap.map { "${it.key} = ${typeOrNull(it.value)}" }.joinToString(", ")
        //language=MySQL
        updateDatabase("UPDATE Users SET $params WHERE UserId=?", id)
    }
    
    fun addCourse(subject: String, module: String?, course: String, shorthand: String?) {
        //language=MySQL
        updateDatabase("INSERT INTO Courses (Subject, Module, Course, ShortHand) VALUES (?, ?, ?, ?)", subject, module, course, shorthand)
    }

    @Language("MySQL")
    fun getCourseById(id: Long): Course? {
        //language=MySQL
        updateDatabase("DROP TABLE IF EXISTS TempCourses")
        //language=MySQL
        updateDatabase("""
            CREATE TEMPORARY TABLE TempCourses AS
              SELECT Courses.CourseId, Subject, Module, Course, Shorthand, Count(Users.UserId) AS UserCount FROM Courses
              LEFT OUTER JOIN User_Course_Links UCL on Courses.CourseId = UCL.CourseId
              LEFT OUTER JOIN Users on UCL.UserId = Users.UserId
              GROUP BY Courses.CourseId;
        """.trimIndent())
        @Language("MySQL")
        val getCoursesQuery = """
            SELECT TempCourses.CourseId, Subject, Module, Course, Shorthand, UserCount FROM TempCourses
            WHERE TempCourses.CourseId=?
        """.trimIndent()
        return getDatabaseResult(getCoursesQuery, Course, id)?.firstOrNull()
    }

    @Language("MySQL")
    fun getChannelsForCourse(id: Long): Set<Channel> = getDatabaseResult("SELECT ChannelId FROM Channels WHERE CourseId=?", Channel, id) ?: emptySet()

    fun generateTokenForUser(id: Long): String {
        val token = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis() / 1000L
        //language=MySQL
        updateDatabase("INSERT INTO Tokens (Token, UserId, CreationDate) VALUES ('$token', $id, $currentTime)")
        return token
    }

    @Language("MySQL")
    fun getUsers(): Set<User> = getDatabaseResult("SELECT UserId, CurrentSemester, Major, IsAdmin FROM Users", User) ?: emptySet()

    fun addUser(user: User) {
        //language=MySQL
        updateDatabase("INSERT INTO Users (UserId, CurrentSemester, Major) VALUES (?, ?, ?)", user.userId, user.currentSemester, user.mayor)
    }

    @Language("MySQL")
    fun getUsersForCourse(id: Long): Set<User> = getDatabaseResult("""
        SELECT Users.UserId, CurrentSemester, Major, IsAdmin FROM Users
        INNER JOIN User_Course_Links UCL on Users.UserId = UCL.UserId
        WHERE UCL.CourseId=?
    """.trimIndent(), User, id) ?: emptySet()

    fun addChannelToCourse(courseId: Long, channelId: Long) {
        //language=MySQL
        updateDatabase("INSERT INTO Channels (ChannelId, CourseId) VALUES (?, ?)", channelId, courseId)
    }

    @Language("MySQL")
    fun isInDatabase(id: Long): Boolean = hasResult("SELECT * FROM Users WHERE UserId=?", id)

    fun removeUser(id: Long) {
        //language=MySQL
        updateDatabase("DELETE FROM Users WHERE UserId=?", id)
    }

    fun removeCourse(id: Long) {
        //language=MySQL
        updateDatabase("DELETE FROM Channels WHERE CourseId=?", id)
        //language=MySQL
        updateDatabase("DELETE FROM User_Course_Links WHERE CourseId=?", id)
        //language=MySQL
        updateDatabase("DELETE FROM Courses WHERE CourseId=?", id)
    }

    @Suppress("UNCHECKED_CAST")
    fun updateCourseInfo(id: Long, map: Map<String, String?>) {
        val updateMap = map.filterValues { it != null && it != "" } as Map<String, String>
        val update = updateMap.map { "${it.key} = ${typeOrNull(it.value)}" }.joinToString(", ") //TODO: Replace with PreparedStatements
        //language=MySQL
        updateDatabase("UPDATE Courses SET $update WHERE CourseId=?", id)
    }

    fun removeChannel(id: Long) {
        //language=MySQL
        updateDatabase("DELETE FROM Channels WHERE ChannelId=?", id)
    }

    /*
        Utilities
     */

    /**
     * @return the query, if the object is null, returns the word null. Useful for SQL Queries
     */
    private fun <T> typeOrNull(input: T?): String = when (input) {
        null -> "null"
        is String -> "'$input'"
        else -> input.toString()
    }

    /*private fun <T : Any> getDatabaseResult(query: String, function: (ResultSet) -> T): Set<T>? = getOrNull(log) {
        log.trace(query)
        val dbResult = dbConnection.createStatement().executeQuery(query)
        generateSequence { if (dbResult.next()) function(dbResult) else null }.toSet()
    }*/

    private fun <T : Any> getDatabaseResult(query: String, factory: DatabaseItemInterface<T>, vararg args: Any?): Set<T>? = getOrNull(log) {
        val statement = dbConnection.prepareStatement(query).applyArgs(args)
        log.trace(statement.toString())
        val dbResult = statement.executeQuery()
        generateSequence { if (dbResult.next()) factory.generateItem(dbResult) else null }.toSet()
    }

    private fun hasResult(query: String, vararg args: Any?): Boolean = getOrNull(log) {
        val statement = dbConnection.prepareStatement(query).applyArgs(args)
        log.trace(statement.toString())
        statement.executeQuery().next()
    } ?: false

    private fun updateDatabase(query: String, vararg args: Any?): Boolean {
        val result = getOrNull(log) {
            val statement = dbConnection.prepareStatement(query).applyArgs(args)
            log.trace(statement.toString())
            statement.executeUpdate()
        } ?: return false
        return result > 0
    }

    private fun PreparedStatement.applyArgs(args: Array<out Any?>): PreparedStatement = apply {
        args.forEachIndexed { index, argument ->
            when(argument) {
                null -> setNull(index + 1, Types.VARCHAR)
                is Int -> setInt(index + 1, argument)
                is Long -> setLong(index + 1, argument)
                is String -> setString(index + 1, argument)
                else -> {
                    log.warn("One Argument was of type ${argument::class.qualifiedName}. Statement: ${toString()}")
                    setObject(index + 1, argument)
                }
            }
        }
    }

    private fun <T> getOrNull(logger: Logger? = null, function: () -> T): T? = try { function() } catch (e: Exception) { (logger ?: log).info(e.toString()); null }

    interface DatabaseItemInterface<T> {
        fun generateItem(item: ResultSet): T
    }
}
