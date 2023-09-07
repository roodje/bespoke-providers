## Allianz Banque (AIS)
[Current open problems on our end][1]

## BIP overview

|                                       |                                           |
|---------------------------------------|-------------------------------------------|
| **Country of origin**                 | France                                    | 
| **Site Id**                           | 0ac9244f-811b-446c-b7c1-f46473887908      |
| **Standard**                          | STET                                      |
| **Form of Contact**                   | Email - developerportal@arkea.com         |
| **Developer Portal**                  | https://developer.arkea.com/              |
| **Signing algorithms used**           | SHA 256 with RSA                          |
| **Mutual TLS Authentication Support** | Yes                                       |
| **IP Whitelisting**                   | Yes                                       |
| **Auto-onboarding**                   | No support                                |
| **Type of certificate**               | eIDAS (QWAC, QSEAL) or OB trans / OB sign |
| **AIS Standard version**              | 1.4.2                                     |
| **Account types**                     | CURRENT_ACCOUNT, CREDIT_CARD              |
| **Requires PSU IP address**           | YES                                       |
| **Repository**                        | https://git.yolt.io/providers/stet        |

## Links - production
|                     |                                                                      |
|---------------------|----------------------------------------------------------------------|
| **Login domains**   | [api-openbanking.allianzbanque.fr](api-openbanking.allianzbanque.fr) | 

## Links - sandbox
|                     |                                                              |
|---------------------|--------------------------------------------------------------|
| **Login domains**   | [api-sandbox.allianzbanque.fr](api-sandbox.allianzbanque.fr) | 

## Registration details
The bank requires manual registration , one should log to the developer portal https://developer.arkea.com/ 
( credentials are standard ) and create a new app. Creation of the app requires pasting the X509 Certificate Chain.

### Multiple Registration
When registered in the Developer Portal, one can create multiple apps with various certificates.It is also possible 
to create multiple accounts with different addresses.

### Certificate rotation
When rotating certificates we need to change authmeans on our side (in APY) and simultaneously perform a change 
in the developer portal to avoid downtime.

## Connection Overview
The bank uses CavageSignatureStrategy for signing the requests, which uses the
following fields in this order '(request-target)', 'Digest' and  'X-Request-Id'and PSU-IP address.
The provider uses [RFC 7616][2] for calculating the digest with SHA-256 algorithm.

## Client configuration overview
|                           |                                                                       |
|---------------------------|-----------------------------------------------------------------------|
| **Signing key id**        | Eidas signing key id                                                  | 
| **Signing certificate**   | Eidas signing certificate                                             | 
| **Transport key id**      | Eidas transport key id                                                |
| **Transport certificate** | Eidas transport certificate                                           |
| **Client id**             | Unique APIKey obtained during manual registration in Developer Portal | 

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method.

## Business Decisions

Extended transaction history is not yet supported by CM Arkea, i.e. history including transactions older than 90 days.
Therefore we decided to narrow fetching time as a safe way to get data.

According to the documentation: Only Accounting Balance (CLBD) and Instant Balance (XPCD) are reported in the balances
endpoint.
CurrentBalance = XPCD
AvailableBalance = CLBD

C4PO-9794
We decided to filter pending transactions, because we had an issue with empty bookingDate.
And these transactions are filtered later by core team.

As discussed in https://git.yolt.io/providers/stet/-/merge_requests/120#note_739459 there is a decision to map valueDate.

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%ALLIANZ_BANQUE%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[7]: <https://tools.ietf.org/html/rfc7616>
