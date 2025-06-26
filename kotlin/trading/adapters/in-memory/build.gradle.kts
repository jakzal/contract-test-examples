plugins {
    id("kotlin-common-conventions")
}

dependencies {
    implementation(project(":trading:domain"))
    testImplementation(testFixtures(project(":trading:domain")))
}