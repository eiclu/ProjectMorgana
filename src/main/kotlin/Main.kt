import database.DatabaseHelper
import discord.DiscordListener
import io.ktor.server.jetty.EngineMain
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

val domain: String = System.getenv("URL")
val clientId: String = System.getenv("DISCORD_CLIENTID")
val clientSecret: String = System.getenv("DISCORD_CLIENTSECRET")
val databaseHelper: DatabaseHelper = DatabaseHelper()
val chatInterface: DiscordListener = DiscordListener(System.getenv("DISCORD_GUILDID").toLong(), databaseHelper)
val jdaInstance: JDA = JDABuilder.create(System.getenv("DISCORD_BOTTOKEN"), listOf(
    GatewayIntent.GUILD_MEMBERS,
    GatewayIntent.DIRECT_MESSAGES
)).addEventListeners(chatInterface).build()
val debug = domain == "localhost:4567"

fun main() { EngineMain.main(arrayOf()) }

