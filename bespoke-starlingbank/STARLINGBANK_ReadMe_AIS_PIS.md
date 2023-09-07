# Starling Bank

|   |   |
|---|---|
| **Country of origin** | United Kingdom |
| **Site Id**  |  ee4f87b5-68a6-4464-b769-d41737109458 |
| **Standard**   | bespoke |
| **Account types**| CURRENT_ACCOUNT |
| |which corresponds to their: personal, joint, euro and business accounts types |
| **Contact**  | E-mail: developer@starlingbank.com |
| **Developer Portal** | [Developer portal][2] |
| **Documentation** | [Developer portal][2] |
| **IP Whitelisting**| No |
| **PIS Standard version**  | 2.0 |
| **Auto-onboarding**| Yes |
| **Requires PSU IP address** | No |
| **Type of certificate** | QSEAL |
| **Signing algorithms used**| SHA256withRSA |

## AIS Registration details:
**Our registration on their side includes couple of applications. We actively use two of them both are named "Yolt".**
**Registration used for AIS related calls has client id qPpZlc6dt5eaYYyy7nl2**

AIS connection works only with client_id (api-key) and client_secret (api-secret). Both of them can be found after logging into Starling [Developer portal][2].\
Starling used to required valid eIDAS certificate to grant permissions (it was not used to sign requests). 
During FCA migration their developer portal was not ready, so they have adjusted our registration for OB certs (OBSEAL & OBWAC) on their side manually (12.01.2021).

## PIS Registration details:
**Our registration on their side includes couple of applications. We actively use two of them both are named "Yolt".**
**Registration used for PIS related calls has client id YWQkwj06mhnTtXZL3afT**

Apart from client_id (api-key) and client_secret (api-secret) payment endpoint requires signing. Public key had to be uploaded into developer
portal and request had to be signed with corresponding private key.\
It was decided to use **OBSEAL** certificate in this case (YTS for Yolt app OBSEAL December 2020, kid: 3a47f4cc-529c-40f2-9457-2a5997c49fa1).\
\
Private key is stored in HSM, so auth means for PIS contains additionally:
 * signing-private-key-id to retrieve key from HSM,
 * private-signing-key-header-id which identifies our public key on Starling side.
 
Starling used to required valid eIDAS certificate to grant permissions (it was not used to sign requests). 
During FCA migration their developer portal was not ready, so they have adjusted our registration for OB certs (OBSEAL & OBWAC) on their side manually (12.01.2021).

## Connetion overview:

**Consent validity rules**
Both Starlingbank AIS & PIS consent pages are SPA, thus we are unable to determine consent validity rules.

## Certificate rotation:

Public key used for signing has to be uploaded into developer portal with another public key that will be used in case of rotation. \
It was decided to use **OBWAC** (YTS for Yolt app OBWAC December 2020, kid: b680ebd5-9934-40ed-bbf5-444ea8eb9d89) certificate as a rotation key.
To rotate key you have to generate signature of: digest of new public key and date when the signature was generated.
You have only 5 minutes to upload new public key and signature to Starling's developer portal.
Full details can be found at CertificateRotationSignatureGenerator.class which was removed in commit: \
Revision number: daaec528ee2c3f1fe74d8042fc98f86680d5a8a8 (Oct 23 2020 09:26 GMT+0200, used with eIDAS then).
Code can be found in:
Revison number: 95f5f052416177a2634027322e34175b179b52d2 (option in code: 'Suggested by Starling's developer')

## Sandbox
URL: https://api-sandbox.starlingbank.com\
Each application registration has its own sandbox registration included, so client_id (api-key) and client_secret (api-secret) 
are shared between sandbox and production. \
Keys required for signing are not shared. Public keys have been uploaded into dev portal and private keys have been committed into src/test/resources/starlingbank/certificates/sandbox.

## Notes:
#### Differences in balances from the Starlingbank balance API:
  
 * **effectiveBalance** is the amount that is REALLY on your bank account
 
 * **availableToSpend** is the amount that you can spend until you reach your max negative limit
> Example:
> - max negative: -250
> - current balance: 100
> - availableToSpend: 350

#### Consent specific behaviour:
Within an authorization user get access on base of the account 'holder name', in case of different 'holder names' 
the consent cannot be supplied and user need to reauthenticate to switch between accounts with the same 'holder name'.
It can be bypassed by adding account multiple time but only using IOS platform, ANDROID platform does not support it 
till YOLT2 app version is available for the user.

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 

##Business decisions
 * 08.09.2020 Starling in its PIS api expects to provide amount in minor units (cents for EUR or pence for GBP). 
 As providers received from payment amount in major units it needs to be multiplied by 100 to convert it to minor units.
 It was discussed with Malina Ciolpan and Ruben van Leon. They both accepted this approach.

**Payment Flow Additional Information**
 * Because of StarlingBank PIS flow the C4PO decided to return INITIATE_SUCCESS after initiate payment step which in JSON response no 'status' is returned .
   Payment status is getting on getStatus step and on this point we discover whether payment is REJECTED/PENDING/ACCEPTED.

|   |   |
|---|---|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?** | execute-on-submit (one-step flow only) |
| **it is possible to initiate a payment having no debtor account** | YES |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | In Starling Bank there are three possible values (ACCEPTED, REJECTED, PENDING). We decided to return INITIATION_SUCCESS after payment initiation. In further steps, when payment was made, COMPLETED status or REJECTED is returned. |

## Links
* [Open issues connected to Starling][1]
* [Developer portal][2]
 
[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20summary%20~Starling%20AND%20status%20!%3D%20Done%20AND%20status%20!%3D%20Canceled%20AND%20status%20!%3D%20%22LIVE%20MAINTENANCE%22%20ORDER%20BY%20status>
[2]: <https://developer.starlingbank.com/docs>
