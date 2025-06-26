plugins {
    id("kotlin-common-conventions")
    id("java-test-fixtures")
}

dependencies {
    testFixturesImplementation(kotlin("test"))
    testFixturesImplementation(kotlin("test-junit5"))
}
