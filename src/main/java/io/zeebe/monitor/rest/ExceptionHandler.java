package io.zeebe.monitor.rest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

	private final String basePath;
	private final String logoPath;
	private final String customCssPath;
	private final String customJsPath;
	private final String customTitle;


	public ExceptionHandler(@Value("${server.servlet.context-path}") final String basePath,
	                        @Value("${white-label.logo.path}") final String logoPath,
	                        @Value("${white-label.custom.title}") final String customTitle,
	                        @Value("${white-label.custom.css.path}") final String customCssPath,
	                        @Value("${white-label.custom.js.path}") final String customJsPath) {
		this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
		this.logoPath = logoPath;
		this.customTitle = customTitle;
		this.customCssPath = customCssPath;
		this.customJsPath = customJsPath;

	}

	@org.springframework.web.bind.annotation.ExceptionHandler(RuntimeException.class)
	public String handleRuntimeException(RuntimeException exc, final Model model) {
		LOG.error(exc.getMessage(), exc);

		model.addAttribute("status", exc.getClass().getSimpleName());
		model.addAttribute("error", exc.getMessage());
		model.addAttribute("message", exc.getMessage());
		model.addAttribute("trace", ExceptionUtils.getStackTrace(exc));

		model.addAttribute("custom-title", customTitle);
		model.addAttribute("context-path", basePath);
		model.addAttribute("logo-path", logoPath);
		model.addAttribute("custom-css-path", customCssPath);
		model.addAttribute("custom-js-path", customJsPath);

		return "error";
	}

}
