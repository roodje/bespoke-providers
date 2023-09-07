## Triodos Bank (NL) (AIS)
[Current open problems on our end][1]

Triodos Bank N.V. is a bank based in the Netherlands with branches in Belgium, Germany, France, United Kingdom and Spain. 
As of 2020, Triodos Bank has 721,000 customers worldwide
 

## BIP overview 
[NL reference BIP][2]
[UK reference BIP][3]
[BE reference BIP][4]

|                                       |                                                                                                 |
|---------------------------------------|-------------------------------------------------------------------------------------------------|
| **Country of origin**                 | The Netherlands, Belgium, The United Kingdom                                                    | 
| **Site Id**                           | 8993cd94-8000-47e3-b21b-f7ec323b5340 (NL)                                                       |
| **Site Id**                           | 5f3dd396-882f-42f5-93fc-9158caa3036c (UK)                                                       |
| **Site Id**                           | c2a0851c-b2cf-444f-8773-ae8870bf8125 (BE)                                                       |
| **Standard**                          | [STET Standard][5]                                                                              |
| **Contact**                           |                                                                                                 |
| **Developer Portal**                  |                                                                                                 | 
| **Account SubTypes**                  | CURRENT_ACCOUNT                                                                                 |
| **IP Whitelisting**                   |                                                                                                 |
| **AIS Standard version**              | 1.3                                                                                             |
| **Auto-onboarding**                   |                                                                                                 |
| **Requires PSU IP address**           | Yes (according to e-mail), Yes for PIS (according to Swagger). No limits are currently in place |
| **Type of certificate**               | eIDAS                                                                                           |
| **Signing algorithms used**           |                                                                                                 |
| **Mutual TLS Authentication Support** |                                                                                                 |
| **Repository**                        | https://git.yolt.io/providers/bespoke-triodosbank                                               |

## Client configuration overview
|                                  |                                                         |
|----------------------------------|---------------------------------------------------------|
| **Signing key id**               | eIDAS signing key id                                    | 
| **Signing certificate**          | eIDAS signing certificate                               | 
| **Transport key id**             | eIDAS transport key id                                  |
| **Transport certificate**        | eIDAS transport certificate                             |
| **Certificate Agreement Number** | value obtainable from our eIDAS certificate, extensions |
| **Client id**                    | value obtainable after client dynamic registration      | 

### Registration details
This bank requires customer registration. The client registration flow is dynamic and compliant with RFC7591.

### Certificate rotation
As the bank does not require any certification during registration. We can simply start using new certificate.

## Connection Overview
The bank uses http cavage signatures for signing the requests, which uses the
following fields in this order '(request-target)',  'Digest' and  'X-Request-Id'.
The provider uses RFC 7616 for calculating the digest with SHA-256 algorithm.
Simplified sequence diagram:
[flow][9]

**Consent validity rules** are set to EMPTY_RULES_SET for Triodos bank due to 
login page cannot be checked on developer machine as `No pending authorisations found for consent with uuid`
error appears when opening it for the second time.

### Transaction - dates base on statuses
In case of a pending transaction, only the “bookingDate” will be populated.
The “valueDate” will be filled once the transaction is actually booked.

### Debit and Credit Transactions
From bank documentation:

Note that amounts in the transaction report are not signed. The direction of the transaction can be determined from the
presence of the debtorAccount (received) or creditorAccount (payed) fields.

According to that we should map transactions with creditorAccount to "Debit" transaction type, and transactions
with debtorAccount to "Credit".

But there is one remark about pendning transactions:

* pending transaction with "-"  
The refund is an incoming transaction for the customer as the money is returned from the merchant. 
This explains why the minus sign was there in combination with the creditor account when pending. When this transaction became booked, the minus sign is no longer there as the debtor account signifies that it’s a received payment.
  

* pending transaction with "+"  
Pending transactions without the minus sign behave as expected in that the direction of the payment is solely determined 
by the presence of the DebtorAccount (incoming) or CreditorAccount (outgoing).

  Update about pending transaction with "-"
We made a workaround to map this transaction correctly. At 1th of December Bank told us that fix is already on PRD "As suspected, it is caused by refund transactions that can also exist in pending state, which our system currently doesn’t handle correctly. The fix has been deployed". We are not going to change our workaround because of fact that issue is difficult to catch.

### Transaction - empty "remittanceInformationUnstructured" field in json
"remittanceInformationUnstructured" field might be sent empty and it's working as expected, confirmed in mail by their support

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 

## Business decisions
Validations problems during transactions fetch for Jortt (<https://yolt.atlassian.net/browse/YT-96>), proposed solution
is to pass empty string when transaction description is missing

## Sandbox overview
--empty--

**Payment Flow Additional Information**

|                                                                                                        |                                                                                                                                                 |
|--------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?**                    | execute-on-submit                                                                                                                               |
| **it is possible to initiate a payment having no debtor account**                                      | YES                                                                                                                                             |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | For periodic payments the final status is ACCP, for bulk payments the final status is ACSP and for all other payments the final status is ACSC. |

## External links
* [Current open problems on our end][1]
* [Main reference BIP][2]
* [STET Standard][5]
* [http cavage signatures][46]
* [RFC 7616][7]
* [Developer portal][8]
 
[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20Triodos%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: 
[3]: 
[4]: 
[5]: <https://www.berlin-group.org/>
[6]: <https://tools.ietf.org/html/draft-cavage-http-signatures-08>
[7]: <https://tools.ietf.org/html/rfc7616>
[8]: <https://developer.triodos.com/docs/getting-started>
[9]: <https://files.readme.io/bfda24d-Xs2aPaymentSimple.png>
