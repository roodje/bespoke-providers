## Banque Populaire (PIS)
[Current open problems on our end][1]

French bank

## BIP overview 

|                                       |                                        |
|---------------------------------------|----------------------------------------|
| **Country of origin**                 | France                                 | 
| **Site Id**                           | 1d223488-a172-11e9-a2a3-2a2ae2dbcce4   |
| **Standard**                          | STET                                   |
| **Contact**                           | E-mail: 89c3_api_support@i-bp.fr       |
| **Developer Portal**                  | https://www.api.89c3.com/en/           |
| **IP Whitelisting**                   | No                                     |
| **PIS Standard version**              | 1.0.0                                  |
| **Requires PSU IP address**           | Yes                                    |
| **Type of certificate**               | eIDAS                                  |
| **Signing algorithms used**           | rsa-sha256                             |
| **Mutual TLS Authentication Support** | Yes                                    |
| **Repository**                        | https://git.yolt.io/providers/stet     |

## Links - sandbox

|                    |                                                           |
|--------------------|-----------------------------------------------------------|
| **Base URL**       | www.<bankcode>.sandbox.api.89C3.com                       |
| **Token Endpoint** | www.<bankcode>.sandbox.api.89C3.com/stet/psd2/oauth/token |  

## Links - production 

|              |                                     |
|--------------|-------------------------------------|
| **Base URL** | https://www.10807.live.api.89c3.com |


## Client configuration overview

|                           |                                                       |
|---------------------------|-------------------------------------------------------|
| **Signing key id**        | Signing key id                                        | 
| **Signing certificate**   | Signing certificate (eidas)                           | 
| **Transport key id**      | Transport key id                                      |
| **Transport certificate** | Transport certificate (eidas)                         |
| **Client Id**             | Client Id obtained from dynamic registration          |
| **Client Email**          | Contact e-mail sent to bank                           | 
| **Client Name**           | Client application name (shown on authorization page) | 

## Connection Overview

payments are execute-on-submit, it means on our confirm payment step we can only call for payment status
it's possible to initiate a payment without having debtor account
At payment status AcceptedSettlementCompleted we can be sure that the money was transferred from the debtor to creditor
   
## Sandbox overview

not used

## Consent validity rules

not implemented

## Payment Flow Additional Information

|                                                                                                        |                             |
|--------------------------------------------------------------------------------------------------------|-----------------------------|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?**                    | execute-on-submit           |
| **it is possible to initiate a payment having no debtor account**                                      | YES / NO                    |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | AcceptedSettlementCompleted |

## Business and technical decisions


## External links
* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>