rootProject.name = "chess"
include("chess-core")
include("chess-server")
include("chess-server:src:test:kotlin")
findProject(":chess-server:src:test:kotlin")?.name = "kotlin"
