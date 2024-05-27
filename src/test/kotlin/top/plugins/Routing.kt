package birthcat.top.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import top.birthcat.authority.PrincipalAuthority
import top.birthcat.authority.authority

data class MySession(val authority: List<String> = listOf(), var count:Int = 0): PrincipalAuthority {
    override fun authorizes(): List<String> {
        return authority
    }
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.sessions.set(MySession(listOf("user")))
            call.respondText("Hello World!")
        }
        authority("session", listOf("user")) {
            get("/user")  {
                call.respondText("user OK")
            }
        }
        authority("session", listOf("admin")) {
            get("/admin")  {
                call.respondText("Admin OK")
            }
        }
    }
}
