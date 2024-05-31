package top.birthcat.authority

import io.ktor.server.application.*
import io.ktor.util.*

@KtorDsl
data class AuthorityConfig(
    var noSessionHandler:suspend (ApplicationCall) -> Unit = {
        error("AuthorityConfig must set noSessionHandler.")
    },
    var noAuthorityHandler:suspend (ApplicationCall) -> Unit = {
        error("AuthorityConfig must set noAuthorityHandler.")
    },
)


class Authority(internal var config: AuthorityConfig) {

    companion object : BaseApplicationPlugin<Application, AuthorityConfig, Authority> {
        override val key: AttributeKey<Authority> = AttributeKey("Authority")

        override fun install(pipeline: Application, configure: AuthorityConfig.() -> Unit): Authority {
            val config = AuthorityConfig().apply(configure)
            return Authority(config)
        }
    }
}