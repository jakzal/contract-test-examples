# Contract Test Examples

This repository contains examples of [Contract Tests](https://zalas.pl/collaboration-and-contract-tests/)
in various programming languages.

## Examples

### Trading

This example is loosely based on the `TradeOrderRepository` found in the "Domain-Driven Design: Tackling Complexity in the Heart of Software"
book by Eric Evans.

Implement the `TradeOrderRepository` with a database storage. The repository should have two methods:

* `forTrackingId(String): TradeOrder` - takes the tracking ID and returns a TradeOrder.
  ![forTrackingId](docs/images/trading/trade-order-repository-1.png)

* `outstandingForBrokerageAccountId(String): List<TradeOrder>` - takes the brokerage account ID and returns a list of TradeOrders
  ![outstandingForBrokerageAccountId](docs/images/trading/trade-order-repository-2.png)

The expected behaviour of `TradeOrderRepository` methods:
* `forTrackingId`
  * returns the TradeOrder if it exists for the given tracking ID
  * returns `null` if the TradeOrder is not found for the given tracking ID (alternatively, expect an exception)
* `outstandingForBrokerageAccountId`
  * returns all outstanding TradeOrders for the given account ID
  * returns an empty list if no TradeOrder was found for the given account ID

<!-- diagrams: https://excalidraw.com/#json=BO8BXplvlraagR_1XPHqN,kpyw34vOoCd64QOgC71_sQ -->

#### Solutions

- [Kotlin](https://github.com/jakzal/contract-test-examples/tree/main/kotlin/trading) | Steps: [Domain Model](https://github.com/jakzal/contract-test-examples/commit/a280090fc8e08c46450be5197f73f5841516880b) > [Integration microtest](https://github.com/jakzal/contract-test-examples/commit/4697ac952b3b679beacf215bf2ed4a45f5baeeb3) > [Contract placeholder](https://github.com/jakzal/contract-test-examples/commit/d4354cd93cb7f07ef4adf06aa5d1bf3056d74a8a) > [Extract creational method](https://github.com/jakzal/contract-test-examples/commit/f9f7092f39b41f787d9ca2a2f2e26a926d7b55f5) > [Extract fixture setup](https://github.com/jakzal/contract-test-examples/commit/7f7fc92cccc79b1fef11cd55692632a5c1aac7b6) > [Pull tests up to the contract test case](https://github.com/jakzal/contract-test-examples/commit/6fa77f300369aa850a35b98550fa9b2bd6e3d1a1) > [Second adapter](https://github.com/jakzal/contract-test-examples/commit/5358e696f7a84803be0633161cc47e4d0f9a6062)
