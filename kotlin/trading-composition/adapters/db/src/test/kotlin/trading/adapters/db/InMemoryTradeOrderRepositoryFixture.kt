package trading.adapters.db

import trading.TradeOrder
import trading.TradeOrderRepository

class InMemoryTradeOrderRepositoryFixture : TradeOrderRepositoryFixture {
    private val existingTradeOrders = mutableListOf<TradeOrder>()

    override fun createTradeOrderRepository(): TradeOrderRepository =
        InMemoryTradeOrderRepository(existingTradeOrders)

    override fun givenExistingTradeOrders(tradeOrder: TradeOrder, vararg tradeOrders: TradeOrder) {
        existingTradeOrders.addAll(listOf(tradeOrder) + listOf(*tradeOrders))
    }

    override fun beforeTest() {}
}
