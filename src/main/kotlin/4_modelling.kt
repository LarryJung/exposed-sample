import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * @author Larry
 */
fun main() {
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(GikoUsers, Givers, Gikos)
    }
}

// === table config. dsl 에 활용. 복수 컨벤션 ==============================================================================
object GikoUsers : BaseLongIdTable("giko_user", "giko_user_id") {
    val payAccountId = long("pay_account_id").nullable()
    val accountId = integer("account_id")
    val available = bool("available")
}

object Givers : BaseLongIdTable("giver", "giver_id") {
    val gikoUserId = reference("giko_user_id", GikoUsers.id, onDelete = null, onUpdate = null).nullable()
    var partnerId = long("partner_id").nullable()
}

object Gikos : BaseLongIdTable("giko", "giko_id") {
    val gikoUserId = reference("giko_user_id", GikoUsers.id, onDelete = null, onUpdate = null)
    val giverId = reference("giver_id", Givers.id)
    val gikoStatus = enumerationByName("giko_status", 50, GikoStatus::class)
    val gikoSerialId = varchar("giko_serial_id", 50)
    val balance = integer("balance")
    val issuedDate = date("issued_date").clientDefault { LocalDate.now() }
}


// === dao config. 단수 컨벤션. var 로만.. =================================================================================
class GikoUser(id: EntityID<Long>) : BaseEntity(id, GikoUsers) {
    companion object : BaseEntityClass<GikoUser>(GikoUsers)

    var payAccountId by GikoUsers.payAccountId
    var accountId by GikoUsers.accountId
    var available by GikoUsers.available
}

class Giver(id: EntityID<Long>) : BaseEntity(id, Givers) {
    companion object : BaseEntityClass<Giver>(Givers)

    var gikoUser by GikoUser optionalReferencedOn Givers.gikoUserId
    var partnerId by Givers.partnerId
}

class Giko(id: EntityID<Long>) : BaseEntity(id, Gikos) {
    companion object : BaseEntityClass<Giko>(Gikos)

    var gikoUser by GikoUser referencedOn Gikos.gikoUserId
    var giver by Giver referencedOn Gikos.giverId
    var gikoStatus by Gikos.gikoStatus
    var gikoSerialId by Gikos.gikoSerialId
    var balance by Gikos.balance
    var issuedDate by Gikos.issuedDate
}


// === UTILS ===========================================================================================================

abstract class BaseLongIdTable(name: String, idName: String = "id") : LongIdTable(name, idName) {
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

abstract class BaseEntity(id: EntityID<Long>, table: BaseLongIdTable) : LongEntity(id) {
    val createdAt by table.createdAt
    var updatedAt by table.updatedAt
}

abstract class BaseEntityClass<E : BaseEntity>(table: BaseLongIdTable) : LongEntityClass<E>(table) {

    init {
        EntityHook.subscribe { action ->
            if (action.changeType == EntityChangeType.Updated) {
                try {
                    action.toEntity(this)?.updatedAt = LocalDateTime.now()
                } catch (e: Exception) {
                    //nothing much to do here
                }
            }
        }
    }
}

val BaseEntity.idValue: Long
    get() = this.id.value


enum class GikoStatus(val description: String) {
    CREATED("생성"),
    PUBLISHED("발행"),
    CANCELED("발행 취소"),
    RECEIVED("수락"),

    EXPIRED_REFUNDED("유효기간 만료 환불"),
    REQUESTED_REFUNDED("사용자 요청 잔액 환불"),
    NON_RECEIPT_REFUNDED("미수신 환불"),

    PAYMENT_FAILED("결제 실패"),
    PAYMENT_PENDING("결제 확인 중"),

    ADMIN_FORCE_REFUNDED("어드민 강제 만료(머니환불)"),
    ADMIN_MANUAL_REFUNDED("어드민 강제 만료(수기환불)");
}