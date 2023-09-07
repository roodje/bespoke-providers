## Consors Bank! (AIS)
[Current open problems on our end][1] 

## BIP overview 

|                                       |                                                       |
|---------------------------------------|-------------------------------------------------------|
| **Country of origin**                 | DE                                                    | 
| **Site Id**                           | c096d351-13f9-48df-8e56-f27d0b3b309                   |
| **Standard**                          | Berlin                                                |
| **Contact**                           | E-mail: xs2a@consorsbank.de                           |
| **Developer Portal**                  | https://www.consorsbank.de/ev/Service-Beratung/xs2a#2 | 
| **Account SubTypes**                  | Current, Savings                                      |
| **IP Whitelisting**                   | No                                                    |
| **AIS Standard version**              | 1.3                                                   |
| **Auto-onboarding**                   | No                                                    |
| **Requires PSU IP address**           | Only 4 calls a day without PSU IP addr                |
| **Type of certificate**               | eIDAS                                                 |
| **Signing algorithms used**           |                                                       |
| **Mutual TLS Authentication Support** | Yes                                                   |
| **Repository**                        | https://git.yolt.io/providers/bespoke-consorsbank     |

## Links - sandbox

|               |                                    |
|---------------|------------------------------------|
| **Base URL**  | https://xs2a-sndbx.consorsbank.de  | 

## Links - production 

|               |                             |
|---------------|-----------------------------|
| **Base URL**  | https://xs2a.consorsbank.de | 

## Client configuration overview


|                                  |     |
|----------------------------------|-----|
| **Signing key id**               |     | 
| **Signing certificate**          |     | 
| **Transport key id**             |     |
| **Transport certificate**        |     |
| **Certificate Agreement Number** |     |
| **Client id**                    |     | 

## Registration details
Bank doesn't require registration

## Connection Overview

## Connection Overview

**Consent validity rules**
ConsorsBank AIS provider return the same consent HTML for successful an failed consents, thus we are unable to determine 
consent validity rules for AIS.

## Sandbox overview

## User Site deletion
There's `onUserSiteDelete` method implemented by this provider, however, only in a best effort manner.

## Business and technical decisions
  
## External links
* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status%20%3D%20CONSORS%20BANK>
