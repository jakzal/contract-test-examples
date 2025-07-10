package trading

data class TradeOrder(
    val trackingId: TrackingId,
    val brokerageAccountId: BrokerageAccountId,
    val type: TradeOrderType,
    val security: Security,
    val numberOfShares: Int,
    val status: TradeOrderStatus
)

@JvmInline
value class TrackingId(val value: String)

@JvmInline
value class BrokerageAccountId(val value: String)

@JvmInline
value class Security(val value: String)

enum class TradeOrderType { BUY_ORDER, SELL_ORDER }

enum class TradeOrderStatus { OUTSTANDING, FULFILLED }
