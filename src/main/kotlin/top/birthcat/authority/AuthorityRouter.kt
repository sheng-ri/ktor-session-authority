package top.birthcat.authority

import io.ktor.server.application.*
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

class AuthorityRouterConfig {
    lateinit var authorizes: List<String>
    lateinit var sessionName: String
}

val AuthorityInterceptors: RouteScopedPlugin<AuthorityRouterConfig> = createRouteScopedPlugin(
    "AuthorityInterceptors",
    ::AuthorityRouterConfig
) {
    val authConfig = application.plugin(Authority).config

    on(AuthorityHook) { call ->
        val session = call.sessions.get(pluginConfig.sessionName)
        if (session == null) {
            authConfig.noSessionHandler.invoke(call)
            return@on
        }
        if (session is PrincipalAuthority) {
            if (session.authorizes().any { pluginConfig.authorizes.contains(it) }) {
                return@on
            } else {
                authConfig.noSessionHandler.invoke(call)
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