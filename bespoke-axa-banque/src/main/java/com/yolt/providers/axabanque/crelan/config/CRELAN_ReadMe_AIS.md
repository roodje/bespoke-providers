## Crelan (AIS)

[Current open problems on our end][1] - //Fill component of particular bank.

## BIP overview

|                                       |                                                  |
|---------------------------------------|--------------------------------------------------|
| **Country of origin**                 | BE                                               | 
| **Site Id**                           | 91b7cdce-36c8-4093-9a25-f8db8e7c994b             |
| **Standard**                          | Berlin Group (NextGenPSD2)                       |
| **Contact**                           | E-mail: psd2@crelan.be                           |
| **Developer Portal**                  | [https://developer.crelan.be/howto][2]           | 
| **Account SubTypes**                  | Current                                          |
| **IP Whitelisting**                   | No                                               |
| **AIS Standard version**              | 6.1 (based on NextGenPsd2 1.3.6)                 |
| **Auto-onboarding**                   | No                                               |
| **Requires PSU IP address**           | No                                               |
| **Type of certificate**               | eidas                                            |
| **Signing algorithms used**           | No signing                                       |
| **Mutual TLS Authentication Support** | Yes                                              |
| **Repository**                        | https://git.yolt.io/providers/bespoke-axa-banque |

## Links - sandbox

|                            |                                                                                  |
|----------------------------|----------------------------------------------------------------------------------|
| **Base URL**               | https://api-sandboc.crelan.be/berlingroup/v1                                     | 
| **Authorization Endpoint** | https://webapi-sandbox.crelan.be/public/berlingroup/authorize/{authorization-id} |
| **Token endpoint**         | https://api-sandbox.crelan.be/berlingroup/v1/token                               |

## Links - production

|                            |                                                                          |
|----------------------------|--------------------------------------------------------------------------|
| **Base URL**               | https://api.crelan.be/berlingroup/v1                                     | 
| **Authorization Endpoint** | https://webapi.crelan.be/public/berlingroup/authorize/{authorization-id} |
| **Token endpoint**         | https://api.crelan.be/berlingroup/v1/token                               |

## Client configuration overview

|                           |                             |
|---------------------------|-----------------------------|
| **Transport key id**      | Eidas transport key id      |
| **Transport certificate** | Eidas transport certificate |

## Registration details

There is no registration.

### Certificate rotation

As there is no registration or signing, rotation isn't needed.

## Connection Overview

Connection is well described on banks [developer portal][3]

## Sandbox overview

Sandbox is available but wasn't used during implementation

## Consent validity rules

For now it is impossible to implement validity rules as consent testing mechanism doesn't support client group. It will
be implemented in C4PO-10148

## Business and technical decisions

## External links

* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20CRELAN%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>

[2]: <https://developer.crelan.be/howto/>

[3]: <https://developer.crelan.be/content/howto/ais-manage-consents>
