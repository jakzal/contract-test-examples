rootProject.name = "contract-test-examples"

include("trading:domain", "trading:adapters:exposed", "trading:adapters:in-memory")
include("trading-composition:domain", "trading-composition:adapters:exposed", "trading-composition:adapters:in-memory")