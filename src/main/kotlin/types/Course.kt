package types

import database.DatabaseHelper
import java.sql.ResultSet

data class Course(
        val courseId: Long,
        val course: String,
        val module: String?,
        val subject: String,
        val shorthand: String? = null,
        val userCount: Int = 0,
        val enabled: Boolean = false
) {
        companion object : DatabaseHelper.DatabaseItemInterface<Course> {
                override fun generateItem(item: ResultSet) = Course(
                        item.getLong("CourseId"),
                        item.getString("Course"),
                        item.getString("Module"),
                        item.getString("Subject"),
                        item.getString("Shorthand"),
                        item.getInt("UserCount")
                )
        }
}