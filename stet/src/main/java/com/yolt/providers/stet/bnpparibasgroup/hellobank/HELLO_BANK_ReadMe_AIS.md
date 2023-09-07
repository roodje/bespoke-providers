## Hello bank! (AIS)
[Current open problems on our end][1] - //Fill component of particular bank.

## BIP overview 

|                                       |                                                                                                           |
|---------------------------------------|-----------------------------------------------------------------------------------------------------------|
| **Country of origin**                 | France                                                                                                    | 
| **Site Id**                           | 7d1b1bff-e04b-446a-9ab0-44b456d14d42                                                                      |
| **Standard**                          | STET                                                                                                      |
| **Contact**                           | E-mail: paris_bddf_support_api_dsp2@bnpparibas.com                                                        |
| **Developer Portal**                  | https://apistore.bnpparibas/en/marketplace/AISP-Mabanque-France/8337de9d-a64e-4c95-bd8f-22b4f72ce7e0/true | 
| **Account SubTypes**                  | CURRENT_ACCOUNT                                                                                           |
| **IP Whitelisting**                   |                                                                                                           |
| **AIS Standard version**              |                                                                                                           |
| **PISP Standard version**             |                                                                                                           |
| **Auto-onboarding**                   | yes                                                                                                       |
| **Requires PSU IP address**           | no                                                                                                        |
| **Type of certificate**               ||
| **Signing algorithms used**           | SHA 256 with RSA                                                                                          |
| **Mutual TLS Authentication Support** | yes                                                                                                       |
| **Repository**                        | https://git.yolt.io/providers/stet                                                                        |

## Links - sandbox
Example fields (for each bank fields might be different)


|                            |                                                              |
|----------------------------|--------------------------------------------------------------|
| **Base URL**               | https://api.sandbox.bddf.bnpparibas/psd2-sandbox/retail/V1.4 | 
| **Authorization Endpoint** | /authorize                                                   |
| **Token endpoint**         | /token                                                       |

## Links - production 
|                   |                                                                                                                            |
|-------------------|----------------------------------------------------------------------------------------------------------------------------|
| **Login domains** | [api-nav-psd2.bddf.bnpparibas](api-nav-psd2.bddf.bnpparibas) <br> [espace-client.hellobank.fr](espace-client.hellobank.fr) | 

## Client configuration overview

|                                      |                                                                                   |
|--------------------------------------|-----------------------------------------------------------------------------------|
| **Signing key id**                   | Signing key id                                                                    | 
| **Signing certificate**              | Signing certificate eidas                                                         | 
| **Transport key id**                 | Transport key id                                                                  |
| **Transport certificate**            | Transport certificate eidas                                                       |
| **Client id**                        | Client id obtained via autoonboarding                                             | 
| **Client secret**                    | Client secret obtained via autoonboarding                                         | 
| **Registration access token**        | Token used for update registration. It is equal to clientId for old registrations | 
| **Client logo uri (URL encoded)**    | Logo url to be shown on consent page                                              | 
| **Client website uri (URL encoded)** | Website uri of the registration client                                            | 
| **Client e-mail**                    | E-mail for contact with client                                                    | 

## Registration details

Registration is mostly standard. Keep in mind that registration scopes must not exceed the scopes within the certificate. The API supports standard CRUD operations.
It returns three values connected with the registration client id, client secret and registration access token. The registration access token we keep as an authentication mean is used for read update delete operations of registrations API.
Only UPDATE operations is implemented at the moment. The update call itself is asynchronous and the operation requires approval for a bank. So each time you do a successful update you will see a message like this (check via RDD):
`{ "message":"Your update request has been recorded. We will contact you to confirm." }`

The registrations are being shared between Hello Bank and BNP Paribas, so there is no need to register these banks separately.

## Connection Overview

**Consent validity rules** are implemented for Hello Bank AIS & Bnp Paribas Bank AIS. Due to a problem with Consent Testing
mechanism, it was turned off for this bank and will be fixed as part of C4PO-10504

## Sandbox overview

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method.

## Business and technical decisions

27.01.2021  
In result of Bank changes, that is: to introduce transaction status OTHR for Hello Bank
was decided to map OTHR to PENDING for Hello Bank. To achieve this in the smoothest way the StetPsd2Api_1.4.1_OAS3_retail_aisp_T1.2.0.json
was extended with OTHR transaction status. The change is moved to StetPsd2Api_1.4.1_OAS3_retail_aisp_T1.2.0_27012021.json file
which became as a source file to generate DTOs.

C4PO-4437  
From bnp-paribas-bespoke implementation shows, that during creating signature and digest we need to create json from
requestBody with all fields also with empty fields.

* C4PO-9794
  We decided to turn on filtering other than Booking and Other transactions

**Payment Flow Additional Information**

|                                                                                                        |                             |
|--------------------------------------------------------------------------------------------------------|-----------------------------|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?**                    | execute-on-consent          |
| **it is possible to initiate a payment having no debtor account**                                      | YES                         |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | AcceptedSettlementCompleted |

## External links
* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>


### URL for API documentation
https://apistore.bnpparibas/libraries/45269a1d-011a-4b0e-b0d9-a7281ac7431f/true

### Connections that BNP Paribas has
* Connexis Cash
* Mabanque - France
* Hello bank! - France
* MaBanqueEntreprise - France

### Our current implementation
* BnpParibasDataProvider with BNP_PARIBAS provider key <-> 'Mabanque - France' connection
* HelloBankDataProvider with HELLO_BANK provider key <-> 'Hello bank! - France'
