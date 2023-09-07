## Credit Agricole (AIS)
[Current open problems on our end][1]


## BIP overview

|                                       |                                        |
|---------------------------------------|----------------------------------------|
| **Country of origin**                 | France                                 | 
| **Site Id**                           | 4a1230ad-bd80-4122-9ebb-7abd3f256d80   |
| **Standard**                          | [STET Standard][2]                     |
| **Contact**                           | E-mail: hello.DSP2@ca-ts.fr            |
| **Developer Portal**                  | https://developer.credit-agricole.fr/  | 
| **Account SubTypes**                  |                                        |
| **IP Whitelisting**                   | Not required                           |
| **AIS Standard version**              | 1.4.1                                  |
| **PISP Standard version**             | 1.4.1                                  |
| **Auto-onboarding**                   | YES                                    |
| **Requires PSU IP address**           |                                        |
| **Type of certificate**               | eidas                                  |
| **Signing algorithms used**           |                                        |
| **Mutual TLS Authentication Support** |                                        |
| **Repository**                        | https://git.yolt.io/providers/stet     |

## Links - sandbox
Example fields (for each bank fields might be different)


|                            |     |
|----------------------------|-----|
| **Base URL**               |     | 
| **Authorization Endpoint** |     |
| **Token endpoint**         |     |

## Links - production
|                      |                                                                                                                                    |
|----------------------|------------------------------------------------------------------------------------------------------------------------------------|
| **Registration URL** | https://api.credit-agricole.fr/psd2_actor_registration/v1/tpp_consumers                                                            |
| **Login domains**    | [psd2-portal.credit-agricole.fr](psd2-portal.credit-agricole.fr) <br> [psd2-portal.banque-chalus.fr](psd2-portal.banque-chalus.fr) | 

## Client configuration overview

|                                  |                                                                            |
|----------------------------------|----------------------------------------------------------------------------|
| **Signing key id**               | eIDAS signing key id (QSEAL)                                               | 
| **Signing certificate**          | eIDAS signing certificate (QSEAL)                                          | 
| **Transport key id**             | eIDAS transport key id (QWAC)                                              |
| **Transport certificate**        | eIDAS transport certificate (QWAC)                                         |
| **Client contact email address** | used for autoonboarding as a mean of communication                         |
| **Client name**                  | registration creates an app. This is the app name which needs to be unique |



## Registration details
Although Credit Agricole has multiple regions it requires just one registration. It requires an unique client name. 

### Delete registration
Deletion of registration was not recognized, therefore no REST communication with the bank in order to delete the registration.
Please note that the current request to delete the registration will be performed by deleting it only on our side.

## Connection Overview
Credit Agricole has many regions and dynamic flow. 

**Consent validity rules**
Credit Agricole AIS uses dynamic flow, thus we are unable to determine consent validity rules for AIS.

## Sandbox overview

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method.

## Business and technical decisions

C4PO-9794
We decided to filter pending transactions, because we had an issue with empty bookingDate.
And these transactions are filtered later by core team.

## External links
* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%22Credit%20Agricole%22%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: <https://www.stet.eu/en/psd2/>
