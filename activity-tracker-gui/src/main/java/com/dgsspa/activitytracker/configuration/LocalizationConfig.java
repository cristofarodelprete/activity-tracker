package com.dgsspa.activitytracker.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class LocalizationConfig {
	
	@Bean
    public ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("localization");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }
}
