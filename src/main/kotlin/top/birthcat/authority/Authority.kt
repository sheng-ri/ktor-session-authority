package top.birthcat.authority

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.pipeline.*

object AuthorityHook : Hook<suspend (ApplicationCall) -> Unit> {
    private val AuthorityPhase: PipelinePhase = PipelinePhase("AuthorityPhase")

    override fun install(
        pipeline: ApplicationCallPipeline,
        handler: suspend (ApplicationCall) -> Unit
    ) {
        pipeline.insertPhaseAfter(ApplicationCallPipeline.Plugins, AuthorityPhase)
        pipeline.intercept(AuthorityPhase) { handler(call) }
    }
}

data class AuthorityConfig(var authorizes: List<String> = listOf()) {
    lateinit var sessionName: String
}

val AuthorityInterceptors: RouteScopedPlugin<AuthorityConfig> = createRouteScopedPlugin(
    "AuthorityInterceptors",
    ::AuthorityConfig
) {
    on(AuthorityHook) { call ->
        val session = call.sessions.get(pluginConfig.sessionName)
        if (session == null) {
            call.respondNullable<Any?>(HttpStatusCode.Unauthorized,null)
            return@on
        }
        if (session is PrincipalAuthority) {
            // TODO: this add unauthorized/Forbidden handler
            if (session.authorizes().any { pluginConfig.authorizes.contains(it) }) {
                return@on
            } else {
                call.respondNullable<Any?>(HttpStatusCode.Forbidden,null)
            }
        } else {
            error("We except session is extends PrincipalAuthority")
        }

    }
}

inline fun Route.authority(session: String,authorizes: List<String>, build: Route.() -> Unit): Route {
    val selector = createChild(AuthorityRoute())
    selector.install(AuthorityInterceptors) {
        this.authorizes = authorizes
        this.sessionName = session
    }
    selector.build()
    return selector
}

class AuthorityRoute: RouteSelector() {

    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Transparent
    }

    override fun toString(): String = "(authority)"
}

interface PrincipalAuthority {
    fun authorizes(): List<String>
}