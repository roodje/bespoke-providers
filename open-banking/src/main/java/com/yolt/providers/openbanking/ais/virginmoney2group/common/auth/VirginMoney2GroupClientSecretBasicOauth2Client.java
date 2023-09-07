package com.yolt.providers.openbanking.ais.virginmoney2group.common.auth;

import com.yolt.providers.openbanking.ais.common.HttpUtils;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.producer.VirginMoney2GroupTokenBodyProducer;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.properties.VirginMoney2GroupProperties;
import org.springframework.util.MultiValueMap;

public class VirginMoney2GroupClientSecretBasicOauth2Client extends BasicOauthClient<MultiValueMap<String, String>> {

    public VirginMoney2GroupClientSecretBasicOauth2Client(VirginMoney2GroupTokenBodyProducer tokenBodyProducer, VirginMoney2GroupProperties properties, boolean isInPisFlow) {
        super(properties.getOAuthTokenUrl(),
                authenticationMeans -> HttpUtils.basicCredentials(authenticationMeans.getClientId(), authenticationMeans.getClientSecret()),
                tokenBodyProducer,
                isInPisFlow);
    }
}