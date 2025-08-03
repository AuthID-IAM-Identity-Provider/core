package io.authid.core.shared.components.i18n.configurations;
import io.authid.core.shared.components.i18n.sources.JsonMessageSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /**
     * Bean untuk membaca file ValidationMessages.properties
     */
    @Bean
    public MessageSource propertiesMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n_validation/ValidationMessages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * Bean utama yang akan di-inject. Menggabungkan JSON dan Properties.
     */
    @Bean
    @Primary
    public MessageSource jsonMessageSource() {
        JsonMessageSource jsonSource = new JsonMessageSource();

        // ✨ Set bean properties sebagai parent/fallback
        jsonSource.setParentMessageSource(propertiesMessageSource());

        return jsonSource;
    }


    @Bean
    @Qualifier("supportedLocales")
    public List<Locale> supportedLocales() {
        return List.of(
                new Locale("en"),
                new Locale("id"),
                new Locale("fr"),
                new Locale("ar"),
                new Locale("de"),
                new Locale("es"),
                new Locale("it"),
                new Locale("ja"),
                new Locale("ko"),
                new Locale("pt"),
                new Locale("ru"),
                new Locale("tr"),
                new Locale("zh"),
                new Locale("hi")
        );
    }

    @Bean
    public LocaleResolver localeResolver(@Qualifier("supportedLocales") List<Locale> supportedLocales) {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        localeResolver.setSupportedLocales(supportedLocales);
        return localeResolver;
    }
}
