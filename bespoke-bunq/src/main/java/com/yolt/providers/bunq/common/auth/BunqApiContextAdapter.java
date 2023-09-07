package com.yolt.providers.bunq.common.auth;

import com.bunq.sdk.security.SecurityUtils;
import com.google.gson.*;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;
import java.security.KeyPair;

@NoArgsConstructor
public class BunqApiContextAdapter implements JsonSerializer<BunqApiContext>, JsonDeserializer<BunqApiContext> {

    @Override
    public BunqApiContext deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String userId = jsonObject.get("bunq_user_id").getAsString();
        String installationToken = jsonObject.get("server_installation_token").getAsString();
        String oAuthApiToken = jsonObject.get("oauth_api_token").getAsString();
        String sessionToken = jsonObject.get("session_token").getAsString();
        Long sessionExpiryTime = jsonObject.get("session_expiry_time").getAsLong();
        KeyPair keyPairClient = SecurityUtils.createKeyPairFromFormattedStrings(jsonObject.get("public_key_client").getAsString(), jsonObject.get("private_key_client").getAsString());
        return new BunqApiContext(userId, installationToken, keyPairClient, oAuthApiToken, sessionToken, sessionExpiryTime);
    }

    @Override
    public JsonElement serialize(final BunqApiContext apiContext, final Type type, final JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("bunq_user_id", apiContext.getBunqUserId());
        jsonObject.addProperty("oauth_api_token", apiContext.getOauthToken());
        jsonObject.addProperty("server_installation_token", apiContext.getServerToken());
        jsonObject.addProperty("session_token", apiContext.getSessionToken());
        jsonObject.addProperty("session_expiry_time", apiContext.getExpiryTimeInSeconds());
        KeyPair keyPairClient = apiContext.getKeyPair();
        String publicKeyClientString = SecurityUtils.getPublicKeyFormattedString(keyPairClient);
        jsonObject.addProperty("public_key_client", publicKeyClientString);
        String privateKeyClientString = SecurityUtils.getPrivateKeyFormattedString(keyPairClient);
        jsonObject.addProperty("private_key_client", privateKeyClientString);
        return jsonObject;
    }
}
