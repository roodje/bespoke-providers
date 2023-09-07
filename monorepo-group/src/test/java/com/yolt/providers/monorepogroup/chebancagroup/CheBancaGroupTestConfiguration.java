package com.yolt.providers.monorepogroup.chebancagroup;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.monorepogroup.RestTemplateManagerMock;
import com.yolt.providers.monorepogroup.TestConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class CheBancaGroupTestConfiguration extends TestConfiguration {

    @Bean
    @Qualifier("CheBancaRestTemplateManager")
    RestTemplateManager getRestTemplateManagerForCheBanca() {
        ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory = new ExternalRestTemplateBuilderFactory();
        externalRestTemplateBuilderFactory.requestFactory(SimpleClientHttpRequestFactory::new);
        return new RestTemplateManagerMock(externalRestTemplateBuilderFactory);
    }
}
