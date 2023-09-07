## Société Générale Particuliers (AIS)

* [API guidelines](https://developer.societegenerale.fr/en/api-guideline)
* [Products](https://developer.societegenerale.fr/en/product)

## BIP overview

|                                       |                                      |
|---------------------------------------|--------------------------------------|
| **Country of origin**                 | France                               | 
| **Site Id**                           | 654bcfc0-a173-11e9-a2a3-2a2ae2dbcce4 |
| **Standard**                          |                                      |
| **Contact**                           | E-mail:                              |
| **Developer Portal**                  |                                      | 
| **Account SubTypes**                  | CURRENT_ACCOUNT, CREDIT_CARD         |
| **IP Whitelisting**                   |                                      |
| **AIS Standard version**              |                                      |
| **Auto-onboarding**                   |                                      |
| **Requires PSU IP address**           |                                      |
| **Type of certificate**               | eIDAS                                |
| **Signing algorithms used**           |                                      |
| **Mutual TLS Authentication Support** |                                      |
| **Repository**                        | https://git.yolt.io/providers/stet   |

## Links - production 
|                     |                                                                                                                                                                |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Login domains**   | [particuliers.societegenerale.fr](particuliers.societegenerale.fr) <br> [particuliers.sg-signin.societegenerale.fr](particuliers.sg-signin.societegenerale.fr) | 

## Connection overview

**Consent testing**

* Societe Generale Ent and Pro have to be blacklisted from consent testing due to business decision made by Leon. See https://yolt.atlassian.net/browse/C4PO-3241 for reference. 
* Consent validity rules has been implemented for Societe Generale PRI PIS.
* Societe Generale PRI/ENT/PRO AIS uses EMPTY_RULES_SET as consent validity rules due to error page shares same HTML code as a correct one.

**Payment Flow Additional Information**

|                                                                                                        |                             |
|--------------------------------------------------------------------------------------------------------|-----------------------------|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?**                    | execute-on-submit           |
| **it is possible to initiate a payment having no debtor account**                                      | YES                         |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | AcceptedSettlementCompleted |

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method.

## Business decisions

* 07.08.2020. Due to the fact, we receive 3 types of balances in Societe Generale PRI,
we decided to map them in the given order: XPCD - CLBD - OTHR. Decision was approved by Leon.

* 17.12.2020 - many times for `CARD` cashAccountType we receive transactions with status `OTHR` which breaks
our implementation (https://yolt.atlassian.net/browse/C4PO-6711). Leon decided to map each transaction with
status `OTHR` into `PENDING`.
Reference for transactions statuses (chapter 4.4.5.1. Body (application/hal+json; charset=utf-8), p. 37): [STET API v1.4.2.17][0]

* 05.05.2021 - field `expectingBookingDate` was added to SG PRI bank's data model due to bank sends us this field
not mentioned in documentation. See C4PO-8236.

[0]: https://www.stet.eu/assets/files/PSD2/1-4-2/api-dsp2-stet-v1.4.2.17-part-2-functional-model.pdf
