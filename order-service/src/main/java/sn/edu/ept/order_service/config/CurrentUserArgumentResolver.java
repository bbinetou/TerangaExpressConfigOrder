package sn.edu.ept.order_service.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@Component
public class CurrentUserArgumentResolver
        implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
            && parameter.getParameterType().equals(ConnectedUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        HttpServletRequest request =
            webRequest.getNativeRequest(HttpServletRequest.class);

        if (request == null) {
            throw new IllegalStateException("HttpServletRequest introuvable");
        }

        // Lire les headers injectés par le Gateway
        String userId = request.getHeader("X-Auth-Id");
        String email  = request.getHeader("X-User-Email");
        String role   = request.getHeader("X-User-Role");

        log.debug("[CurrentUser] X-Auth-Id={} X-User-Role={} X-User-Email={}",
                userId, role, email);

        if (userId == null) {
            // Development mode: provide default user when gateway headers are missing
            log.warn("[CurrentUser] X-Auth-Id absent - utilisation de l'utilisateur par défaut pour le développement");
            return ConnectedUser.builder()
                    .id(1L)
                    .email("dev@test.com")
                    .role("ADMIN")
                    .build();
        }

        return ConnectedUser.builder()
                .id(Long.parseLong(userId))
                .email(email)
                .role(role)
                .build();
    }
}
