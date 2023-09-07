## Revolut (PIS)

Revolut Ltd is a British financial technology company headquartered in London, 
United Kingdom that offers banking services. It was founded in 2015.
Revolut has over 12 million customers worldwide.

## BIP overview 

|                                       |                                                                                 |
|---------------------------------------|---------------------------------------------------------------------------------|
| **Country of origin**                 | Headquarter UK / app available worldwide                                        | 
| **Site Id**                           | 3673486a-2ff0-4508-8de1-b8b1fbf213f4	                                           |
| **Standard**                          | [Open Banking Standard][4]                                                      |
| **Contact**                           | E-mail: openbanking-support@revolut.com, Service Desk: api-requests@revolut.com |
| **Developer Portal**                  | [https://developer.revolut.com/portal/signin][2]                                | 
| **Documentation**                     | [https://developer.revolut.com/docs/open-banking-api/][3]                       |
| **IP Whitelisting**                   | No                                                                              |
| **PIS Standard version**              | 3.1.0                                                                           |
| **Auto-onboarding**                   | Yes                                                                             |
| **Requires PSU IP address**           | No                                                                              |
| **Type of certificate**               | OBSEAL / OBWAC                                                                  |
| **Signing algorithms used**           | PS256                                                                           |
| **Mutual TLS Authentication Support** | Yes                                                                             |
| **Repository**                        | https://git.yolt.io/providers/open-banking                                      |

## Links - sandbox

|                            |                                               |
|----------------------------|-----------------------------------------------|
| **Base URL**               | https://sandbox-oba.revolut.com               | 
| **Registration Endpoint**  | https://sandbox-oba.revolut.com/register      |
| **Authorization Endpoint** | https://sandbox-oba.revolut.com/ui/index.html |
| **Token Endpoint**         | https://sandbox-oba-auth.revolut.com/token    |

## Links - production 

|                            |                                       |
|----------------------------|---------------------------------------|
| **Login domains**          | [oba.revolut.com](oba.revolut.com)    | 
| **Base URL**               | https://oba.revolut.com               | 
| **Registration Endpoint**  | https://oba.revolut.com/register      |
| **Authorization Endpoint** | https://oba.revolut.com/ui/index.html |
| **Token Endpoint**         | https://oba-auth.revolut.com/token    | 

## Client configuration overview

|                                        |                                                                         |
|----------------------------------------|-------------------------------------------------------------------------|
| **Institution id**                     | Unique identifier of the financial institution assigned by Open Banking | 
| **Client id**                          | Unique identifier received during registration process                  |
| **Signing key id**                     | OBSEAL key id                                                           | 
| **Signing key header id**              | OBSEAL key id                                                           | 
| **Transport key id**                   | OBWAC key id                                                            |
| **Transport certificate**              | OBWAC certificate                                                       |
| **Transport certificate subject name** | OBWAC certificate subject obtained using openssl tool                   |
| **Software Id**                        | Id of Software Statement Assertion                                      |
| **Software Statement Assertion**       | SSA required during dynamic registration - tls_client_auth method       |
| **Organization Id**                    | TPP's organization id                                                   |

## Registration details

Revolut allows to [add][5] new app with dynamic registration, [update][6] it details and [delete][7] it.
All the above is well described in theirs [documentation][3].

Important note regarding Transport certificate subject name - this authentication mean is used as a value for
jwt claim tls_client_auth_dn in registration payload. Revolut expects this value to be in specific format.
Current implementation of BouncyCastle doesn't produce correct results for eIDAS certificates.
Even though we do not use eIDAS certificates in Revolut GB, we decided to unify the way of retrieving this value
across Revolut group. To obtain correct value we need to use following command on transport certificate:
```shell
openssl x509 -in /path/to/your/cert.pem -inform pem -noout -subject -nameopt RFC2253
```
Result of this command is prefixed with `subject=` which needs to be removed before saving as authentication mean.
For more details please consult this section of [documentation][8]

## Connection Overview

Revolut follows Open Banking 3.1.0 standard. It means that flow is similar to other banks. Due to that fact,
Open Banking DTOs are used in implementation, and code relay mostly on our generic Open Banking implementation.

## Sandbox overview

Sandbox is described in [documentation][3] but was never actively used by us when working with this bank connection.
  
## Business and technical decisions

## External links
* [Current open problems on our end][1]
* [Developer portal][2]
* [Documentation][3]
* [Open Banking Standard][4]


[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20REVOLUT%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: <https://developer.revolut.com/portal/signin>
[3]: <https://developer.revolut.com/docs/open-banking-api/>
[4]: <https://standards.openbanking.org.uk/>
[5]: <https://developer.revolut.com/docs/build-banking-apps/#identification-and-authentication-identification-and-authentication-dynamic-client-registration-registration-request>
[6]: <https://developer.revolut.com/docs/build-banking-apps/#identification-and-authentication-identification-and-authentication-dynamic-client-registration-updating-a-client>
[7]: <https://developer.revolut.com/docs/build-banking-apps/#identification-and-authentication-identification-and-authentication-dynamic-client-registration-deleting-a-client>
