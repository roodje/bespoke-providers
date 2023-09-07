## Libra Bank (AIS)
[Current open problems on our end][1]


## BIP

|                                              |                                              |
|----------------------------------------------|----------------------------------------------|
| **Country of origin**                        | Romania                                      |
| **Site Id**                                  | 43ebaaf4-a7f0-49b4-a9bd-e879fccbb326         |
| **Standard**                                 | Berlin group                                 |
| **General Support Contact**                  | Email : apibanking@librabank.ro              |
| **Other's Contact**                          |                                              |
| **Skype Contact**                            |                                              |
| **AIS Standard version**                     | 1                                            |
| **Mutual TLS Authentication support**        | No                                           |
| **Signing algorithms used**                  | SHA256 or SHA512                             |
| **Account types**                            | Current                                      |
| **Requires PSU IP address**                  | No                                           |
| **Auto Onboarding**                          | None                                         |
| **IP Whitelisting**                          | None                                         |
| **Type of needed certificate**               | Eidas certificate are required : QSEAL       |
| **Repository**                               | https://git.yolt.io/providers/monorepo-group |


##Additional developer portal info:
There was no possibility to use email as username, hence account was created with YfkeOltenaar username.
It looks like login had flaws and every first attempt to log in 401 error is returned. Fallowing tries are successful.


## Links - development

|                                              |                                                                                                               |
|----------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| **Developer portal**                         | https://api.librabank.ro/store/                                                                               |
| **Sandbox base url**                         | https://api-test.librabank.ro:8243/                                                                           |
| **Sandbox authorization/authentication**     | https://is-test.librabank.ro:9443/oauth2/authorize                                                            |
| **Documentation**                            | https://api.librabank.ro/store/site/static/Libra%20Internet%20Bank%20API%20-%20Technical%20Documentation.pdf  |

## Links - production

|                                              |                                                |
|----------------------------------------------|------------------------------------------------|
| **Production base url**                      | https://api-test.librabank.ro:8243             |
| **Production authorization/authentication**  | https://is.librabank.ro/oauth2/authorize       |
| **Production accounts**                      | https://api.librabank.ro:8243/ACCOUNTS_API/v1  |

## Client configuration overview

|                         |                                                  |
|-------------------------|--------------------------------------------------|
| **Signing key id**      | Eidas signing key id                             |
| **Signing certificate** | Eidas signing certificate                        |
| **Client Id**           | Consumer Key retrieved from developer portal.    |
| **Client Secret**       | Consumer Secret retrieved from developer portal. | 

### Registration details
This bank required registering application on their developer portal and notifying them by email so they can approve production keys.

## Certificate rotation


## Connection Overview


## Business and technical decisions

## Sandbox overview
Sandbox seems to work and it requires signing. 

## User Site deletion
Consent deletion is available.

## External links
* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20LIBRA_BANK%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status/>
