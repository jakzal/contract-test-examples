plugins {
    id("kotlin-common-conventions")
}

dependencies {
    implementation(project(":trading-composition:domain"))
    testImplementation(testFixtures(project(":trading-composition:domain")))
}