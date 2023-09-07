## UniCredit IT (AIS)

[Current open problems on our end][1]

UniCredit IT is an Italian bank which belongs to UniCredit Group.
UniCredit Group PSD2 API is implemented based on Berlin Group Implementation Guidelines 1.3.4
 
## BIP overview 
[UniCredit IT reference BIP][2]

|                             |                                                 |
|-----------------------------|-------------------------------------------------|
| **Country of origin**       | Italy                                           | 
| **Site Id**                 | eaeeadc1-4d27-4ae0-a9eb-e34ec0dbecc5            |
| **Standard**                | [Berlin Group][3]                               |
| **AIS Standard version**    | 1.3.4                                           |
| **PISP Standard version**   | 1.3.4                                           |
| **Account types**           | CURRENT_ACCOUNT, CREDIT_CARD                    |
| **Requires PSU IP address** | Always for request initiated directly by PSU    |
| **Consent flow type**       | Global                                          |
| **Repository**              | https://git.yolt.io/providers/bespoke-unicredit |

## Links - production 
|                    |                   |
|--------------------|-------------------|
| **Login domains**  | www.unicredit.it  |

## Client configuration overview
|                           |                             |
|---------------------------|-----------------------------|
| **Transport key id**      | eIDAS transport key id      |
| **Transport certificate** | eIDAS transport certificate |

### Registration details
Client registration is needed only once per single eIDAS for all the banks from UniCredit Group.
It means that when we have been successfully onboarded for our very first UniCredit Group bank, 
we are already onboarded for the other banks from UniCredit Group which are implemented based on Berlin Group standards.
There are no client credentials used for communication with bank's API - client is authenticated based on eIDAS QWAC cert in mTLS.

### Certificate rotation
According to the UniCredit's API documentation there is no possibility to register more than one eIDAS for the same company, from doc:
"It is possible to onboard once with a single eIDAS certificate. Other onboarding calls will be rejected."
However, in API CATALOG in Dev Portal, there is a section "TPP MANAGEMENT" with "TPP Offboarding" endpoint 
documentation available - perhaps it may be used to revoke the old eIDAS and let us re-onboard using the new one.

## Connection Overview
```mermaid
sequenceDiagram
  autonumber
  participant PSU (via Yolt app);
  participant Yolt (providers);
  participant ASPSP;
  PSU (via Yolt app)->>+Yolt (providers): getLoginInfo()
  Yolt (providers)->>+ASPSP: calls /v1/consents
  ASPSP->>-Yolt (providers): {consentId:"1234",_links.scaRedirect.href:"https://asd.com"}
  Yolt (providers)->>-PSU (via Yolt app): authorization URL
  PSU (via Yolt app)->>+ASPSP: authenticates via web/app
  ASPSP->>-PSU (via Yolt app): redirect to Yolt
  activate Yolt (providers)
  PSU (via Yolt app)->>+Yolt (providers): fetchData()
  loop Various data fetches
  Yolt (providers)->>+ASPSP: /accounts, /transactions, /balances
  ASPSP->>-Yolt (providers): accounts, balances, transactions
  end
  Yolt (providers)-->>PSU (via Yolt app): user data
  deactivate Yolt (providers)
```

**Consent validity rules**

Consent validity rules has been implemented for UniCredit IT PIS.
Consent validity rules are set to EMPTY_RULES_SET for UniCredit IT AIS bank due to error page shares same HTML code as a correct one.

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 

## Business decisions
--empty--

## Sandbox overview
--empty--

**Payment Flow Additional Information**

|                                                                                                        |                             |
|--------------------------------------------------------------------------------------------------------|-----------------------------|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?**                    | execute-on-consent          |
| **it is possible to initiate a payment having no debtor account**                                      | NO                          |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | AcceptedSettlementCompleted |

## External links
* [Current open problems on our end][1]
* [Berlin Group][3]
* [Developer Portal][4]
 
[1]: <https://yolt.atlassian.net/browse/C4PO-2851?jql=project%20%3D%20%22C4PO%22%20AND%20text%20~%22unicredit%22%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: <https://yolt.atlassian.net/wiki/spaces/LOV/pages/3902154/BIP+UniCredit>
[3]: <https://www.berlin-group.org/>
[4]: <https://developer.unicredit.eu/>
