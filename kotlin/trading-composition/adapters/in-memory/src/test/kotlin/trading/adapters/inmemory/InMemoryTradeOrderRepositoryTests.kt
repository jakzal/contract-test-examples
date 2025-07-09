package trading.adapters.inmemory

import org.junit.jupiter.api.TestFactory
import trading.TradeOrder
import trading.TradeOrderRepositoryContract

class InMemoryTradeOrderRepositoryTests {
    @TestFactory
    fun `TradeOrderRepository Contract`() = TradeOrderRepositoryContract().allTests { tradeOrders: List<TradeOrder> ->
        InMemoryTradeOrderRepository(tradeOrders)
    }
}