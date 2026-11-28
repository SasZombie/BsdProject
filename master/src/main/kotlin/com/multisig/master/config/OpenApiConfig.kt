package com.multisig.master.config

import com.multisig.master.config.annotation.CurrentUser
import com.multisig.master.dto.AuthUser
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    init {
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(AuthUser::class.java)
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUser::class.java)
    }

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "BearerAuth"

        return OpenAPI()
            .info(
                Info()
                    .title("Multi-Sig Master API")
                    .version("1.0.0")
                    .description("REST API documentation for requests, approvals, and authentication")
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("opaque")
                    )
            )
    }
}