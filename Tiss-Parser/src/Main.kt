package at.morgana

import com.machinepublishers.jbrowserdriver.JBrowserDriver
import java.sql.DriverManager


fun main() {
    val dbConnection = DriverManager.getConnection("jdbc:${System.getenv("DB_URL")}", System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD"))
    val driver = JBrowserDriver()
    val parser = Parser(driver, dbConnection)
    arrayOf(/*45669, 45860, 46100, 46319, 45300, */57489).map { "https://tiss.tuwien.ac.at/curriculum/public/curriculum.xhtml?key=$it" }.forEach {
        parser.parseTable(it)
    }
}