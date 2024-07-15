rootProject.name = "waypoints"

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version ("0.8.0") }

include(
    ":waypoints",
    ":waypoints-api",
    ":waypoints-api:example",
    ":api-base",
    ":api-sqlite",
    ":api-tests",
    ":signgui",
    ":utils",
    ":pointers",
)
