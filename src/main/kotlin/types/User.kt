package types

import database.DatabaseHelper
import java.sql.ResultSet

data class User(
        val userId: Long,
        val currentSemester: Int? = null,
        val mayor: String? = null,
        val isAdmin: Boolean = false,
        val userName: String? = null,
        val userTag: String? = null,
        val userImageUrl: String? = null
) {
        companion object : DatabaseHelper.DatabaseItemInterface<User> {
                override fun generateItem(item: ResultSet) = User(
                        item.getLong("UserId"),
                        item.getInt("CurrentSemester"),
                        item.getString("Major"),
                        item.getBoolean("IsAdmin")
                )
        }
}