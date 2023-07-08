package com.studysquad.global.util;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.studysquad.global.error.exception.InvalidLoginUserException;
import com.studysquad.global.security.Login;
import com.studysquad.user.domain.Role;
import com.studysquad.user.dto.LoginUser;

public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
		boolean hasLoginUserType = LoginUser.class.isAssignableFrom(parameter.getParameterType());

		return hasLoginAnnotation && hasLoginUserType;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			throw new InvalidLoginUserException();
		}

		return LoginUser.builder()
			.email(authentication.getName())
			.role(Role.USER)
			.build();
	}
}
