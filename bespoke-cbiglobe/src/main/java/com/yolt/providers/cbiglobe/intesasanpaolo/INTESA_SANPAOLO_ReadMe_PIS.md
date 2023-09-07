## Intesa Sanpaolo (PIS)
[Current open problems on our end][1]

## BIP overview 
[Main reference BIP][2]

|                                       |                                                |
|---------------------------------------|------------------------------------------------|
| **Country of origin**                 | Italy                                          | 
| **Site Id**                           | 51d3931c-da6a-4e42-b3de-1c688eb9929c           |
| **Standard**                          | [Berlin Group Standard][3]                     |
| **Contact**                           | Email: cbiglobe@cbi-org.eu                     |
| **Developer Portal**                  | https://cbiglobeopenbankingapiportal.nexi.it/  |
| **IP Whitelisting**                   | No                                             |
| **PIS Standard version**              | 1.1                                            |
| **Requires PSU IP address**           | Yes                                            |
| **Type of certificate**               | eIDAS                                          |
| **Signing algorithms used**           | rsa-sha512                                     |
| **Mutual TLS Authentication Support** | Yes                                            |
| **Repository**                        | https://git.yolt.io/providers/bespoke-cbiglobe |

## ASPSP list
|  Code | Name                      |
|-------|---------------------------|
| 03069 | **Intesa Sanpaolo S.p.A** |

## Links - production
|                   |                                         |
|-------------------|-----------------------------------------|
| **Base URL**      | https://tppinterface.intesasanpaolo.com |

## Client configuration overview
|                           |                                                                               |
|---------------------------|-------------------------------------------------------------------------------|
| **Transport key id**      | eIDAS transport key id                                                        |
| **Transport certificate** | eIDAS transport certificate (QWAC)                                            |
| **Signing key id**        | eIDAS signing key id                                                          |
| **Signing certificate**   | eIDAS signing certificate (QSEAL)                                             |
| **Client id**             | The client identifier that is returned during registration process            |
| **Client secret**         | The secret that is returned with set of client id during registration process |

### Registration details
Registration from AIS is used

### Certificate rotation
Refer AIS ReadMe

## Connection Overview
The swagger can be downloaded from here: [Technical Documents][5] (corrupted one).
The connection requires mutual TLS with QWAC eIDAS certificate.
The signature of the request is required and it based on [Cavage HTTP Signatures][7].

**Consent validity rules**

Intesa SanPaolo PIS consent page is an SPA, thus we are unable to determine consent validity rules for PIS.

### Simplified flow chart
```mermaid
graph TD
  A{Has single ASPSP?}
  A-->|Yes|B[Initiate payment]
  A-->|No|C[Stop payment flow]
  B-->D[Establish consent]
  D-->E[Payment is sent automatically on the bank side - no submit is required]
```

### Simplified sequence diagram
```mermaid
sequenceDiagram
  autonumber
  participant PSU (via app);
  participant Yolt (providers);
  participant ASPSP;
  PSU (via app)->>+Yolt (providers): authorize
  Yolt (providers)->>+ASPSP: POST client token grant (basic authentication)
  ASPSP->>-Yolt (providers): access token & refresh token
  Yolt (providers)->>+ASPSP: POST consent creation
  ASPSP->>-Yolt (providers): consent details
  Yolt (providers)->>-PSU (via app): authorization URL
  PSU (via Yolt app)->>+ASPSP: authenticate on consent page
  Yolt (providers)->>+ASPSP: poll for payment status until payment is completed or rejected
```

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 

## Business and technical decisions

**2022-07-07** - As part of ticket https://yolt.atlassian.net/browse/C4PO-9571 we decided to map custom CBI Globe payment statuses
and leave possibility to choose debtor account on consent page. As a consequence we accept the risk that some flows regarding
that process won't be supported by us. We will check how bank behaves on testing process, then we will have to decide to 
leave implementation as it is right now or to make debtor data as mandatory and simplify the flow.

## Sandbox overview

**Payment Flow Additional Information**

|   |   |
|---|---|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?** | execute-on-consent |
| **it is possible to initiate a payment having no debtor account** | NO |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | AcceptedSettlementCompleted |


## External links
* [Current open problems on our end][1]
* [Main reference BIP][2]
* [Berlin Group Standard][3]
* [CBI Globe Portal][4]
* [CBI Globe Technical Documents][5]
* [CBI Globe Account Information Services][6]
* [Cavage HTTP Signatures][7]
 
[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%22Intesa%20Sanpaolo%22%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: <https://yolt.atlassian.net/wiki/spaces/LOV/pages/3902771/BIP+Intesa+SanPaolo+CBI+Globe>
[3]: <https://www.berlin-group.org/>
[4]: <https://cbiglobeopenbankingapiportal.nexi.it/en/cbi-globe/overview>
[5]: <https://cbiglobeopenbankingapiportal.nexi.it/en/technical-documents>
[6]: <https://cbiglobeopenbankingapiportal.nexi.it/en/api/accountInformationServices/2.3.2/accounts/get>
[7]: <https://tools.ietf.org/html/draft-cavage-http-signatures-10>
