package at.morgana

import com.machinepublishers.jbrowserdriver.JBrowserDriver
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.sql.Connection

class Parser(val driver: JBrowserDriver, val dbConnection: Connection) {
    fun parseTable(url: String) {
        driver.get(url)

        val waiter = WebDriverWait(driver, 10)
        waiter.until(ExpectedConditions.visibilityOfElementLocated(By.id("j_id_2b")))

        val rows = driver.findElementsByXPath("//form[@id='j_id_2b']//table//tr//div")
        val courses: MutableSet<Course> = mutableSetOf()

        var subject: String? = null
        var module: String? = null
        var course: String? = null

        for(row in rows) {
            val rowClass = row.getAttribute("class")
            if (rowClass.contains("nodeTable-level-1")) {
                subject = Regex("Prüfungsfach ").replaceFirst(row.text, "")
                module = null
                course = null
            }
            if (rowClass.contains("nodeTable-level-2") && !rowClass.contains("item")) {
                module = Regex("Modul ").replaceFirst(row.text, "")
                course = null
            }
            if (rowClass.contains("item")) {
                course = Regex("[A-Z]{2} ").replaceFirst(row.text, "")
            }
            if (subject != null && course != null) {
                courses.add(Course(subject, module, course))
            }
        }

        val values = courses.joinToString("), (") {
            "'${it.course}', ${if (it.module == null) "null" else "'${it.module}'"}, '${it.subject}'"
        }

        val query = "INSERT IGNORE INTO Courses (Course, Module, Subject) VALUES ($values)"
        //println(query)
        dbConnection.createStatement().executeUpdate(query)
        println("Done.")
    }
}

