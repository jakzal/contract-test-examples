package trading

import trading.TradeOrderStatus.FULFILLED
import trading.TradeOrderStatus.OUTSTANDING
import trading.TradeOrderType.BUY_ORDER
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class TradeOrderRepositoryContract {

    @Test
    fun `returns the TradeOrder if it exists for the given tracking ID`() {
        val matching = tradeOrder(trackingId = TrackingId("t456"))

        val repository = tradeOrderRepositoryWith(
            tradeOrder(trackingId = TrackingId("t123")),
            matching,
            tradeOrder(trackingId = TrackingId("t789"))
        )

        val found = repository.forTrackingId(TrackingId("t456"))

        assertEquals(matching, found)
    }

    @Test
    fun `returns null if the TradeOrder is not found for the given tracking ID`() {
        val repository = tradeOrderRepositoryWith(
            tradeOrder(trackingId = TrackingId("t123")),
            tradeOrder(trackingId = TrackingId("t456")),
            tradeOrder(trackingId = TrackingId("t789"))
        )

        val tradeOrder = repository.forTrackingId(TrackingId("t999"))

        assertNull(tradeOrder)
    }

    @Test
    fun `returns an empty list if no TradeOrder was found for the given account ID`() {
        val repository = tradeOrderRepositoryWith(
            tradeOrder(brokerageAccountId = BrokerageAccountId("123"), status = FULFILLED),
            tradeOrder(brokerageAccountId = BrokerageAccountId("123"), status = FULFILLED),
            tradeOrder(brokerageAccountId = BrokerageAccountId("123"), status = FULFILLED)
        )

        val tradeOrders = repository.outstandingForBrokerageAccountId(BrokerageAccountId("987"))

        assertEquals(tradeOrders, emptyList())
    }

    @Test
    fun `returns all outstanding TradeOrders for the given account ID`() {
        val matchingBothAccountIdAndStatus =
            tradeOrder(brokerageAccountId = BrokerageAccountId("123"), status = OUTSTANDING)
        val anotherMatchingBotAccountIdAndStatus =
            tradeOrder(brokerageAccountId = BrokerageAccountId("123"), status = OUTSTANDING)
        val matchingAccountIdButFulfilled =
            tradeOrder(brokerageAccountId = BrokerageAccountId("123"), status = FULFILLED)
        val outstandingButAnyOtherAccountId =
            tradeOrder(brokerageAccountId = BrokerageAccountId("344"), status = OUTSTANDING)
        val anotherMatchingAccountIdButFulfilled =
            tradeOrder(brokerageAccountId = BrokerageAccountId("123"), status = FULFILLED)

        val repository = tradeOrderRepositoryWith(
            matchingBothAccountIdAndStatus,
            matchingAccountIdButFulfilled,
            anotherMatchingAccountIdButFulfilled,
            outstandingButAnyOtherAccountId,
            anotherMatchingBotAccountIdAndStatus,
        )

        val tradeOrders = repository.outstandingForBrokerageAccountId(BrokerageAccountId("123"))

        assertEquals(listOf(matchingBothAccountIdAndStatus, anotherMatchingBotAccountIdAndStatus), tradeOrders)
    }

    private fun tradeOrder(
        trackingId: TrackingId? = null,
        brokerageAccountId: BrokerageAccountId? = null,
        type: TradeOrderType? = null,
        security: Security? = null,
        numberOfShares: Int? = null,
        status: TradeOrderStatus? = null,
    ): TradeOrder =
        TradeOrder(
            trackingId.orGenerate(),
            brokerageAccountId.orGenerate(),
            type.orRandom(),
            security.orRandom(),
            numberOfShares.orRandom(),
            status.orRandom()
        )

    protected abstract fun tradeOrderRepositoryWith(
        tradeOrder: TradeOrder,
        vararg tradeOrders: TradeOrder
    ): TradeOrderRepository
}

private val lastTrackingId = AtomicInteger(1000)

private fun TrackingId?.orGenerate() = this ?: TrackingId("t${lastTrackingId.incrementAndGet()}")
private val lastBrokerageAccountId = AtomicInteger(2000)

private fun BrokerageAccountId?.orGenerate() = this ?: BrokerageAccountId("${lastBrokerageAccountId.incrementAndGet()}")

private fun TradeOrderStatus?.orRandom() = this ?: TradeOrderStatus.values().random()

private fun TradeOrderType?.orRandom() = this ?: BUY_ORDER

private fun Security?.orRandom() = this ?: Security(listOf("AMZN", "WCOM", "GOOG").random())

private fun Int?.orRandom() = this ?: (1..500).random()
