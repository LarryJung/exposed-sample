import org.jetbrains.exposed.sql.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * @author Larry
 */
fun main() {
    val db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    val txHandler = TxHandlerImpl(db)
    txHandler.tx {
        SchemaUtils.create(GikoUsers, Givers, Gikos)
    }

    val param = GikoAdminSearchParam(giverPayAccountId = 0L)

    txHandler.tx {

        val qGiver = Givers.alias("gv")
        val qReceiverGikoUser = GikoUsers.alias("rgu")
        val qGiverGikoUser = GikoUsers.alias("ggu")

        val query = Gikos
            .innerJoin(
                qGiver,
                onColumn = { giverId },
                otherColumn = { qGiver[Givers.id] })
            .innerJoin(
                qGiverGikoUser,
                onColumn = { qGiver[Givers.gikoUserId] },
                otherColumn = { qGiverGikoUser[GikoUsers.id] })
            .innerJoin(
                qReceiverGikoUser,
                onColumn = { Gikos.gikoUserId },
                otherColumn = { qReceiverGikoUser[GikoUsers.id] })
            .slice(
                Gikos.gikoSerialId,
                Gikos.balance,
                Gikos.issuedDate,
                qGiverGikoUser[GikoUsers.payAccountId],
                Expression.build { qGiverGikoUser[GikoUsers.accountId].lessEq(0) },
                qReceiverGikoUser[GikoUsers.payAccountId],
                qReceiverGikoUser[GikoUsers.accountId],
                Expression.build { qReceiverGikoUser[GikoUsers.accountId].lessEq(0) },
                qReceiverGikoUser[GikoUsers.available],
                Gikos.createdAt
            )
            .selectAll()

        param.giverPayAccountId?.let {
            query.andWhere { qGiverGikoUser[GikoUsers.payAccountId].eq(it) }
        }
        param.receiverPayAccountId?.let {
            query.andWhere { qReceiverGikoUser[GikoUsers.payAccountId].eq(it) }
        }
        param.gikoSerialId?.let {
            query.andWhere { Gikos.gikoSerialId.eq(it) }
        }
        param.issuedDateStart?.let {
            query.andWhere { Gikos.issuedDate.greaterEq(it) }
        }
        query.map {
            it[Gikos.id]
        }
    }
}


data class GikoAdminSearchParam(
    val giverPayAccountId: Long? = null,
    val receiverPayAccountId: Long? = null,
    val gikoSerialId: String? = null,
    val gikoStatus: String? = null,
    val issuedDateStart: LocalDate? = null,
    val issuedDateEnd: LocalDate? = null,
    override var page: Int? = 0,
    override var pageSize: Int? = 20,
) : AdminPagingBase


interface AdminPagingBase {
    var page: Int?
    var pageSize: Int?

    fun getOffSet(): Long = (page!! * pageSize!!).toLong()
    fun getLimit(): Long = pageSize!!.toLong()
}

data class AdminGikoDto(
    val gikoSerialId: String,
    val balance: Int,
    val gikoStatus: GikoStatus,
    val issuedDate: LocalDate,
    val giverPayAccountId: Long?,
    val giverSignOut: Boolean,
    val receiverAccountIdPair: AdminAccountIdPair,
    val receiverSignOut: Boolean,
    val available: Boolean,
    val createdAt: LocalDateTime,
)

data class AdminAccountIdPair(
    val payAccountId: Long?,
    val accountId: Int?,
)