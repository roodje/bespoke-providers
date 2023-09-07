
# bespoke-sparkassen-and-landesbanks

[Current open problems on our end][1]

Sparkassen-and-landesbanks is a group of german banks

## BIP overview 
[Main reference BIP][2]

|   |   |
|---|---|
| **Country of origin** | Germany | 
| **Site Id**  |  8ac70240-e629-45d1-8dec-b646c34d8f6c |
| **Standard**   |  [Berlin Group Standard][3] |
| **AIS Standard version**  |  unspecified |
| **PISP Standard version**  | (not implemented in providers)|
| **Account types**| CURRENT_ACCOUNT |
| **Requires PSU IP address** |No (but implemented based on experience with Berlin-Group)

## Client configuration overview
|   |   |
|---|---|
| **Transport key id**  |  eIDAS transport key id |
| **Transport certificate** | eIDAS transport certificate |

### Registration details
No registration is needed. A valid eIDAS certificate should be enough to consume the AIS API.

### Certificate rotation
As the bank does not require any certification and there is no auto onboarding in place we can simply start using a new certificate.

## Connection Overview
Useful information can be found on the [Developer Portal][4]
Things worth noticing:
* There is a dynamic flow and the PSU needs to choose their Sparkasse 
* There is no separate call for balances, they are retrieved together with accounts with the use of **withBalances** query parameter
* eIDAS's **organization identifier** should be provided as **client_id**
* Transactions data is served only in XML

**Consent validity rules** are implemented for LBBW AIS & Nord LB AIS Banks. 
Sparkassen bank uses dynamic flow, thus we are unable to determine consent validity rules for AIS.

**Payment Flow Additional Information**

|   |   |
|---|---|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?** | execute-on-consent |
| **it is possible to initiate a payment having no debtor account** | NO |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | AcceptedSettlementCompleted | 

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 

## External links
* [Current open problems on our end][1]
* [Main reference BIP][2]
* [Berlin Group Standard][3]
* [Developer Portal][4]
 
[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20SPARKASSEN%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: <https://yolt.atlassian.net/wiki/spaces/LOV/pages/3899724/BIP+Sparkassen+and+Landesbanken>
[3]: <https://www.berlin-group.org/>
[4]: <https://xs2a.sparkassen-hub.com/sandbox>
