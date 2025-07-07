package trading.adapters.db

import org.jetbrains.exposed.v1.core.Table
import trading.TradeOrderStatus
import trading.TradeOrderType

object TradeOrders : Table("trade_orders") {
    private val id = integer("id").autoIncrement()
    override val primaryKey = PrimaryKey(id)
    val trackingId = text("tracking_id").uniqueIndex()
    val brokerageAccountId = text("brokerage_account_id").index()
    val type = enumeration("trade_order_type", TradeOrderType::class).index()
    val security = text("trade_order_security").index()
    val numberOfShares = integer("number_of_shares")
    val status = enumeration("trade_order_status", TradeOrderStatus::class).index()
}