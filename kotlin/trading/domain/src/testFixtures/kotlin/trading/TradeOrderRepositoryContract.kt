package trading

import trading.TradeOrderStatus.FULFILLED
import trading.TradeOrderStatus.OUTSTANDING
import trading.TradeOrderType.BUY_ORDER
import trading.TradeOrderType.SELL_ORDER
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class TradeOrderRepositoryContract {

    @Test
    fun `returns the TradeOrder if it exists for the given tracking ID`() {
        givenExistingTradeOrders(
            TradeOrder(TrackingId("t123"), BrokerageAccountId("123"), BUY_ORDER, Security("AMZN"), 10, OUTSTANDING),
            TradeOrder(TrackingId("t456"), BrokerageAccountId("123"), SELL_ORDER, Security("WCOM"), 50, FULFILLED),
            TradeOrder(TrackingId("t789"), BrokerageAccountId("123"), SELL_ORDER, Security("GOOG"), 25, FULFILLED)
        )

        val repository = createTradeOrderRepository()

        val tradeOrder = repository.forTrackingId(TrackingId("t456"))

        assertEquals(
            TradeOrder(
                TrackingId("t456"),
                BrokerageAccountId("123"),
                SELL_ORDER,
                Security("WCOM"),
                50,
                FULFILLED
            ),
            tradeOrder
        )
    }

    @Test
    fun `returns null if the TradeOrder is not found for the given tracking ID`() {
        givenExistingTradeOrders(
            TradeOrder(TrackingId("t123"), BrokerageAccountId("123"), BUY_ORDER, Security("AMZN"), 10, OUTSTANDING),
            TradeOrder(TrackingId("t456"), BrokerageAccountId("123"), SELL_ORDER, Security("WCOM"), 50, FULFILLED),
            TradeOrder(TrackingId("t789"), BrokerageAccountId("123"), SELL_ORDER, Security("GOOG"), 25, FULFILLED)
        )

        val repository = createTradeOrderRepository()

        val tradeOrder = repository.forTrackingId(TrackingId("t999"))

        assertNull(tradeOrder)
    }

    @Test
    fun `returns an empty list if no TradeOrder was found for the given account ID`() {
        givenExistingTradeOrders(
            TradeOrder(TrackingId("t123"), BrokerageAccountId("123"), BUY_ORDER, Security("AMZN"), 10, OUTSTANDING),
            TradeOrder(TrackingId("t456"), BrokerageAccountId("123"), SELL_ORDER, Security("WCOM"), 50, FULFILLED),
            TradeOrder(TrackingId("t789"), BrokerageAccountId("123"), SELL_ORDER, Security("GOOG"), 25, FULFILLED)
        )

        val repository = createTradeOrderRepository()

        val tradeOrders = repository.outstandingForBrokerageAccountId(BrokerageAccountId("987"))

        assertEquals(tradeOrders, emptyList())
    }

    @Test
    fun `returns all outstanding TradeOrders for the given account ID`() {
        givenExistingTradeOrders(
            TradeOrder(TrackingId("t123"), BrokerageAccountId("123"), BUY_ORDER, Security("AMZN"), 10, OUTSTANDING),
            TradeOrder(TrackingId("t456"), BrokerageAccountId("123"), SELL_ORDER, Security("WCOM"), 50, FULFILLED),
            TradeOrder(TrackingId("t789"), BrokerageAccountId("123"), SELL_ORDER, Security("GOOG"), 25, FULFILLED),
            TradeOrder(TrackingId("t100"), BrokerageAccountId("344"), BUY_ORDER, Security("AMZN"), 15, OUTSTANDING),
            TradeOrder(TrackingId("t200"), BrokerageAccountId("344"), BUY_ORDER, Security("GOOG"), 25, OUTSTANDING),
            TradeOrder(TrackingId("t300"), BrokerageAccountId("123"), SELL_ORDER, Security("AMZN"), 75, OUTSTANDING),
        )

        val repository = createTradeOrderRepository()

        val tradeOrders = repository.outstandingForBrokerageAccountId(BrokerageAccountId("123"))

        assertEquals(
            listOf(
                TradeOrder(TrackingId("t123"), BrokerageAccountId("123"), BUY_ORDER, Security("AMZN"), 10, OUTSTANDING),
                TradeOrder(TrackingId("t300"), BrokerageAccountId("123"), SELL_ORDER, Security("AMZN"), 75, OUTSTANDING)
            ),
            tradeOrders
        )
    }

    protected abstract fun createTradeOrderRepository(): TradeOrderRepository

    protected abstract fun givenExistingTradeOrders(tradeOrder: TradeOrder, vararg tradeOrders: TradeOrder)
}