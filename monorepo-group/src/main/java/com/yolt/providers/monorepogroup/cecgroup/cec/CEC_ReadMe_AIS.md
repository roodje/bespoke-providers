## CEC (AIS)

[Current open problems on our end][1]

## BIP

|                                       |                                                  |
|---------------------------------------|--------------------------------------------------|
| **Country of origin**                 | Romania                                          |
| **Site Id**                           | 7eb47e4e-91a0-4737-a74b-b68fc2b679fa             |
| **Standard**                          | [NextGenPSD2][3]                                 |
| **General Support Contact**           | Email : psd2cec@cec.ro                           |
| **Other's Contact**                   |                                                  |
| **Skype Contact**                     |                                                  |
| **AIS Standard version**              | 1.0.0                                            |
| **Mutual TLS Authentication support** | Yes                                              |
| **Signing algorithms used**           | rsa-sha256                                       |
| **Account types**                     | Current                                          |
| **Requires PSU IP address**           | Yes                                              |
| **Auto Onboarding**                   | No                                               |
| **IP Whitelisting**                   | Bank is not supporting Whitelisting              |
| **Type of needed certificate**        | Eidas certificates are required : QWAC and QSEAl |
| **Repository**                        | https://git.yolt.io/providers/monorepo-group     |

## Links - development

|                                          |                                         |
|------------------------------------------|-----------------------------------------|
| **Developer portal**                     | https://apiportal.cec.ro/cec/prod       |
| **Sandbox base url**                     |                                         |
| **Sandbox authorization/authentication** |                                         |
| **Documentation**                        | https://apiportal.cec.ro/cec/prod/start |

## Links - production

|                                             |                                              |
|---------------------------------------------|----------------------------------------------|
| **Production base url**                     | https://api.cec.ro/cec/prod                  |
| **Production authorization/authentication** | https://api.cec.ro/cec/prod/oauth2/authorize |
| **Production accounts**                     | https://api.cec.ro/cec/prod/v1/accounts      |

## Client configuration overview

|                           |                                  |
|---------------------------|----------------------------------|
| **Signing key id**        | Eidas signing key id             |
| **Signing certificate**   | Eidas signing certificate        |
| **Transport key id**      | Eidas transport key id           |
| **Transport certificate** | Eidas transport certificate      |
| **Client Id**             | Client ID of the application     |
| **Client Secret**         | Client Secret of the application |

### Registration details

One can register on the developer portal

## Certificate rotation

## Connection Overview

## Business and technical decisions

## Sandbox overview

## Consent validity rules

Consent testing was turned on, the page is simple HTML so consent validity rules should work just fine.

## User Site deletion

## External links

* [Current open problems on our end][1]
* [Developer portal][2]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20CEC%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status/>

[2]: <https://apiportal.cec.ro/cec/prod>

[3]: <https://www.berlin-group.org/>
