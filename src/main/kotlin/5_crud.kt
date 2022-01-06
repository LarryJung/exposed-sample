import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * @author Larry
 */
fun main() {
    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(GikoUsers, Givers, Gikos)
    }

    // insert
    transaction {
        addLogger(StdOutSqlLogger)
        // dsl
        GikoUsers.insert {
            it[payAccountId] = 0L
            it[accountId] = 0
            it[available] = true
        }

        // dao
        GikoUser.new {
            payAccountId = 1L
            accountId = 1
            available = true
        }


    }
    transaction {
        addLogger(StdOutSqlLogger)

        val gikoUserByDAO = GikoUser.find { GikoUsers.payAccountId.eq(1L) }.first() // dao, GikoUser
        println(gikoUserByDAO.available) // true
    }

    transaction {
        addLogger(StdOutSqlLogger)

        // dsl, ResultRow
        val gikoUserByDSL = GikoUsers.select { GikoUsers.payAccountId.eq(0L) }.first()

        // dao, GikoUser
        val gikoUserByDAO = GikoUser.find { GikoUsers.payAccountId.eq(1L) }.first()

        println("get dsl id: ${gikoUserByDSL[GikoUsers.payAccountId]}")
        println("get dao id: ${gikoUserByDAO.payAccountId}")

        gikoUserByDAO.available = false
    }

    transaction {
        addLogger(StdOutSqlLogger)

        val gikoUserByDAO = GikoUser.find { GikoUsers.payAccountId.eq(1L) }.first()
        println(gikoUserByDAO.available) // false

        gikoUserByDAO.delete()
    }

    transaction {
        addLogger(StdOutSqlLogger)
        GikoUsers.deleteAll()
    }
}
