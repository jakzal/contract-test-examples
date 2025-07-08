plugins {
    id("kotlin-common-conventions")
}

dependencies {
    implementation(project(":trading:domain"))
    implementation(libs.bundles.exposed)
    implementation(libs.postgresql)
    testImplementation(libs.bundles.testcontainers)
}