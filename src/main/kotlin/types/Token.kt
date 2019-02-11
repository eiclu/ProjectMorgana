package types

import database.DatabaseHelper.DatabaseItemInterface
import java.sql.ResultSet

data class Token(
    val userId: Long,
    val timeSeconds: Long
) {
    companion object : DatabaseItemInterface<Token> {
        override fun generateItem(item: ResultSet) = Token(
            item.getLong("UserId"),
            item.getLong("CreationDate")
        )
    }
}