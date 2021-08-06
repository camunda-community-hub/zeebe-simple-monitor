package io.zeebe.monitor.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WhitelabelProperties {

	private final String basePath;
	private final String logoPath;
	private final String customCssPath;
	private final String customJsPath;
	private final String customTitle;

	public WhitelabelProperties(@Value("${server.servlet.context-path}") final String basePath,
	                            @Value("${white-label.logo.path}") final String logoPath,
	                            @Value("${white-label.custom.title}") final String customTitle,
	                            @Value("${white-label.custom.css.path}") final String customCssPath,
	                            @Value("${white-label.custom.js.path}") final String customJsPath){
		this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
		this.logoPath = logoPath;
		this.customTitle = customTitle;
		this.customCssPath = customCssPath;
		this.customJsPath = customJsPath;
	}

	public String getBasePath() {
		return basePath;
	}

	public String getLogoPath() {
		return logoPath;
	}

	public String getCustomCssPath() {
		return customCssPath;
	}

	public String getCustomJsPath() {
		return customJsPath;
	}

	public String getCustomTitle() {
		return customTitle;
	}
}
