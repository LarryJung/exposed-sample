import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils

/**
 * @author Larry
 */
fun main() {
    val db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    val txHandler = TxHandlerImpl(db)

    txHandler.tx {
        SchemaUtils.create(GikoUsers, Givers, Gikos)
    }

    txHandler.tx {
        repeat(10) {
            GikoUser.new {
                payAccountId = it.toLong()
                accountId = it
                available = true
            }
        }

        val giverGikoUser = GikoUser.new {
            payAccountId = 100L
            accountId = 100
            available = true
        }
        val giver = Giver.new {
            gikoUser = giverGikoUser
        }
        repeat(10) {
            Giko.new {
                gikoUser = GikoUser[it.toLong() + 1]
                this.giver = giver
                gikoStatus = GikoStatus.CREATED
                gikoSerialId = "serial"
                balance = 1000
            }
        }
    }

    println("lazy loading ====")
    txHandler.tx {
        val giko = Giko[1L]
        println(giko.giver)
        println(giko.giver.gikoUser)
    }

    println("eager loading 단건 ====")
    txHandler.tx {
        val giko = Giko[1L].load(Giko::gikoUser, Giko::giver, Giver::gikoUser)
        println(giko.giver)
        println(giko.giver)
        println(giko.giver.gikoUser)
    }

    println("eager loading 여러건 ====")
    txHandler.tx {
        println(Giko.all().with(Giko::gikoUser, Giko::giver, Giver::gikoUser).map { it.gikoUser.payAccountId })
    }
}