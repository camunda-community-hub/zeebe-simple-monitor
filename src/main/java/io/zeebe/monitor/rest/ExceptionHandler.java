package io.zeebe.monitor.rest;

import io.camunda.zeebe.client.api.command.ClientException;
import io.zeebe.monitor.rest.ui.ErrorMessage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

  private final WhitelabelProperties whitelabelProperties;

  public ExceptionHandler(WhitelabelProperties whitelabelProperties) {
    this.whitelabelProperties = whitelabelProperties;
  }

  @org.springframework.web.bind.annotation.ExceptionHandler(value = {ClientException.class})
  protected ResponseEntity<Object> handleZeebeClientException(final RuntimeException ex, final WebRequest request) {
    LOG.debug("Zeebe Client Exception caught and forwarding to UI.", ex);
    return ResponseEntity
        .status(HttpStatus.FAILED_DEPENDENCY)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(ex.getMessage()));
  }

  @org.springframework.web.bind.annotation.ExceptionHandler(RuntimeException.class)
  public String handleRuntimeException(final RuntimeException exc, final Model model) {
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
