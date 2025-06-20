package ru.practicum.exception;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
public class ColonPathVariableResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PathVariable.class) &&
                parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        PathVariable ann = parameter.getParameterAnnotation(PathVariable.class);
        if (ann == null) return null;

        String name = !ann.name().isEmpty() ? ann.name() : parameter.getParameterName();
        String rawValue = webRequest.getParameter(name);
        if (rawValue == null) {
            rawValue = getFromUriTemplateVars(webRequest, name);
        }

        if (rawValue != null && rawValue.startsWith(":")) {
            rawValue = rawValue.substring(1);
        }

        return Long.parseLong(rawValue);
    }

    private String getFromUriTemplateVars(NativeWebRequest request, String name) {
        Map<String, String> uriVars = (Map<String, String>)
                request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        return uriVars != null ? uriVars.get(name) : null;
    }
}
