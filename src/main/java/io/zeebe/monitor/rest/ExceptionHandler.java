package io.zeebe.monitor.rest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

	private final WhitelabelProperties whitelabelProperties;

	public ExceptionHandler(WhitelabelProperties whitelabelProperties) {
		this.whitelabelProperties = whitelabelProperties;
	}

	@org.springframework.web.bind.annotation.ExceptionHandler(RuntimeException.class)
	public String handleRuntimeException(RuntimeException exc, final Model model) {
		LOG.error(exc.getMessage(), exc);

		model.addAttribute("error", exc.getClass().getSimpleName());
		model.addAttribute("message", exc.getMessage());
		model.addAttribute("trace", ExceptionUtils.getStackTrace(exc));

		model.addAttribute("custom-title", whitelabelProperties.getCustomTitle());
		model.addAttribute("context-path", whitelabelProperties.getBasePath());
		model.addAttribute("logo-path", whitelabelProperties.getLogoPath());
		model.addAttribute("custom-css-path", whitelabelProperties.getCustomCssPath());
		model.addAttribute("custom-js-path", whitelabelProperties.getCustomCssPath());

		return "error";
	}

}
