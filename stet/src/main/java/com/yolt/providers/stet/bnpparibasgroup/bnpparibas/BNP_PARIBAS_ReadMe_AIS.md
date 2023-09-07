## BNP Paribas (AIS)

[Current open problems on our end][1] - //Fill component of particular bank.

## BIP overview

|                                       |                                                                                                           |
|---------------------------------------|-----------------------------------------------------------------------------------------------------------|
| **Country of origin**                 | France                                                                                                    | 
| **Site Id**                           | 1c270f38-ce5d-4b23-876f-fa73574d26ba                                                                      |
| **Standard**                          | STET                                                                                                      |
| **Contact**                           | E-mail: paris_bddf_support_api_dsp2@bnpparibas.com                                                        |
| **Developer Portal**                  | https://apistore.bnpparibas/en/marketplace/AISP-Mabanque-France/8337de9d-a64e-4c95-bd8f-22b4f72ce7e0/true | 
| **Account SubTypes**                  | CURRENT_ACCOUNT                                                                                           |
| **IP Whitelisting**                   |                                                                                                           |
| **AIS Standard version**              |                                                                                                           |
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

|           |                                                                                                                                  |
|-----------|----------------------------------------------------------------------------------------------------------------------------------|
| **Login   |                                                                                                                                  |
| domains** | [connexion-mabanque.bnpparibas](connexion-mabanque.bnpparibas) <br> [api-nav-psd2.bddf.bnpparibas](api-nav-psd2.bddf.bnpparibas) | 

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

## Sandbox overview

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method.

## Business and technical decisions

C4PO-4295  
Accounts with organisation usage type should be ignored.

Information from bank's swagger:   
Transactions are sent from newest to oldest booking date. Deferred debit card transactions are integrated with others
transactions. No pagination data. DateTo / DateFrom : max 3 months period is returned.

C4PO-4437  
In bnp-paribas-bespoke we built signature based on headers in order:
'Authorization accept content-type digest x-request-id'. We are not sure, this order is required. We are going to try to
create it using default order

C4PO-4437  
From bnp-paribas-bespoke implementation shows, that during creating signature and digest we need to create json from
requestBody with all fields also with empty fields.

C4PO-4437  
During testing  migrated provider, we have noticed, we didn't move properly mapping. In bespoke date value "" was mapped as null. That should be consistent for group, so we decided to change it for whole STET generic.

* C4PO-9794
  We decided to turn on filtering other than Booking and Other transactions
## External links

* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>

### URL for API documentation

https://apistore.bnpparibas/en/marketplace/AISP-Mabanque-France/8337de9d-a64e-4c95-bd8f-22b4f72ce7e0/true

### Connections that BNP Paribas has

* Connexis Cash
* Mabanque - France
* Hello bank! - France
* MaBanqueEntreprise - France

### Our current implementation

* BnpParibasDataProvider with BNP_PARIBAS provider key <-> 'Mabanque - France' connection
* HelloBankDataProvider with HELLO_BANK provider key <-> 'Hello bank! - France'

**Consent validity rules** are implemented for BNP Paribas bank AIS.
