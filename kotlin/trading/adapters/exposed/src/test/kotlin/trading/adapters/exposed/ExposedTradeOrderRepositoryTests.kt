package trading.adapters.exposed

import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.addLogger
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import trading.BrokerageAccountId
import trading.Security
import trading.TrackingId
import trading.TradeOrder
import trading.TradeOrderStatus.FULFILLED
import trading.TradeOrderStatus.OUTSTANDING
import trading.TradeOrderType.BUY_ORDER
import trading.TradeOrderType.SELL_ORDER
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Testcontainers
class ExposedTradeOrderRepositoryTests {

    companion object {
        @Container
        private val postgresql = PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"))
    }

    @BeforeEach
    fun createSchema(): Unit = transaction(postgresql.connection) {
        addLogger(StdOutSqlLogger)
        SchemaUtils.drop(TradeOrders)
        SchemaUtils.create(TradeOrders)
    }

    @Test
    fun `returns the TradeOrder if it exists for the given tracking ID`() {
        val existingTradeOrders = listOf(
            TradeOrder(TrackingId("t123"), BrokerageAccountId("123"), BUY_ORDER, Security("AMZN"), 10, OUTSTANDING),
            TradeOrder(TrackingId("t456"), BrokerageAccountId("123"), SELL_ORDER, Security("WCOM"), 50, FULFILLED),
            TradeOrder(TrackingId("t789"), BrokerageAccountId("123"), SELL_ORDER, Security("GOOG"), 25, FULFILLED)
        )
        transaction(postgresql.connection) {
            addLogger(StdOutSqlLogger)
            TradeOrders.batchInsert(existingTradeOrders) {
                this[TradeOrders.trackingId] = it.trackingId.value
                this[TradeOrders.brokerageAccountId] = it.brokerageAccountId.value
                this[TradeOrders.type] = it.type
                this[TradeOrders.security] = it.security.value
                this[TradeOrders.numberOfShares] = it.numberOfShares
                this[TradeOrders.status] = it.status
            }
        }

        val repository = ExposedTradeOrderRepository(postgresql.connection)

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
        val existingTradeOrders = listOf(
            TradeOrder(TrackingId("t123"), BrokerageAccountId("123"), BUY_ORDER, Security("AMZN"), 10, OUTSTANDING),
            TradeOrder(TrackingId("t456"), BrokerageAccountId("123"), SELL_ORDER, Security("WCOM"), 50, FULFILLED),
            TradeOrder(TrackingId("t789"), BrokerageAccountId("123"), SELL_ORDER, Security("GOOG"), 25, FULFILLED)
        )
        transaction(postgresql.connection) {
            addLogger(StdOutSqlLogger)
            TradeOrders.batchInsert(existingTradeOrders) {
                this[TradeOrders.trackingId] = it.trackingId.value
                this[TradeOrders.brokerageAccountId] = it.brokerageAccountId.value
                this[TradeOrders.type] = it.type
                this[TradeOrders.security] = it.security.value
                this[TradeOrders.numberOfShares] = it.numberOfShares
                this[TradeOrders.status] = it.status
            }
        }

        val repository = ExposedTradeOrderRepository(postgresql.connection)

        val tradeOrder = repository.forTrackingId(TrackingId("t999"))

        assertNull(tradeOrder)
    }

    @Test
    fun `returns an empty list if no TradeOrder was found for the given account ID`() {
        val existingTradeOrders = listOf(
            TradeOrder(TrackingId("t123"), BrokerageAccountId("123"), BUY_ORDER, Security("AMZN"), 10, OUTSTANDING),
            TradeOrder(TrackingId("t456"), BrokerageAccountId("123"), SELL_ORDER, Security("WCOM"), 50, FULFILLED),
            TradeOrder(TrackingId("t789"), BrokerageAccountId("123"), SELL_ORDER, Security("GOOG"), 25, FULFILLED)
        )
        transaction(postgresql.connection) {
            addLogger(StdOutSqlLogger)
            TradeOrders.batchInsert(existingTradeOrders) {
                this[TradeOrders.trackingId] = it.trackingId.value
                this[TradeOrders.brokerageAccountId] = it.brokerageAccountId.value
                this[TradeOrders.type] = it.type
                this[TradeOrders.security] = it.security.value
                this[TradeOrders.numberOfShares] = it.numberOfShares
                this[TradeOrders.status] = it.status
            }
        }

        val repository = ExposedTradeOrderRepository(postgresql.connection)

        val tradeOrders = repository.outstandingForBrokerageAccountId(BrokerageAccountId("987"))

        assertEquals(tradeOrders, emptyList())
    }

    @Test
    fun `returns all outstanding TradeOrders for the given account ID`() {
        val existingTradeOrders = listOf(
            TradeOrder(TrackingId("t123"), BrokerageAccountId("123"), BUY_ORDER, Security("AMZN"), 10, OUTSTANDING),
            TradeOrder(TrackingId("t456"), BrokerageAccountId("123"), SELL_ORDER, Security("WCOM"), 50, FULFILLED),
            TradeOrder(TrackingId("t789"), BrokerageAccountId("123"), SELL_ORDER, Security("GOOG"), 25, FULFILLED),
            TradeOrder(TrackingId("t100"), BrokerageAccountId("344"), BUY_ORDER, Security("AMZN"), 15, OUTSTANDING),
            TradeOrder(TrackingId("t200"), BrokerageAccountId("344"), BUY_ORDER, Security("GOOG"), 25, OUTSTANDING),
            TradeOrder(TrackingId("t300"), BrokerageAccountId("123"), SELL_ORDER, Security("AMZN"), 75, OUTSTANDING),
        )
        transaction(postgresql.connection) {
            addLogger(StdOutSqlLogger)
            TradeOrders.batchInsert(existingTradeOrders) {
                this[TradeOrders.trackingId] = it.trackingId.value
                this[TradeOrders.brokerageAccountId] = it.brokerageAccountId.value
                this[TradeOrders.type] = it.type
                this[TradeOrders.security] = it.security.value
                this[TradeOrders.numberOfShares] = it.numberOfShares
                this[TradeOrders.status] = it.status
            }
        }

        val repository = ExposedTradeOrderRepository(postgresql.connection)

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

private val <SELF : PostgreSQLContainer<SELF>> PostgreSQLContainer<SELF>.connection
    get() = Database.connect(url = jdbcUrl, user = username, password = password)
