## Deutsche Kreditbank AG (DKB)
[Current open problems on our end][1]

## BIP overview

|   |   |
|---|---|
| **Country of origin** | Germany | 
| **Site Id**  |cc6b36d7-e989-428c-ac1c-da5a51aa6d59     |
| **Standard**   | Berlin |
| **Contact**  |E-mail: kundenportal@crealogix.com |
| **Developer Portal** |https://api.dkb.de/store | 
| **Account SubTypes**|Current |
| **IP Whitelisting**| No |
| **AIS Standard version**  |1.3.6|
| **PISP Standard version**  |1.3.6|
| **Auto-onboarding**|No, but they require empty request to save our QWAC on database.|
| **Requires PSU IP address** |Yes|
| **Type of certificate** |Eidas certificate required: QWAC|
| **Signing algorithms used**|  |
| **Mutual TLS Authentication Support**|  |

## Links - sandbox


|   |   |
|---|---|
| **Base URL** | https://api.dkb.de/psd2sandbox/1.3.6| 
| **Authorization Endpoint**|  |
| **Token endpoint** |  |

## Links - production


|   |   |
|---|---|
| **Base URL** | https://api.dkb.de/psd2/1.3.6 | 
| **Authorization Endpoint**| https://api.dkb.de/pre-auth/1.0.6 |
| **Token endpoint** |  |

## Client configuration overview

|   |   |
|---|---|
| **Transport key id**  | eIDAS transport key id  |
| **Transport certificate** | eIDAS transport certificate |

## Registration details

There is no registration, but they require to cal /consents endpoint (without token) just, so they would register our
eIDAS transport certificate (QWAC). After this call they will create us as TPP in their database and future interactions
with the API can start directly with embedded token call.

## Connection Overview

DKB is an example of decoupled embedded flow. To perform correct authorization you need to perform 5 requests:

1. First you need to obtain bearer token which will be used to every other API call. To achieve that you need to send
   request on **/psd2/v1/auth/token** (Pre-StepAuthorizationAPI) with user credentials in body.
2. With that token you need to send request to **/psd2/v1/consents** endpoint to create Consent itself, you will receive
   consent id. This token is also needed to in every other API request, so it won't be mentioned anymore.
3. With received consent id and token from first step you need to create authorization through **
   /psd2/v1/consents/<consentId>/authorisations** endpoint. You will receive authorisationId and list of available SCA
   methods, but DKB already informed us that there will always be same pair of them ChipTan and PushTan. Inside those
   objects there will be individual authenticationMethodId's.
4. Then this authorization have to be triggered, so you need to send request on **/psd2/v1/consents/<consentId>
   /authorisations/<authorisationId>**
   with chosen authorisationMethodId in body.
5. Last but not least you need to call **/psd2/v1/consents/<consentId>/authorisations/<authorisationId>** one more time,
   but this time passing OTP from user in request body.

## Sandbox overview

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 

## Business and technical decisions

26.08.2021\
Since registration call requires PSU ip address header and from obvious reasons there is no user in that case we have to
fill in this gap. There is an appropriate instruction in their documentation for such case:
"If not available, the TPP shall use the IP Address used by the TPP when submitting this request."
Given this fact developers decided that we will send one of our app-prd public addresses, when it comes to the
registration.

## External links

* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/jira/software/c/projects/C4PO/components?filter=DKB&orderDirection=DESC&orderField=NAME&page=1>
