# ING FR (PIS)
[Current open problems on our end][1]


## BIP 

|                                              |                                                          |
|----------------------------------------------|--------------------------------------------------        |
| **Country of origin**                        | France                                                   |
| **Site Id**                                  | db66fbcb-2987-4c9c-85c5-ec0a779c621b                     |
| **Standard**                                 | [NextGenPSD2][3]                                         |
| **General Support Contact**                  | Email : https://developer.ing.com                          |
| **Other's Contact**                          |                                                          |
| **Skype Contact**                            |                                                          |
| **PISP Standard version**                    | 4.0.1                                                    |
| **Mutual TLS Authentication support**        | Yes                                                      |
| **Signing algorithms used**                  | rsa-sha256                                               |
| **Requires PSU IP address**                  | No                                                       |
| **Auto Onboarding**                          | None                                                     |
| **IP Whitelisting**                          | Bank is not supporting Whitelisting                      |
| **Type of needed certificate**               | Eidas certificates are required : QWAC and QSEAl         |
| **Repository**                               | https://git.yolt.io/providers/bespoke-ing                |
                                                                                                          
## Links - development                                                                                    
                                                                                                          
|                                              |                                                          |
|----------------------------------------------|--------------------------------------------------        |
| **Developer portal**                         | https://developer.ing.com                                |
| **Sandbox base url**                         | https://api.sandbox.ing.com                              |
| **Sandbox authorization/authentication**     | https://api.sandbox.ing.com/oauth2/token                 |
| **Documentation**                            | https://developer.ing.com/api-marketplace/marketplace    |
                                                                                                          
## Links - production                                                                                     
                                                                                                          
|                                              |                                                          |
|----------------------------------------------|--------------------------------------------------        |
| **Production base url**                      | https://api.ing.com                                      |
| **Production authorization/authentication**  | https://api.ing.com/oauth2/token                         |
| **Production accounts**                      | https://api.ing.com/v2/accounts                          |
                                                                                                          
## Client configuration overview                                                                          
                                                                                                          
|                                              |                                                          |
|----------------------------------------------|--------------------------------------------------        |
| **Signing key id**                           | Eidas signing key id                                     |
| **Signing certificate**                      | Eidas signing certificate                                |
| **Transport key id**                         | Eidas transport key id                                   |
| **Transport certificate**                    | Eidas transport certificate                              |

### Registration details
This bank does not require any registration.

## Certificate rotation 


## Connection Overview 

**Consent validity rules**
ING consent page is an SPA thus we are unable to determine validity rules.

## Business and technical decisions


## Sandbox overview
  
  
## External links
* [Current open problems on our end][1]
* [Developer portal][2]
 
[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20ING_FR%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: <https://developer.ing.com/openbanking/>
[3]: <https://www.berlin-group.org/>
