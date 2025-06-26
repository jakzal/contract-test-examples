package trading.adapters.inmemory

import trading.TradeOrder
import trading.TradeOrderRepositoryContract

class InMemoryTradeOrderRepositoryTests : TradeOrderRepositoryContract() {
    companion object {
        private val existingTradeOrders = mutableListOf<TradeOrder>()
    }

    override fun createTradeOrderRepository() = InMemoryTradeOrderRepository(existingTradeOrders)

    override fun givenExistingTradeOrders(tradeOrder: TradeOrder, vararg tradeOrders: TradeOrder) {
        existingTradeOrders.addAll(listOf(tradeOrder) + listOf(*tradeOrders))
    }
}