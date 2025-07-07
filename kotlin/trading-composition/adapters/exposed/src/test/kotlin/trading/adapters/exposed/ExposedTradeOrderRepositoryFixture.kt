package trading.adapters.exposed

import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.addLogger
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import trading.TradeOrder
import trading.TradeOrderRepository

class ExposedTradeOrderRepositoryFixture : TradeOrderRepositoryFixture {
    private val postgresql = PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine")).apply {
        start()
        createSchema(this.connection)
    }

    fun createSchema(connection: Database): Unit = transaction(connection) {
        addLogger(StdOutSqlLogger)
        SchemaUtils.drop(TradeOrders)
        SchemaUtils.create(TradeOrders)
    }

    override fun createTradeOrderRepository(): TradeOrderRepository = ExposedTradeOrderRepository(
        postgresql.connection
    )

    override fun givenExistingTradeOrders(tradeOrder: TradeOrder, vararg tradeOrders: TradeOrder): Unit =
        transaction(postgresql.connection) {
            addLogger(StdOutSqlLogger)
            TradeOrders.batchInsert(listOf(tradeOrder) + tradeOrders.toList()) {
                this[TradeOrders.trackingId] = it.trackingId.value
                this[TradeOrders.brokerageAccountId] = it.brokerageAccountId.value
                this[TradeOrders.type] = it.type
                this[TradeOrders.security] = it.security.value
                this[TradeOrders.numberOfShares] = it.numberOfShares
                this[TradeOrders.status] = it.status
            }
        }

    override fun beforeTest() = transaction(postgresql.connection) {
        SchemaUtils.drop(TradeOrders)
        SchemaUtils.create(TradeOrders)
    }
}

private val <SELF : PostgreSQLContainer<SELF>> PostgreSQLContainer<SELF>.connection
    get() = Database.Companion.connect(url = jdbcUrl, user = username, password = password)