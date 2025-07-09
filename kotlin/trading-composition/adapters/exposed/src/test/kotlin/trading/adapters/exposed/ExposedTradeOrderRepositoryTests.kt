package trading.adapters.exposed

import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.addLogger
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.TestFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import trading.TradeOrder
import trading.TradeOrderRepositoryContract
import kotlin.test.BeforeTest

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
        persistTradeOrders(existingTradeOrders)
        return ExposedTradeOrderRepository(postgresql.connection)
    }

    private fun persistTradeOrders(existingTradeOrders: List<TradeOrder>) = transaction(postgresql.connection) {
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
}

private val <SELF : PostgreSQLContainer<SELF>> PostgreSQLContainer<SELF>.connection
    get() = Database.connect(url = jdbcUrl, user = username, password = password)
