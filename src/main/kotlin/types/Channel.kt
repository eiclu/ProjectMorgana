package types

import database.DatabaseHelper
import java.sql.ResultSet

data class Channel(
    val channelId: Long,
    val name: String
) {
    companion object : DatabaseHelper.DatabaseItemInterface<Channel> {
        override fun generateItem(item: ResultSet): Channel {
            val channel = item.getLong("channelId")
            return Channel(channel, channel.toString())
        }
    }
}