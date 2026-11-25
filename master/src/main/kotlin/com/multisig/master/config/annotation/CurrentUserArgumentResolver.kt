package com.multisig.master.config.annotation


import com.multisig.master.config.annotation.CurrentUser
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.server.ResponseStatusException

@Component
class CurrentUserArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java) &&
                parameter.parameterType == Long::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Long {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)

        val userId = request?.getAttribute("CurrentUserId") as? String
            ?: request?.getHeader("X-User-Id")

        if (userId.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User context not found")
        }
        
        return userId.toLong()
    }
}