package io.zeebe.monitor.rest;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.Map;

@Component
public class WhitelabelPropertiesMapper {

  public static final String CUSTOM_TITLE = "custom-title";
  public static final String CONTEXT_PATH = "context-path";
  public static final String LOGO_PATH = "logo-path";
  public static final String CUSTOM_CSS_PATH = "custom-css-path";
  public static final String CUSTOM_JS_PATH = "custom-js-path";

  public void addPropertiesToModel(Model model, WhitelabelProperties whitelabelProperties) {
    model.addAttribute(CUSTOM_TITLE, whitelabelProperties.getCustomTitle());
    model.addAttribute(CONTEXT_PATH, whitelabelProperties.getBasePath());
    model.addAttribute(LOGO_PATH, whitelabelProperties.getLogoPath());
    model.addAttribute(CUSTOM_CSS_PATH, whitelabelProperties.getCustomCssPath());
    model.addAttribute(CUSTOM_JS_PATH, whitelabelProperties.getCustomJsPath());
  }

  public void addPropertiesToModel(Map<String, Object> model, WhitelabelProperties whitelabelProperties) {
    model.put(CUSTOM_TITLE, whitelabelProperties.getCustomTitle());
    model.put(CONTEXT_PATH, whitelabelProperties.getBasePath());
    model.put(LOGO_PATH, whitelabelProperties.getLogoPath());
    model.put(CUSTOM_CSS_PATH, whitelabelProperties.getCustomCssPath());
    model.put(CUSTOM_JS_PATH, whitelabelProperties.getCustomJsPath());
  }

}
