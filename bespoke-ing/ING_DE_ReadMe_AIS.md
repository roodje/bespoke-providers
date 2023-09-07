# ING DE (AIS)
[Current open problems on our end][1]


## BIP 

|                                                |                                                       |
|------------------------------------------------|-------------------------------------------------------|
| **Country of origin**                          | Germany                                               |
| **Site Id**                                    | a24ae58c-8c1a-4ee5-8feb-c5f314c12881                  |
| **Standard**                                   | [NextGenPSD2][3]                                      |
| **General Support Contact**                    | Email : https://developer.ing.com                       |
| **Other's Contact**                            |                                                       |
| **Skype Contact**                              |                                                       |
| **AIS Standard version**                       | 2.5.0                                                 |
| **Mutual TLS Authentication support**          | Yes                                                   |
| **Signing algorithms used**                    | rsa-sha256                                            |
| **Account types**                              | Current                                               |
| **Requires PSU IP address**                    | No                                                    |
| **Auto Onboarding**                            | None                                                  |
| **IP Whitelisting**                            | Bank is not supporting Whitelisting                   |
| **Type of needed certificate**                 | Eidas certificates are required : QWAC and QSEAl      |
| **Repository**                                 | https://git.yolt.io/providers/bespoke-ing             |
                                                 
## Links - development
                           
|                                                |                                                       |
|------------------------------------------------|-------------------------------------------------------|                                                                                          
| **Developer portal**                           | https://developer.ing.com                             |
| **Sandbox base url**                           | https://api.sandbox.ing.com                           | 
| **Sandbox authorization/authentication**       | https://api.sandbox.ing.com/oauth2/token              |
| **Documentation**                              | https://developer.ing.com/api-marketplace/marketplace |
                                                 
## Links - production
                            
|                                                |                                                      |
|------------------------------------------------|------------------------------------------------------|                                                  
| **Production base url**                        | https://api.ing.com                                  |
| **Production authorization/authentication**    | https://api.ing.com/oauth2/token                     |
| **Production accounts**                        | https://api.ing.com/v2/accounts                      |
                                                 
## Client configuration overview
                 
|                                                |                                                      |
|------------------------------------------------|------------------------------------------------------|                                                                                           
| **Signing key id**                             | Eidas signing key id                                 | 
| **Signing certificate**                        | Eidas signing certificate                            | 
| **Transport key id**                           | Eidas transport key id                               |
| **Transport certificate**                      | Eidas transport certificate                          |

### Registration details
This bank does not require any registration.

## Certificate rotation 


## Connection Overview 

**Consent validity rules** are set to EMPTY_RULES_SET for AIS ING_DE bank due to error page shares 
same HTML code as a correct one.

## Business and technical decisions

### \<br\> markup change into \\n

On **17.04.2020** was business decision made by Roderick Simons to change markup `<br>` to `\n` 
in `remittanceInformationUnstructured` field ([Slack discusion][4], [Jira task][5]) e.g.:
 
```json5
"remittanceInformationUnstructured": "DEKAMARKT<br>Pasvolgnr: 001 28-08-2020 14:35<br>Transactie: 50R321 Term: GXNCCC<br>Valutadatum: 29-08-2020"
```
change to 
```json5
"remittanceInformationUnstructured": "DEKAMARKT\nPasvolgnr: 001 28-08-2020 14:35\nTransactie: 50R321 Term: GXNCCC\nValutadatum: 29-08-2020"
```

On **07.01.2021** business decision was made to add remittanceInformationStructured reference value to `ExtendedTransactionDTO` [C4PO-7130]

During tasks for ING DE https://yolt.atlassian.net/browse/C4PO-9070 https://yolt.atlassian.net/browse/C4PO-8998 there is a need of extraction a transaction description.
This is passed as unstructured text like `mandatereference:,creditorid:,remittanceinformation:NR XXXX`. Description has been extracted from remitannceinformation.
## Sandbox overview

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 
  
## External links
* [Current open problems on our end][1]
* [Developer portal][2]
 
[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20ING_DE%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: <https://developer.ing.com/openbanking/>
[3]: <https://www.berlin-group.org/>
[4]: <https://lovebirdteam.slack.com/archives/C3DKLAG6Q/p1587119604118000?thread_ts=1586791799.095100&cid=C3DKLAG6Q>
[5]: <https://yolt.atlassian.net/browse/C4PO-3729>
