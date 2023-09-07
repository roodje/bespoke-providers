## Crédit Industriel et Commercial (AIS)
[Current open problems on our end][1]

Crédit Industriel et Commercial (CIC) is a financial services group in France, founded in 1859.
With its parent-company, Crédit Mutuel it is the fourth largest bank in the country. CIC alone has 1,890 branches 
and over 24,000 employees serving over 3.6 million customers[3] The company offers savings accounts, mortgages
and loans, it also owns stakes in specialised entities involved in private banking, asset
management, leasing, securities brokerage, and property/casualty insurance

## BIP overview

|                                       |                                                   |
|---------------------------------------|---------------------------------------------------|
| **Country of origin**                 | France                                            | 
| **Site Id**                           | a6079edf-2426-4ef4-ad13-43b4b9ac7091              |
| **Standard**                          | STET                                              |
| **Form of Contact**                   | Email - openbanking@e-i.com                       |
| **Developer Portal**                  | https://www.cic.fr/oauth2/en/devportal/index.html |
| **Signing algorithms used**           | SHA 256 with RSA                                  |
| **Mutual TLS Authentication Support** | Yes                                               |
| **IP Whitelisting**                   | No                                                |
| **Auto-onboarding**                   | Yes                                               |
| **Type of certificate**               | eIDAS (QWAC, QSEAL)                               |
| **AIS Standard version**              | 2.1                                               |
| **Account types**                     | CURRENT_ACCOUNT, CREDIT_CARDS                     |
| **Requires PSU IP address**           | Yes                                               |
| **Repository**                        | https://git.yolt.io/providers/stet                |

## Links - production
|                   |                                                                        |
|-------------------|------------------------------------------------------------------------|
| **Login domains** | [https://oauth2-apiii.e-i.com/cic/](https://oauth2-apiii.e-i.com/cic/) | 

## Links - sandbox
|                    |                                                                                        |
|--------------------|----------------------------------------------------------------------------------------|
| **Login domains**  | [https://oauth2-apiii.e-i.com/sandbox/cic/](https://oauth2-apiii.e-i.com/sandbox/cic/) | 

## Registration details
The bank requires dynamic registration, autoonboarding is enabled. The registration is pretty normal, except
one thing, when registering one has to provide a Client Name (this field which will be later displayed in the client's
application - it is good to consult this value with TL before typing it ).

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 
