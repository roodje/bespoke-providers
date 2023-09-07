## Comdirect (AIS)

## BIP overview 
|                                       |                                                  |
|---------------------------------------|--------------------------------------------------|
| **Country of origin**                 | Germany                                          | 
| **Site Id**                           | da1f5218-16ee-4ce3-ab70-58e6bcb78354             |
| **Standard**                          | Berlin                                           |
| **Contact**                           | xs2a-support@comdirect.de                        |
| **Developer Portal**                  |                                                  |
| **Account SubTypes**                  | Current Accounts                                 |
| **IP Whitelisting**                   | No                                               |
| **AIS Standard version**              | Version 5.X (base on Berlin 1.3.4 version)       |
| **Auto-onboarding**                   | No                                               |
| **Requires PSU IP address**           | Yes                                              |
| **Type of Certificate**               | eIDAS QWAC                                       |
| **Signing algorithms used**           | No signing                                       |
| **Mutual TLS Authentication Support** | Yes                                              |
| **Repository**                        | https://git.yolt.io/providers/bespoke-axa-banque |

## Links - production 
|                    |                                               |
|--------------------|-----------------------------------------------|
| **Login domains**  | [psd.comdirect.de](https://psd.comdirect.de)  | 

## Client configuration overview
|                           |                                                     |
|---------------------------|-----------------------------------------------------|
| **Signing key id**        | There is no signing in comdirect                    | 
| **Signing certificate**   | There is no signing in comdirect                    | 
| **Transport key id**      | Eidas transport key id                              |
| **Transport certificate** | Eidas transport certificate                         |
| **Client id**             | Equal to institutionId taken from eidas certificate | 

### Registration details
There is no registration.

### Certificate rotation
As there is no registration or signing, rotation isn't needed.

## Connection Overview
![Connection](https://xs2a-developer.comdirect.de/sites/all/modules/ew_content_generation/content/howto/images/01-AIS-Workflow.png)

**Consent validity rules** are set to EMPTY_RULES_SET for Comdirect bank due to error page shares same HTML code as a correct one.

## Business decisions
Currency codes are returned accordingly to: https://www.iban.com/currency-codes
ClientId in requests is institutionId taken from eidas certificate.

## Sandbox overview
We were unable to connect to sandbox since it required test eidas certificates.


**Payment Flow Additional Information**

|                                                                                                        |                             |
|--------------------------------------------------------------------------------------------------------|-----------------------------|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?**                    | execute-on-consent          |
| **it is possible to initiate a payment having no debtor account**                                      | YES                         |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | AcceptedSettlementCompleted |


## External links
* [Current open problems on our end][1]
* [Developer portal][2]
* [Sandbox Base URL][3]
* [API Base URL][4]
 
[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status%20%3D%20Comdirect>
[2]: <https://xs2a-developer.comdirect.de/>
[3]: <https://xs2a-sandbox.comdirect.de>
[4]: <https://xs2a-api.comdirect.de>
