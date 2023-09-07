# open-banking

### Certificate rotation

Certificate rotation is non-invasive as it does not affect users' sessions. 
New qSEAL/qWAC certificate must be registered with open banking JWKS URL - it can be checked by [jwks store][1].
Authentication means must be updated with new openbanking 'kid', certificates and ids to Cloud HSM. 
In case of changes in SSA new registration is required or update of existing one by calling [PUT registration endpoint][2].
Information about rotation and current certificates can be found in [Bank Connections][3].

[1]: <https://keystore.openbanking.org.uk/001580000103UAYAA2/{SOFTWARE_ID}.jwks>
[2]: <https://developer.cybonline.co.uk/our-apis/registration-and-token/dynamic-registration/v/3.2/>
[3]: <https://yolthub.pl.ing.net/Lists/Bank_connections/AllItems.aspx?web=1>

## Virgin Money

Business decisions:
* 2020-09-15 - As described in https://yolt.atlassian.net/browse/C4PO-4814 we will rethrow 403 in case it comes with "The owner of this website (secureapi.prod.ob.virginmoney.com) has banned your IP address" string in its body instead of throwing TokenInvalidException that forces user to re-consent. It is due to fact that that is how bank handles service downtime - by responding with 403 and mentioned string and we do not want to force users to re-consent every time bank have service downtime.

* 2020-09-18 - Bank returns 404 when we ask for Beneficiaries of account with Current Account type, hence we are skipping beneficiaries retrieval for Current Account

* Documentation https://developer.virginmoney.com/

##RBS
* Remittance Information reversed:
Bank supports only Reference type of RemittanceInformation and since description of a payment that user types in is sent as RemittanceUnstructured we had to reverse it. (Source: https://yolt.atlassian.net/browse/C4PO-1244)
* Domestic Payments Scheme - The only supported payment scheme is UK.OBIE.SortCodeAccountNumber. (Source: https://www.bankofapis.com/products/natwest-group-open-banking/payments/documentation/nwb/3.1.4#domestic-payments)
* Limit of EndToEndIdentification:
According to model described in RBS doc (https://www.bankofapis.com/products/natwest-group-open-banking/payments/documentation/nwb/3.1.4#domestic_payment_consents__post ), Faster Payments Scheme (which is used as default when LocalInstrument is not provided) can have  EndToEndIdentification field length max 31 (30 according to response to ticket on their service desk: https://rbsgroupapiservicedesk.spectrumhosting.net/plugins/servlet/desk/portal/1/RSD-1959). We throw error when endToEndIdentification is too long
* Limit of Reference:
According to model described in RBS doc (https://www.bankofapis.com/products/natwest-group-open-banking/payments/documentation/nwb/3.1.4#domestic_payment_consents__post ), Faster Payments Scheme (which is used as default when LocalInstrument is not provided) can have  Reference field length max 18. We throw error when Reference is too long
  
## Payment Execution Context
# providerState support for getStatus request
In OB getStatus flow can be called immediately by s-m after initiation flow or after submit flow. 
In case of calling getStatus after *initiation* flow we should receive null/empty paymentId in request DTO 
and there should be providerState with consentId inside it. 
In case of calling getStatus after *submition* flow, we should receive paymentId in request DTO and providerState should be null/empty.
If we receive *paymentId* we have to call **/domestic-payments/{paymentId}** endpoint to get the payment status.
If we receive *consentId* in providerState instead, we have to call **/domestic-payment-consents/{consentId}** endpoint to get payment consent status.