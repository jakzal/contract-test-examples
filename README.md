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
