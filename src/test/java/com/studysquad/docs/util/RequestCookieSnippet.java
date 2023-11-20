package com.studysquad.docs.util;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.restdocs.headers.AbstractHeadersSnippet;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.operation.RequestCookie;

public class RequestCookieSnippet extends AbstractHeadersSnippet {

	protected RequestCookieSnippet(List<HeaderDescriptor> descriptors, Map<String, Object> attributes) {
		super("cookie-request", descriptors, attributes);
	}

	@Override
	protected Set<String> extractActualHeaders(Operation operation) {
		return operation.getRequest().getCookies().stream()
			.map(RequestCookie::getName)
			.collect(Collectors.toSet());
	}

	public static RequestCookieSnippet requestHeaderCookies(HeaderDescriptor... descriptors) {
		return new RequestCookieSnippet(Arrays.asList(descriptors), null);
	}

	public static HeaderDescriptor cookieWithName(String cookieName) {
		return headerWithName(cookieName);
	}
}
