## Amex (AIS)
[Current open problems on our end][1]

## BIP 

|                                       |                                                                                                                                                                                                                                                                                                                                                           |
|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Country of origin**                 | UK                                                                                                                                                                                                                                                                                                                                                        | 
| **Site Id**                           | 75457be7-96d3-4fc1-98a2-98ded940b563                                                                                                                                                                                                                                                                                                                      |
| **Contact**                           | Through email: openbankingsupport@aexp.com At the Grant Access Journey URL, At the endpoints of the API Through email: openbanking@devmail.americanexpress.com (On boarding clients to Sandbox and Production, New clients, Redirect URLs, Updating existing clients, Redirect URLs, Technical and Legal topics regarding the configurations of the API.) |
| **Standard**                          | bespoke                                                                                                                                                                                                                                                                                                                                                   |
| **Developer portal**                  | E-mail: https://developer.americanexpress.com/                                                                                                                                                                                                                                                                                                            |
| **Account SubTypes**                  | Credit                                                                                                                                                                                                                                                                                                                                                    | 
| **IP Whitelisting**                   | No                                                                                                                                                                                                                                                                                                                                                        |
| **AIS Standard version**              | API does not have any version, changes are made live                                                                                                                                                                                                                                                                                                      |
| **Auto-Onboarding**                   | None                                                                                                                                                                                                                                                                                                                                                      |
| **Requires PSU IP address**           | No                                                                                                                                                                                                                                                                                                                                                        |
| **Type of Certificate**               | OBWAC is needed                                                                                                                                                                                                                                                                                                                                           |
| **Signing algorithms used**           | MAC - Message Authentication Code                                                                                                                                                                                                                                                                                                                         |
| **Mutual TLS Authentication support** | Yes                                                                                                                                                                                                                                                                                                                                                       |
| **Repository**                        | https://git.yolt.io/providers/bespoke-amex                                                                                                                                                                                                                                                                                                                |

## QA

|                             |                                                                                    |
|-----------------------------|------------------------------------------------------------------------------------|
| **Host**                    | https://api.qa.americanexpress.com                                                 |
| **OAuth Access Token URL**  | https://api.qa.americanexpress.com/apiplatform/v2/oauth/token/mac                  |
| **OAuth Refresh Token URL** | https://api.qa.americanexpress.com/apiplatform/v1/oauth/token/refresh/mac          |
| **OAuth Revoke Token URL**  | https://api.qa.americanexpress.com/apiplatform/v2/oauth/token_revocation/mac       |

## Links - production 

|                             |                                                                                    |
|-----------------------------|------------------------------------------------------------------------------------|
| **Host**                    | https://api2s.americanexpress.com                                                  |
| **OAuth Access Token URL**  | https://api2s.americanexpress.com/apiplatform/v2/oauth/token/mac                   |
| **OAuth Refresh Token URL** | https://api2s.americanexpress.com/apiplatform/v1/oauth/token/refresh/mac           |
| **OAuth Revoke Token URL**  | https://api2s.americanexpress.com/apiplatform/v2/oauth/token_revocation/mac        |
| **Login domains**           | www.americanexpress.com <br> [m.amex](m.amex)                                      | 
| **Documentation**           | https://developer.americanexpress.com/products/account-financials-eu/guide#details |

## Links - sandbox
|              |                                      |
|--------------|--------------------------------------|
| **Sandbox**  | https://api.qasb.americanexpress.com | 


## Difference between Sandbox and QA: 

The Sandbox environment is designed to let you play with our products before moving into a formal testing environment. It provides a limited, but meaningful dataset that anyone can use.
QA is our live integration environment, where you can test in preparation for a production launch. It provides dynamic test data, full backend process execution, and outbound communications. You may be asked to answer some questions before receiving QA access.

## Client configuration overview
|   |   |
|---|---|
| **Transport key id**  |  OBWAC key id |
| **Transport certificate** | OBWAC certificate |
| **Client id** | Received from AMEX after registration |
| **Client secret** | Received from AMEX after registration |  
 
### Registration  details
Type of registration is manual.
Registration process is described here: https://developer.americanexpress.com/products/account-financials-eu/guide#start
Simply the process is based on sending e-mail with proper (QWAC or OBWAC) certificate, after this AMEX is configuring the Production Grant Access Journey.

### Certificate rotation

## Connection Overview
**Consent validity rules**

AMEX AIS consent page is an SPA, thus we are unable to determine consent validity rules.

## Business and technical decisions

**2021.01.05** - due to FCA requirements we decided to split implementation to two separate providers for GB and other EU users.

## Sandbox overview

## User Site deletion
There's `onUserSiteDelete` method implemented by this provider as it was crucial for a proper provider operation.

### Credentials
In order to log in to the developer portal of bank you should standard credential which can be found on our KeyBase team files.
  
## External links
* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/browse/C4PO-4268?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20Amex%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
