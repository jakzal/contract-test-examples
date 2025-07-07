package trading.adapters.db

import trading.BrokerageAccountId
import trading.TrackingId
import trading.TradeOrder
import trading.TradeOrderRepository
import trading.TradeOrderStatus.OUTSTANDING

class InMemoryTradeOrderRepository(private val tradeOrders: List<TradeOrder>) : TradeOrderRepository {
    override fun forTrackingId(trackingId: TrackingId) = tradeOrders.find { it.trackingId == trackingId }

    override fun outstandingForBrokerageAccountId(brokerageAccountId: BrokerageAccountId) =
        tradeOrders
            .filter { tradeOrder -> tradeOrder.brokerageAccountId == brokerageAccountId }
            .filter { tradeOrder -> tradeOrder.status == OUTSTANDING }
}