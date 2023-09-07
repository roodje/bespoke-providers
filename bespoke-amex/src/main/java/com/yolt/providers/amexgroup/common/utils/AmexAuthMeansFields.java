package com.yolt.providers.amexgroup.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AmexAuthMeansFields {

    public static final String CLIENT_ID = "client-id";
    public static final String CLIENT_SECRET = "client-secret";
    public static final String CLIENT_TRANSPORT_KEY_ID_ROTATION = "client-transport-private-keyid-rotation";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_ROTATION = "client-transport-certificate-rotation";
    //C4PO-9807 new set of auth means for OB
    public static final String CLIENT_ID_2 = "client-id-2";
    public static final String CLIENT_SECRET_2 = "client-secret-2";
    public static final String TRANSPORT_PRIVATE_KID_2 = "transport-private-kid-2";
    public static final String TRANSPORT_CERTIFICATE_2 = "transport-certificate-2";
}
