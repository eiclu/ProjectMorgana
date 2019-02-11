package discord

import database.DatabaseHelper
import interfaces.ChatInterface
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DiscordListener(val guildId: Long, val databaseHelper: DatabaseHelper) : ListenerAdapter(), ChatInterface {
    private val LOG: Logger = LoggerFactory.getLogger(this::class.java)

    lateinit var discordHelper: DiscordHelper
    override fun onReady(event: ReadyEvent) {
        LOG.info("Discord Bot Initializing")
        discordHelper = DiscordHelper(
            event.jda.guilds.find { it.idLong == guildId } ?: throw Exception("GuildNotFoundException"),
            databaseHelper
        )
        discordHelper.addChannels()
        discordHelper.syncUsers()
        discordHelper.updateAllChannelPermissions()
        LOG.info("Discord Bot Ready")
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        LOG.debug("A new member joined the guild: ${event.user.name}")
        if (!databaseHelper.isInDatabase(event.user.idLong)) {
            GlobalScope.launch { discordHelper.inviteUser(event.user) }
        }
    }

    override fun onUserUpdateChannels(userId: Long) {
        discordHelper.addChannels()
        discordHelper.updateUserChannels(userId)
    }

    override fun onChannelsUpdated() {

    }

    override fun onTextChannelDelete(event: TextChannelDeleteEvent) {
        databaseHelper.removeChannel(event.channel.idLong)
    }

    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        GlobalScope.launch { discordHelper.inviteUser(event.author) }
    }
}