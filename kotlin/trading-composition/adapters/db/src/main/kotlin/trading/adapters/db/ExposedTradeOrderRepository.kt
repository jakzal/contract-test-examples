package trading.adapters.db

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import trading.*
import trading.TradeOrderStatus.OUTSTANDING

class ExposedTradeOrderRepository(private val db: Database) : TradeOrderRepository {
    override fun forTrackingId(trackingId: TrackingId): TradeOrder? = transaction(db) {
        TradeOrders
            .selectAll()
            .where { TradeOrders.trackingId eq trackingId.value }
            .singleOrNull()
            .asTradeOrder()
    }

    override fun outstandingForBrokerageAccountId(brokerageAccountId: BrokerageAccountId): List<TradeOrder> =
        transaction(db) {
            TradeOrders
                .selectAll()
                .where { TradeOrders.brokerageAccountId eq brokerageAccountId.value }
                .andWhere { TradeOrders.status eq OUTSTANDING }
                .mapNotNull { it.asTradeOrder() }
        }

    private fun ResultRow?.asTradeOrder(): TradeOrder? = this?.let {
        TradeOrder(
            TrackingId(it[TradeOrders.trackingId]),
            BrokerageAccountId(it[TradeOrders.brokerageAccountId]),
            it[TradeOrders.type],
            Security(it[TradeOrders.security]),
            it[TradeOrders.numberOfShares],
            it[TradeOrders.status]
        )
    }
}