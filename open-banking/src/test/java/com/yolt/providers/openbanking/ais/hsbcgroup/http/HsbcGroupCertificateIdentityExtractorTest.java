package com.yolt.providers.openbanking.ais.hsbcgroup.http;

import com.yolt.providers.openbanking.ais.hsbcgroup.common.http.HsbcGroupCertificateIdentityExtractor;
import lombok.SneakyThrows;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.StringJoiner;

import static org.assertj.core.api.Assertions.assertThat;

class HsbcGroupCertificateIdentityExtractorTest {

    private HsbcGroupCertificateIdentityExtractor certificateIdentityExtractor;

    @BeforeEach
    void beforeEach() {
        certificateIdentityExtractor = new HsbcGroupCertificateIdentityExtractor();
    }

    @Test
    void extractCertificateIdentityInformationText() {
        // given
        X509Certificate certificate = createSelfCertificate();

        // when
        String certificateIdentityInformation = certificateIdentityExtractor.apply(certificate);

        // then
        assertThat(certificateIdentityInformation).isEqualTo(getExpectedCertificateIdentityInformation());
    }

    @SneakyThrows
    private X509Certificate createSelfCertificate() {
        X500Name x500Name = createX500Name();
        BigInteger randomNumber = getRandomNumber();
        Date date = getCurrentDate();
        KeyPair keyPair = createKeyPair();

        X509CertificateHolder holder = new JcaX509v3CertificateBuilder(x500Name, randomNumber, date, date, x500Name, keyPair.getPublic())
                .build(new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                        .build(keyPair.getPrivate()));

        return new JcaX509CertificateConverter().getCertificate(holder);
    }

    private BigInteger getRandomNumber() {
        return new BigInteger(64, new SecureRandom());
    }

    private Date getCurrentDate() {
        return Date.from(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    @SneakyThrows
    private KeyPair createKeyPair() {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    private X500Name createX500Name() {
        String subjectName = new StringJoiner(",")
                .add("C=FR")
                .add("O=Fake Company")
                .add("CN=fake.io")
                .add("L=Paris")
                .add("OID.1.3.6.1.4.1.311.60.2.1.3=FR")
                .add("OID.2.5.4.17=8872FK")
                .add("OID.2.5.4.15=Fake Organization")
                .add("STREET=Mosquito 112")
                .add("OID.2.5.4.97=PSDFR-TWS-B6221")
                .add("OID.2.5.4.5=24015782")
                .add("ST=Fake-France")
                .toString();
        return new X500Name(subjectName);
    }

    private String getExpectedCertificateIdentityInformation() {
        return new StringJoiner(",")
                .add("ST=Fake-France")
                .add("serialNumber=24015782")
                .add("2.5.4.97=#0C0F50534446522D5457532D4236323231")
                .add("street=Mosquito 112")
                .add("2.5.4.15=Fake Organization")
                .add("2.5.4.17=8872FK")
                .add("1.3.6.1.4.1.311.60.2.1.3=FR")
                .add("L=Paris")
                .add("CN=fake.io")
                .add("O=Fake Company")
                .add("C=FR")
                .toString();
    }
}
