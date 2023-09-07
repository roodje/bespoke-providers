## Postbank (AIS)
[Current open problems on our end][1]

## BIP overview 
[Main reference BIP][2]

|                                       |                                                                          |
|---------------------------------------|--------------------------------------------------------------------------|
| **Country of origin**                 | Germany                                                                  | 
| **Site Id**                           | db53d782-3168-48fa-88cf-d3378b3fbf22                                     |
| **Standard**                          | [Berlin Group Standard][3]                                               |
| **Form of Contact**                   | Email - xs2a.api@db.com                                                  |
| **Base URL**                          | **Sandbox & Production** - https://xs2a.db.com/                          |
| **Signing algorithms used**           | No                                                                       |
| **Mutual TLS Authentication Support** | Yes                                                                      |
| **IP Whitelisting**                   | No                                                                       |
| **Auto-onboarding**                   | Unsupported                                                              |
| **AIS Standard version**              | 1.3                                                                      |
| **PISP Standard version**             | 1.3 (not implemented in providers)                                       |
| **Account types**                     | CURRENT_ACCOUNT                                                          |
| **Requires PSU IP address**           | Yes                                                                      |
| **PSU-ID format**                     | Username which is self-defined or unique 2 to 59 alphanumeric characters |
| **PSU-ID-Type**                       | DE_ONLB_POBA                                                             |
| **Business Entity**                   | Postbank                                                                 |
| **Consent Type**                      | Global                                                                   |
| **Supported Flow**                    | Embedded                                                                 |
| **Repository**                        | https://git.yolt.io/providers/bespoke-deutsche-bank                      |

## Client configuration overview
|                           |                                    |
|---------------------------|------------------------------------|
| **Transport key id**      | Eidas transport key id             |
| **Transport certificate** | Eidas transport certificate (QWAC) |

### Registration details
This bank do not support Dynamic Client Registration.

### Certificate rotation
Bank is qualifying TPP based on registration number that is present in eIDAS certificate.
It means that we can swap eIDAS certificates with the same registration number without any side effects.

## Connection Overview
**TODO/ON HOLD**

**Consent validity rules**
Postbank AIS uses dynamic flow, thus we are unable to determine consent validity rules for AIS.

### Simplified sequence diagram:
**TODO/ON HOLD**

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 

## Business and technical decisions
**TODO/ON HOLD**
* Postbank uses embedded flow which means that Strong Customer Authentication (SCA) must be enforced on our side. 
The type of SCA that is supported by Postbank requires some changes on the side of our mobile application (implementation of flickering code) and on the backend side (implementation of polling to verify consent status).

* Currently business decided to put this bank on HOLD.

* The bank doesn't send **bookingDate** and **valueDate** fields for _pending_ transactions. Due to the fact that **dateTime**
field is required in our model we have to get information about transaction date from **transactionId**.
In this bank transaction ids have following formats, so proper logic for this was implemented:

|             | Booking                                     | Pending                                                                    |
|-------------|---------------------------------------------|----------------------------------------------------------------------------|
| **Format**  | 000001-TRADE_KEY + PRODUCT + TRANSACTION_NO | 000001- BRANCH_NO_MAIN + BUSI_CONT_MAIN_KEY + AUTHORIZATION_ID + ENTRY_ TS |
| **Example** | 000001-C760D89F93398EF4490138B5FT0037224    | 000001-330DA1CD87C588578AD4906D5C1E11127151742020-11-12 12:05:17.4263780   |

We also received information that those formats are only suggestions. This is their internal field, so we should not
relay on its value. We found example that following value was returned _000001-714881591_. It means that we don't have
any information about transaction data. Leon made a decision to use actual fetching data time instead. This was consulted
with _accounts-and-transactions owners.

## Sandbox overview
No such information. Sandbox integration has been omitted.

**Payment Flow Additional Information**

|                                                                                                        |                                                                                                        |
|--------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?**                    | no oAuth/after final status payment                                                                    |
| **it is possible to initiate a payment having no debtor account**                                      | NO                                                                                                     |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | No, once the payment is successfully initiated, payment may still not be credited in creditor account. |

## External links
* [Current open problems on our end][1]
* [Main reference BIP][2]
* [Berlin Group Standard][3]
 
[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%22Postbank%22%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: <https://yolt.atlassian.net/wiki/spaces/LOV/pages/3899695/BIP+Deutsche+Bank+and+Postbank>
[3]: <https://www.berlin-group.org/>
