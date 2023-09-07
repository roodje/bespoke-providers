## Bank Van Breda (AIS)
[Current open problems on our end][1]


## BIP

|                                              |                                              |
|----------------------------------------------|----------------------------------------------|
| **Country of origin**                        | Belgium                                      |
| **Site Id**                                  | 70eb88bd-7fae-4bff-b80b-88099538f292         |
| **Standard**                                 | Berlin group                                 |
| **General Support Contact**                  | Email : xs2a-devportal@bankvanbreda.be       |
| **Other's Contact**                          |                                              |
| **Skype Contact**                            |                                              |
| **AIS Standard version**                     | 1.3                                          |
| **Mutual TLS Authentication support**        | Yes                                          |
| **Signing algorithms used**                  | no signing                                   |
| **Account types**                            | Current                                      |
| **Requires PSU IP address**                  | No                                           |
| **Auto Onboarding**                          | None                                         |
| **IP Whitelisting**                          | Bank is not supporting Whitelisting          |
| **Type of needed certificate**               | Eidas certificate are required : QWAC        |
| **Repository**                               | https://git.yolt.io/providers/monorepo-group |

## Links - development
PRD:
API: https://xs2a-api.bankvanbreda.be
AUTH: https://xs2a-api-web.bankvanbreda.be
SANDBOX:
API: https://xs2a-sandbox.bankvanbreda.be
AUTH: https://xs2a-sandbox-web.bankvanbreda.be

|                                              |                                                        |
|----------------------------------------------|--------------------------------------------------------|
| **Developer portal**                         | https://xs2a-devportal.bankvanbreda.be/                |
| **Sandbox base url**                         | https://xs2a-sandbox.bankvanbreda.be                   |
| **Sandbox authorization/authentication**     | https://xs2a-sandbox-web.bankvanbreda.be               |
| **Documentation**                            | https://xs2a-devportal.bankvanbreda.be/apis-sbx        |

## Links - production

|                                              |                                                          |
|----------------------------------------------|----------------------------------------------------------|
| **Production base url**                      | https://xs2a-api.bankvanbreda.be                         |
| **Production authorization/authentication**  | https://xs2a-api-web.bankvanbreda.be                     |
| **Production accounts**                      | https://xs2a-api.bankvanbreda.be/berlingroup/v1/accounts |

## Client configuration overview

|                           |                                                                                                                                       |
|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| **Transport key id**      | Eidas transport key id                                                                                                                |
| **Transport certificate** | Eidas transport certificate                                                                                                           |
| **TPP ID**                | TPP ID / NCA ID, can found in certificate in Subject field of Eidas certificate (for example QWAC). For example: PSDNL-SBX-1234512345 |

### Registration details
This bank does not require any registration.

## Certificate rotation


## Connection Overview


## Business and technical decisions

## Sandbox overview
Sandbox is working very well and can be accessed using provided postman collection together with test certificates from /postman/bankvanbreda folder
Take notice, that even if /accounts endpoint return more than one account, transactions and balances can bve retrieved only for one of them, all other accountIds result in error. 

## User Site deletion
Consent deletion is available.

## External links
* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20BANK_VAN_BREDA%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status/>
