package trading.adapters.inmemory

import trading.TradeOrder
import trading.TradeOrderRepositoryContract

class InMemoryTradeOrderRepositoryTests : TradeOrderRepositoryContract() {
    override fun tradeOrderRepositoryWith(tradeOrder: TradeOrder, vararg tradeOrders: TradeOrder) =
        InMemoryTradeOrderRepository(listOf(tradeOrder) + listOf(*tradeOrders))
}