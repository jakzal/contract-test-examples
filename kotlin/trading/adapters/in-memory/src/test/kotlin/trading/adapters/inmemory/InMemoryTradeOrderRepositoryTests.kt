package trading.adapters.inmemory

import trading.TradeOrder
import trading.TradeOrderRepositoryContract

class InMemoryTradeOrderRepositoryTests : TradeOrderRepositoryContract() {
    override fun tradeOrderRepositoryWith(tradeOrders: List<TradeOrder>) = InMemoryTradeOrderRepository(tradeOrders)
}