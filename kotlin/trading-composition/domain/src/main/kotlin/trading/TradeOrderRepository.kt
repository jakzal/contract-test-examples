package trading

interface TradeOrderRepository {
    fun forTrackingId(trackingId: TrackingId): TradeOrder?
    fun outstandingForBrokerageAccountId(brokerageAccountId: BrokerageAccountId): List<TradeOrder>
}
