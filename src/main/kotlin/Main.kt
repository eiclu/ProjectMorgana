import database.DatabaseHelper
import discord.DiscordListener
import io.ktor.server.jetty.EngineMain
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder

val domain: String = System.getenv("URL")
val clientId: String = System.getenv("DISCORD_CLIENTID")
val clientSecret: String = System.getenv("DISCORD_CLIENTSECRET")
val databaseHelper: DatabaseHelper = DatabaseHelper()
val chatInterface: DiscordListener = DiscordListener(System.getenv("DISCORD_GUILDID").toLong(), databaseHelper)
val jdaInstance: JDA = JDABuilder(System.getenv("DISCORD_BOTTOKEN")).build().apply { this.addEventListener(chatInterface) }
val debug = false

fun main() { EngineMain.main(arrayOf()) }

