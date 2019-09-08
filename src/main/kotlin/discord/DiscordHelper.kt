package discord

import database.DatabaseHelper
import debug
import domain
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import types.Course
import types.Major
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DiscordHelper(val guild: Guild, val databaseHelper: DatabaseHelper) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val roles: List<Role> = guild.roles.filter { role -> Major.values().map { it.roleName }.contains(role.name) }.also {
        log.info(it.joinToString(", ") { it.name })
    }
    private val permissions = mutableListOf(
            Permission.MESSAGE_READ
    )

    suspend fun inviteUser(user: User) {
        if (user.isBot) return
        log.info("Generating an invite for user ${user.asTag}")
        val token = databaseHelper.generateTokenForUser(user.idLong)
        if (!debug) {
            kotlin.runCatching {
                user.openPrivateChannelAsync()
                    .sendMessageAsync("""
                        :wave: Hallo, ich bin der Channel-Manager des TU Wien Informatik Discord-Servers.
                        Um die Channels für deine Lehrveranstaltungen auszuwählen, besuche die unten :link: verlinkte Seite.

                        :wave: Hi, I'm the channel management bot of the Informatik server of the TU Vienna.
                        Please go to the site :link: linked below to select your courses to unlock the corresponding channels.

                        :link: :point_right: <https://$domain/login/token/$token :point_left:>
                    """.trimIndent())
            }
        }
    }

    suspend fun PrivateChannel.sendMessageAsync(message: String, delayMilis: Long = 0): Message = suspendCoroutine { cont ->
        sendMessage(message).queueAfter(delayMilis, TimeUnit.MILLISECONDS, {
            cont.resume(it)
        }, {
            log.warn("Could not send message to user ${user.asTag}. Reason: ${it.localizedMessage}")
        })
    }

    suspend fun User.openPrivateChannelAsync(): PrivateChannel = suspendCoroutine { cont ->
        openPrivateChannel().queue({
            cont.resume(it)
        }, {
            log.warn("Could not open message channel to user $asTag. Reason: ${it.localizedMessage}")
        })
    }

    fun syncUsers() {
        log.info("Syncing users with database")
        val dbUserIds: List<Long> = databaseHelper.getUsers().map { it.userId }
        val serverUserIds: List<Long> = guild.members.map { it.user.idLong }
        val newUserIds: List<Long> = serverUserIds - dbUserIds
        GlobalScope.launch {
            newUserIds.forEach { userId ->
                val user = guild.members.find { it.user.idLong == userId }?.user
                if (user != null && !user.isBot) {
                    databaseHelper.addUser(types.User(user.idLong))
                    inviteUser(user)
                }
                delay(1000L)
            }
        }
    }

    fun updateAllChannelPermissions() {
        log.info("Updating all channel permissions")
        GlobalScope.launch {
            databaseHelper.getCourses().forEach { course ->
                val channels = databaseHelper.getChannelsForCourse(course.courseId).mapNotNull { channel -> guild.channels.find { it.idLong == channel.channelId } }
                val courseUserIds = databaseHelper.getUsersForCourse(course.courseId).map { it.userId }
                val members = guild.members.filter { courseUserIds.contains(it.user.idLong) }
                channels.forEach { channel ->
                    channel.permissionOverrides.filter { it.isMemberOverride }.forEach { permissionOverride ->
                        if (!members.contains(permissionOverride.member)) permissionOverride.delete().complete()
                    }
                    members.forEach { member ->
                        if (channel.permissionOverrides.filter { it.isMemberOverride }.find { it.member == member } == null) {
                            channel.safeAddPermissionOverrideAsync(member, permissions)
                        }
                    }
                }
            }
        }
    }

    fun updateUserChannels(userId: Long) {
        val member = guild.members.find { it.user.idLong == userId } ?: throw Exception("Did not find the User with the ID $userId on the server ${guild.name}")
        log.info("Permission update initiated for user ${member.user.asTag}")
        val subscribedCourseIds = databaseHelper.getCoursesForUser(userId).map { it.courseId }
        val subscribedChannelIds = subscribedCourseIds.map { databaseHelper.getChannelsForCourse(it) }.flatten().map { it.channelId }
        GlobalScope.launch {
            guild.channels.forEach { channel ->
                if (subscribedChannelIds.contains(channel.idLong)) {
                    channel.safeAddPermissionOverrideAsync(member, permissions)
                } else {
                    channel.safeRemovePermissionOverrideAsync(member)
                }
                delay(100L)
            }
        }
    }

    private suspend fun GuildChannel.safeAddPermissionOverrideAsync(member: Member, permissions: MutableList<Permission>, delayMilis: Long = 0): Boolean = if (this.getPermissionOverride(member) == null) suspendCoroutine { cont ->
        val override = createPermissionOverride(member)
        override.setAllow(permissions).queueAfter(
            delayMilis,
            TimeUnit.MILLISECONDS,
            {
                cont.resume(true)
            },
            {
                log.warn("Could not set permission overrides. Reason: ${it.localizedMessage}")
            }
        )
    } else false

    private suspend fun GuildChannel.safeRemovePermissionOverrideAsync(member: Member, delayMilis: Long = 0): Boolean = if (this.getPermissionOverride(member) != null) suspendCoroutine { cont ->
        getPermissionOverride(member).delete().queueAfter(
            delayMilis,
            TimeUnit.MILLISECONDS,
            {
                cont.resume(true)
            },
            {
                log.warn("Could not set permission overrides. Reason: ${it.localizedMessage}")
            }
        )
    } else false

    fun addChannelForCourse(course: Course) {
        log.info("Adding a channel for the course ${course.course}")
        guild.controller.createTextChannel(course.shorthand ?: course.course.filter { it.isUpperCase() || it.isDigit()})
            .addPermissionOverride(guild.roles.find { it.name == "@everyone" }, mutableListOf<Permission>(), permissions)
            .addPermissionOverride(guild.roles.find { it.name == "Bot" }, permissions, mutableListOf<Permission>())
            .addPermissionOverride(guild.roles.find { it.name == "Channel Inspector" }, permissions, mutableListOf<Permission>())
            .setTopic(arrayOf(course.course, course.module, course.subject).filterNotNull().joinToString(" - "))
            .setParent(getCategory(course.subject))
            .queue({ channel ->
                guild.getTextChannelById(channel.idLong)
                    .sendMessage("Welcome on the newly created channel for ${course.course}!").queue()
                databaseHelper.addChannelToCourse(course.courseId, channel.idLong)
                log.info("Added a channel")
            }, { exception ->
                log.warn("Could not add new channel. Reason: ${exception.localizedMessage}")
            })
    }

    private fun getCategory(subject: String): Category {
        if (guild.getCategoriesFiltered(subject).isEmpty()) {
            guild.controller.createCategory(subject).complete()
        }
        return guild.getCategoriesFiltered(subject).first()
    }

    private fun Guild.getCategoriesFiltered(subject: String): List<Category> = getCategoriesByName(subject, true).filter { it.channels.size < 50 }

    fun addChannels() {
        val courses = databaseHelper.getCourses()
        courses.filter { it.userCount > 1 }.filter { databaseHelper.getChannelsForCourse(it.courseId).isEmpty() }.forEach {
            addChannelForCourse(it)
        }
    }

    val summerStart = OffsetDateTime.of(LocalDate.of(2019, 7, 1), LocalTime.MIDNIGHT, ZoneOffset.UTC)

    fun updateUserProfile(userId: Long, onlyErsti: Boolean = false) {
        val dbUser = databaseHelper.getUserById(userId)
        val discordMember = guild.members.find { it.user.idLong == userId } ?: throw RuntimeException("User $userId not found on server")
        guild.controller.run {
            if (!onlyErsti) {
                removeRolesFromMember(discordMember, roles).complete()
                roles.find { it.name == dbUser?.mayor?.roleName }?.let { addSingleRoleToMember(discordMember, it).complete() }
            }
            guild.roles.find { it.name == "Ersti" }?.also { erstiRole ->
                if (dbUser?.currentSemester == 1 || (dbUser?.currentSemester == 0 && discordMember.timeJoined.isAfter(summerStart))) {
                    if (!discordMember.roles.contains(erstiRole)) {
                        log.info("Added Ersti Role to member ${discordMember.user.asTag}")
                        addSingleRoleToMember(discordMember, erstiRole).complete()
                    }
                } else {
                    if (discordMember.roles.contains(erstiRole)) {
                        log.info("Removed Ersti Role from member ${discordMember.user.asTag}")
                        removeSingleRoleFromMember(discordMember, erstiRole).complete()
                    }
                }
            }
        }
    }

    fun updateAllUserProfiles() {
        GlobalScope.launch {
            guild.members.forEach { member ->
                updateUserProfile(member.user.idLong, true)
                delay(500L)
            }
        }
    }
}
