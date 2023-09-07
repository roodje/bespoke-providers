# ING NL (PIS)
[Current open problems on our end][1]


## BIP 

|                                              |                                                                                                                                            |
|----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
|                                              |                                                                                                                                            |
| **Country of origin**                        | Netherlands                                                                                                                                |
| **Site Id**                                  | 2967f2c0-f0e6-4f1f-aeba-e4357b82ca7a                                                                                                       |
| **Standard**                                 | [NextGenPSD2][3]                                                                                                                           |
| **General Support Contact**                  | Email : https://developer.ing.com                                                                                                            |
| **PISP Standard version**                    | 4.0.1                                                                                                                                      |
| **Mutual TLS Authentication support**        | Yes                                                                                                                                        |
| **Signing algorithms used**                  | rsa-sha256                                                                                                                                 |
| **Requires PSU IP address**                  | No                                                                                                                                         |
| **Auto Onboarding**                          | None                                                                                                                                       |
| **IP Whitelisting**                          | Bank is not supporting Whitelisting                                                                                                        |
| **Type of needed certificate**               | Eidas certificates are required : QWAC and QSEAl                                                                                           |
| **Repository**                               | https://git.yolt.io/providers/bespoke-ing                                                                                                  |
                                               
## Links - development                         
                                               
|                                              |                                                            |
|----------------------------------------------|------------------------------------------------------------|
| **Developer portal**                         | https://developer.ing.com                                  |
| **Sandbox base url**                         | https://api.sandbox.ing.com                                |
| **Sandbox authorization/authentication**     | https://api.sandbox.ing.com/oauth2/token                   |
| **Documentation**                            | https://developer.ing.com/api-marketplace/marketplace      |
                                                                                                            
## Links - production                                                                                       
                                                                                                            
|                                              |                                                            |
|----------------------------------------------|------------------------------------------------------------|
| **Production base url**                      | https://api.ing.com                                        |
| **Production authorization/authentication**  | https://api.ing.com/oauth2/token                           |
| **Production accounts**                      | https://api.ing.com/v2/accounts                            |
                                                                                                            
## Client configuration overview                                                                            
                                                                                                            
|                                              |                                                            |
|----------------------------------------------|------------------------------------------------------------|
| **Signing key id**                           | Eidas signing key id                                       |
| **Signing certificate**                      | Eidas signing certificate                                  |
| **Transport key id**                         | Eidas transport key id                                     |
| **Transport certificate**                    | Eidas transport certificate                                |

### Registration details
This bank does not require any registration.

## Certificate rotation 


## Connection Overview 

**Consent validity rules**
ING consent page is an SPA thus we are unable to determine validity rules.
 

## Business and technical decisions

**09.06.2020**
There was a discussion regarding what timezones should we use to present dates returned by the bank.
The conclusion was to use bank headquarters timezone unless date is provided with an offset.


##### 30.07.2020 ING PIS:
Payment has been successfully tested (29.07.2020), so we switched LIVE, but we do not have any clients on YFB-EXT-PRD, so it stays behind feature toggle.

## Sandbox overview

**Payment Flow Additional Information**

|   |   |
|---|---|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?** | execute-on-consent |
| **it is possible to initiate a payment having no debtor account** | YES |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | AcceptedSettlementCompleted |
  
## External links
* [Current open problems on our end][1]
* [Developer portal][2]
 
[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20ING_NL%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: <https://developer.ing.com/openbanking/>
[3]: <https://www.berlin-group.org/>
