package trading.adapters.exposed

import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.addLogger
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import trading.*
import trading.TradeOrderStatus.FULFILLED
import trading.TradeOrderStatus.OUTSTANDING
import trading.TradeOrderType.BUY_ORDER
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Testcontainers(disabledWithoutDocker = true)
class ExposedTradeOrderRepositoryTests {

    companion object {
        @Container
        private val postgresql = PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"))
    }

    @BeforeTest
    fun createSchema(): Unit = transaction(postgresql.connection) {
        addLogger(StdOutSqlLogger)
        SchemaUtils.drop(TradeOrders)
        SchemaUtils.create(TradeOrders)
    }

    @TestFactory
    fun `TradeOrderRepository Contract`() = TradeOrderRepositoryContract().allTests(::tradeOrderRepositoryWith)

    private fun tradeOrderRepositoryWith(existingTradeOrders: List<TradeOrder>): ExposedTradeOrderRepository {
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
        return ExposedTradeOrderRepository(postgresql.connection)
    }
}

class TradeOrderRepositoryContract {

    fun `returns the TradeOrder if it exists for the given tracking ID`(tradeOrderRepositoryWith: (List<TradeOrder>) -> TradeOrderRepository) {
        val matching = tradeOrder(trackingId = TrackingId("t456"))

        val repository = tradeOrderRepositoryWith(
            listOf(
                tradeOrder(trackingId = trackingIdOtherThan("t456")),
                matching,
                tradeOrder(trackingId = trackingIdOtherThan("t456"))
            )
        )

        val found = repository.forTrackingId(TrackingId("t456"))

        assertEquals(matching, found)
    }

    fun `returns null if the TradeOrder is not found for the given tracking ID`(tradeOrderRepositoryWith: (List<TradeOrder>) -> TradeOrderRepository) {
        val repository = tradeOrderRepositoryWith(
            listOf(
                tradeOrder(trackingId = trackingIdOtherThan("t999")),
                tradeOrder(trackingId = trackingIdOtherThan("t999")),
                tradeOrder(trackingId = trackingIdOtherThan("t999"))
            )
        )

        val tradeOrder = repository.forTrackingId(TrackingId("t999"))

        assertNull(tradeOrder)
    }

    fun `returns an empty list if no TradeOrder was found for the given account ID`(tradeOrderRepositoryWith: (List<TradeOrder>) -> TradeOrderRepository) {
        val repository = tradeOrderRepositoryWith(
            listOf(
                tradeOrder(brokerageAccountId = brokerageAccountIdOtherThan("987"), status = FULFILLED),
                tradeOrder(brokerageAccountId = brokerageAccountIdOtherThan("987"), status = FULFILLED),
                tradeOrder(brokerageAccountId = brokerageAccountIdOtherThan("987"), status = FULFILLED)
            )
        )

        val tradeOrders = repository.outstandingForBrokerageAccountId(BrokerageAccountId("987"))

        assertEquals(emptyList(), tradeOrders)
    }

    fun `returns all outstanding TradeOrders for the given account ID`(tradeOrderRepositoryWith: (List<TradeOrder>) -> TradeOrderRepository) {
        val matchingBothAccountIdAndStatus =
            tradeOrder(brokerageAccountId = BrokerageAccountId("123"), status = OUTSTANDING)
        val anotherMatchingBothAccountIdAndStatus =
            tradeOrder(brokerageAccountId = BrokerageAccountId("123"), status = OUTSTANDING)
        val matchingAccountIdButFulfilled =
            tradeOrder(brokerageAccountId = BrokerageAccountId("123"), status = FULFILLED)
        val outstandingButAnyOtherAccountId =
            tradeOrder(brokerageAccountId = brokerageAccountIdOtherThan("123"), status = OUTSTANDING)
        val anotherMatchingAccountIdButFulfilled =
            tradeOrder(brokerageAccountId = BrokerageAccountId("123"), status = FULFILLED)

        val repository = tradeOrderRepositoryWith(
            listOf(
                matchingBothAccountIdAndStatus,
                matchingAccountIdButFulfilled,
                anotherMatchingAccountIdButFulfilled,
                outstandingButAnyOtherAccountId,
                anotherMatchingBothAccountIdAndStatus,
            )
        )

        val tradeOrders = repository.outstandingForBrokerageAccountId(BrokerageAccountId("123"))

        assertEquals(listOf(matchingBothAccountIdAndStatus, anotherMatchingBothAccountIdAndStatus), tradeOrders)
    }

    fun allTests(tradeOrderRepositoryWith: (List<TradeOrder>) -> TradeOrderRepository): List<DynamicTest> = listOf(
        this::`returns the TradeOrder if it exists for the given tracking ID`,
        this::`returns null if the TradeOrder is not found for the given tracking ID`,
        this::`returns an empty list if no TradeOrder was found for the given account ID`,
        this::`returns all outstanding TradeOrders for the given account ID`
    ).map { test ->
        dynamicTest(test.name) { test(tradeOrderRepositoryWith) }
    }
}

private val <SELF : PostgreSQLContainer<SELF>> PostgreSQLContainer<SELF>.connection
    get() = Database.connect(url = jdbcUrl, user = username, password = password)

private fun tradeOrder(
    trackingId: TrackingId? = null,
    brokerageAccountId: BrokerageAccountId? = null,
    type: TradeOrderType? = null,
    security: Security? = null,
    numberOfShares: Int? = null,
    status: TradeOrderStatus? = null,
) = TradeOrder(
    trackingId.orGenerate(),
    brokerageAccountId.orGenerate(),
    type.orRandom(),
    security.orRandom(),
    numberOfShares.orRandom(),
    status.orRandom()
)

private val lastTrackingId = AtomicInteger(1000)
private val lastBrokerageAccountId = AtomicInteger(2000)

private fun nextTrackingId() = TrackingId("t${lastTrackingId.incrementAndGet()}")
private fun nextBrokerageAccountId() = BrokerageAccountId("${lastBrokerageAccountId.incrementAndGet()}")

private fun trackingIdOtherThan(trackingId: String) = nextTrackingId().also { assert(it.value != trackingId) }
private fun brokerageAccountIdOtherThan(brokerageAccountId: String) =
    nextBrokerageAccountId().also { assert(it.value != brokerageAccountId) }

private fun TrackingId?.orGenerate() = this ?: nextTrackingId()

private fun BrokerageAccountId?.orGenerate() = this ?: nextBrokerageAccountId()

private fun TradeOrderStatus?.orRandom() = this ?: TradeOrderStatus.entries.toTypedArray().random()

private fun TradeOrderType?.orRandom() = this ?: BUY_ORDER

private fun Security?.orRandom() = this ?: Security(listOf("AMZN", "WCOM", "GOOG").random())

private fun Int?.orRandom() = this ?: (1..500).random()