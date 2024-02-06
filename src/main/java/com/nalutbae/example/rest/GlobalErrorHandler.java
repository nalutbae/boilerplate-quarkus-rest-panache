package com.nalutbae.example.rest;

import com.nalutbae.example.domain.CustomRuntimeException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class GlobalErrorHandler {
	@ServerExceptionMapper(CustomRuntimeException.class)
	public Response handleCustomRuntimeException(CustomRuntimeException cre) {
		return Response.serverError()
			.header("X-CUSTOM-ERROR", "500")
			.entity(new CustomError(500, cre.getMessage()))
			.build();
	}
}
