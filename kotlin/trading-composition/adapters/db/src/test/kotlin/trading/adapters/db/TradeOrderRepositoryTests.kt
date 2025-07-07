package trading.adapters.db

import org.junit.jupiter.params.ParameterizedClass
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import trading.*
import trading.TradeOrderStatus.FULFILLED
import trading.TradeOrderStatus.OUTSTANDING
import trading.TradeOrderType.BUY_ORDER
import trading.TradeOrderType.SELL_ORDER
import java.util.stream.Stream
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

interface TradeOrderRepositoryFixture {
    fun createTradeOrderRepository(): TradeOrderRepository
    fun givenExistingTradeOrders(tradeOrder: TradeOrder, vararg tradeOrders: TradeOrder)
    fun beforeTest()
}

@ParameterizedClass
@MethodSource("fixtures")
class TradeOrderRepositoryTests(
    val fixture: TradeOrderRepositoryFixture
) {

    companion object {
        @JvmStatic
        fun fixtures(): Stream<Arguments> = Stream.of(
            arguments(ExposedTradeOrderRepositoryFixture()),
            arguments(InMemoryTradeOrderRepositoryFixture())
        )
    }

    @BeforeTest
    fun setUp() {
        fixture.beforeTest()
    }

    @Test
    fun `returns the TradeOrder if it exists for the given tracking ID`() {
        fixture.givenExistingTradeOrders(
            TradeOrder(TrackingId("t123"), BrokerageAccountId("123"), BUY_ORDER, Security("AMZN"), 10, OUTSTANDING),
            TradeOrder(TrackingId("t456"), BrokerageAccountId("123"), SELL_ORDER, Security("WCOM"), 50, FULFILLED),
            TradeOrder(TrackingId("t789"), BrokerageAccountId("123"), SELL_ORDER, Security("GOOG"), 25, FULFILLED)
        )

        val repository = fixture.createTradeOrderRepository()

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
        fixture.givenExistingTradeOrders(
            TradeOrder(TrackingId("t123"), BrokerageAccountId("123"), BUY_ORDER, Security("AMZN"), 10, OUTSTANDING),
            TradeOrder(TrackingId("t456"), BrokerageAccountId("123"), SELL_ORDER, Security("WCOM"), 50, FULFILLED),
            TradeOrder(TrackingId("t789"), BrokerageAccountId("123"), SELL_ORDER, Security("GOOG"), 25, FULFILLED)
        )

        val repository = fixture.createTradeOrderRepository()

        val tradeOrder = repository.forTrackingId(TrackingId("t999"))

        assertNull(tradeOrder)
    }

    @Test
    fun `returns an empty list if no TradeOrder was found for the given account ID`() {
        fixture.givenExistingTradeOrders(
            TradeOrder(TrackingId("t123"), BrokerageAccountId("123"), BUY_ORDER, Security("AMZN"), 10, OUTSTANDING),
            TradeOrder(TrackingId("t456"), BrokerageAccountId("123"), SELL_ORDER, Security("WCOM"), 50, FULFILLED),
            TradeOrder(TrackingId("t789"), BrokerageAccountId("123"), SELL_ORDER, Security("GOOG"), 25, FULFILLED)
        )

        val repository = fixture.createTradeOrderRepository()

        val tradeOrders = repository.outstandingForBrokerageAccountId(BrokerageAccountId("987"))

        assertEquals(tradeOrders, emptyList())
    }

    @Test
    fun `returns all outstanding TradeOrders for the given account ID`() {
        fixture.givenExistingTradeOrders(
            TradeOrder(TrackingId("t123"), BrokerageAccountId("123"), BUY_ORDER, Security("AMZN"), 10, OUTSTANDING),
            TradeOrder(TrackingId("t456"), BrokerageAccountId("123"), SELL_ORDER, Security("WCOM"), 50, FULFILLED),
            TradeOrder(TrackingId("t789"), BrokerageAccountId("123"), SELL_ORDER, Security("GOOG"), 25, FULFILLED),
            TradeOrder(TrackingId("t100"), BrokerageAccountId("344"), BUY_ORDER, Security("AMZN"), 15, OUTSTANDING),
            TradeOrder(TrackingId("t200"), BrokerageAccountId("344"), BUY_ORDER, Security("GOOG"), 25, OUTSTANDING),
            TradeOrder(TrackingId("t300"), BrokerageAccountId("123"), SELL_ORDER, Security("AMZN"), 75, OUTSTANDING)
        )

        val repository = fixture.createTradeOrderRepository()

        val tradeOrders = repository.outstandingForBrokerageAccountId(BrokerageAccountId("123"))

        assertEquals(
            listOf(
                TradeOrder(TrackingId("t123"), BrokerageAccountId("123"), BUY_ORDER, Security("AMZN"), 10, OUTSTANDING),
                TradeOrder(TrackingId("t300"), BrokerageAccountId("123"), SELL_ORDER, Security("AMZN"), 75, OUTSTANDING)
            ),
            tradeOrders
        )
    }
}

