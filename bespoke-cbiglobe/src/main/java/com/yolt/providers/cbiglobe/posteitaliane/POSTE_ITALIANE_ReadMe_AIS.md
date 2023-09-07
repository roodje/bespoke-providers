## Poste Italiane (AIS)
[Current open problems on our end][1]

## BIP overview 
[Main reference BIP][2]

|                                       |                                                |
|---------------------------------------|------------------------------------------------|
| **Country of origin**                 | Italy                                          | 
| **Site Id**                           | 3b9c876d-1d62-4a5b-9046-163757129238           |
| **Standard**                          | [Berlin Group Standard][3]                     |
| **Contact**                           | Email: cbiglobe@cbi-org.eu                     |
| **Developer Portal**                  | https://cbiglobeopenbankingapiportal.nexi.it/  | 
| **Account SubTypes**                  | CURRENT_ACCOUNT                                |
| **IP Whitelisting**                   | No                                             |
| **AIS Standard version**              | 1.1                                            |
| **Auto-onboarding**                   | No                                             |
| **Requires PSU IP address**           | Yes                                            |
| **Type of certificate**               | eIDAS                                          |
| **Signing algorithms used**           | rsa-sha512                                     |
| **Mutual TLS Authentication Support** | Yes                                            |
| **Repository**                        | https://git.yolt.io/providers/bespoke-cbiglobe |

## ASPSP list
| Code  | Name                                    |
|-------|-----------------------------------------|
| 07601 | **Poste Italiane S.p.A. - Banco Posta** |

## Links - production
|                  |                                                                                            |
|------------------|--------------------------------------------------------------------------------------------|
| **login domain** | [securelogin.poste.it](securelogin.poste.it) <br> [idp-poste.poste.it](idp-poste.poste.it) |
| **Base URL**     | https://cbiglobeopenbankingapigateway.nexi.it                                              |

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
This bank only supports manual registration which takes place through the [CBI Globe Portal][4].
Registration gives access to all banks from the CBI Globe group.
You can only register once for a specific email address.
During registration, the eIDAS transport certificate must be uploaded. 
After registration, credentials such as **client id** and **client secret** will be displayed on the portal.

### Certificate rotation
CBI Globe gives possibility to update the QWAC eIDAS certificate on their portal.
After updating certificate, the credentials does not change (client id and client secret).

## Connection Overview
The swagger can be downloaded from here: [Technical Documents][5] (corrupted one).
The connection requires mutual TLS with QWAC eIDAS certificate.
The signature of the request is required and it based on [Cavage HTTP Signatures][7].

**Consent validity rules**

Consent validity rules has been implemented for Poste Italiane PIS.

### Simplified flow chart
```mermaid
graph TD
  A{Has single ASPSP?}
  A-->|Yes|B[Establish first consent]
  A-->|No|C[Display form with ASPSP selection]
  C-->B
  B-->D[Fetch and cache accounts]
  D-->E[Establish second consent]
  E-->F[Create access means]
  F-->G[Fetch data]
```

### Simplified sequence diagram
```mermaid
sequenceDiagram
  autonumber
  participant PSU (via Yolt app);
  participant Yolt (providers);
  participant ASPSP;
  PSU (via Yolt app)->>+Yolt (providers): authorize (first consent page)
  Yolt (providers)->>+ASPSP: POST client token grant (basic authentication)
  ASPSP->>-Yolt (providers): access token & refresh token
  Yolt (providers)->>+ASPSP: POST consent creation
  ASPSP->>-Yolt (providers): consent details
  Yolt (providers)->>-PSU (via Yolt app): authorization URL
  PSU (via Yolt app)->>+ASPSP: authenticate on consent page
  ASPSP->>-PSU (via Yolt app): redirect to Yolt with authorization code
  PSU (via Yolt app)->>+Yolt (providers): authorize (second consent page)
  Yolt (providers)->>+ASPSP: GET consent status
  ASPSP->>-Yolt (providers): consent status
  Yolt (providers)->>+ASPSP: GET accounts
  ASPSP->>-Yolt (providers): accounts list
  Yolt (providers)->>+ASPSP: POST consent creation
  ASPSP->>-Yolt (providers): consent details
  Yolt (providers)->>-PSU (via Yolt app): authorization URL
  PSU (via Yolt app)->>+ASPSP: authenticate on consent page
  ASPSP->>-PSU (via Yolt app): redirect to Yolt with authorization code
  PSU (via Yolt app)->>+Yolt (providers): fetch data
  loop Various data fetches
  Yolt (providers)->>+ASPSP: GET balances and transactions
  ASPSP->>-Yolt (providers): balances and transactions list
  end
  Yolt (providers)-->>PSU (via Yolt app): user data
```

### Balances
According to documentation (specific annex), balances supported for current account by Poste Italiane:
* closingBooked for accounted balance 
* authorised for available balance

| Balance Type   | Bank explanation                                                                                                                                                                                                                                                                                                                                                                                   |
|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| closingBooked  | Field “lastChangeDateTime” enhanced with the date of balances updating which coincides with sysdate for current account in case of open service, instead will coincides with the date of last updating during the night closing. Regarding the cards, it will always coincides with the last update, unless there were closures. In the case of Corporates the psu-id-type field is not mandatory. |

### Consent Validation Rules

Consent validity rules are set. There is one time link used so after usage link becomes invalid.

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 

## Business and technical decisions
* In the case of mapping balances, a business decision was made to filter the balances against the assigned currency to the account 
and if there is one balance then it should be used to map both the **AvailableBalance** and **CurrentBalance** fields. 
Otherwise, the selection of the balance is to be determined by its type in accordance with the following priority
(The first element has the highest priority):
  * for **AvailableBalance**: EXPECTED, INTERIM_AVAILABLE, AVAILABLE, FORWARD_AVAILABLE, AUTHORISED
  * for **CurrentBalance**: INTERIM_BOOKED, CLOSING_BOOKED, OPENING_BOOKED, EXPECTED
  
* Due to flow which requires to pass two consent pages to fetch all data, the implementation of the API getting cards has been abandoned. 
That's because it would force to pass four consents pages to go through to get all the data (**CurrentAccounts** and **CardAccounts**).

* 04.08.2022 We have found possibility to get more than only last 89 days of transactions, but it will require probably
  additional consent. As bank is not used for now, and we don't know clients preferences, we decided with PO not to do this.
  Everything was described in comment https://yolt.atlassian.net/browse/C4PO-10544?focusedCommentId=94853.

## Sandbox overview
Sandbox is completely different from the production environment in terms of communication, data transfer and overall flow. 
Therefore, its use is not recommended.

## External links
* [Current open problems on our end][1]
* [Main reference BIP][2]
* [Berlin Group Standard][3]
* [CBI Globe Portal][4]
* [CBI Globe Technical Documents][5]
* [CBI Globe Account Information Services][6]
* [Cavage HTTP Signatures][7]
 
[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%22Poste%20Italiane%22%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: <https://yolt.atlassian.net/wiki/spaces/LOV/pages/3903639/BIP+Poste+Italiane+CBI+GLOBE>
[3]: <https://www.berlin-group.org/>
[4]: <https://cbiglobeopenbankingapiportal.nexi.it/en/cbi-globe/overview>
[5]: <https://cbiglobeopenbankingapiportal.nexi.it/en/technical-documents>
[6]: <https://cbiglobeopenbankingapiportal.nexi.it/en/api/accountInformationServices/2.3.2/accounts/get>
[7]: <https://tools.ietf.org/html/draft-cavage-http-signatures-10>
