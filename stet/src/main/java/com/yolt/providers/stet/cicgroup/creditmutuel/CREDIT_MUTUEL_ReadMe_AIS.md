## Crédit Mutuel (AIS)
[Current open problems on our end][1]

is a French cooperative bank, with headquarters in Strasbourg, Alsace. Its slogan is "La banque qui appartient à ses 
clients, ça change tout!" ("A bank owned by its customers, that changes everything!"). It is currently run 
by Nicolas Théry and has over 30 million customers. It is a member of the International Raiffeisen Union (IRU), which is 
an association of cooperatives based on the ideas of Friedrich Wilhelm Raiffeisen

## BIP overview

|                                       |                                                            |
|---------------------------------------|------------------------------------------------------------|
| **Country of origin**                 | France                                                     | 
| **Site Id**                           | 8194a783-cb30-49d5-923a-d814479d6386                       |
| **Standard**                          | STET                                                       |
| **Form of Contact**                   | Email - openbanking@e-i.com                                |
| **Developer Portal**                  | https://www.creditmutuel.fr/oauth2/en/devportal/index.html |
| **Signing algorithms used**           | SHA 256 with RSA                                           |
| **Mutual TLS Authentication Support** | Yes                                                        |
| **IP Whitelisting**                   | No                                                         |
| **Auto-onboarding**                   | Yes                                                        |
| **Type of certificate**               | eIDAS (QWAC, QSEAL)                                        |
| **AIS Standard version**              | 2.1                                                        |
| **Account types**                     | CURRENT_ACCOUNT, CREDIT_CARDS                              |
| **Requires PSU IP address**           | Yes                                                        |
| **Repository**                        | https://git.yolt.io/providers/stet                         |

## Links - production
|                    |                                                                      |
|--------------------|----------------------------------------------------------------------|
| **Login domains**  | [https://oauth2-apisi.e-i.com/cm/](https://oauth2-apisi.e-i.com/cm/) | 

## Links - sandbox
|                   |                                                                                      |
|-------------------|--------------------------------------------------------------------------------------|
| **Login domains** | [https://oauth2-apisi.e-i.com/sandbox/cm/](https://oauth2-apisi.e-i.com/sandbox/cm/) | 

## Registration details
The bank requires dynamic registration, autoonboarding is enabled. The registration is pretty normal, except
one thing, when registering one has to provide a Client Name (this field which will be later displayed in the client's
application - it is good to consult this value with TL before typing it ).

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 
