## La Banque Postale (AIS)
[Current open problems on our end][1]

La Banque Postale is a French bank, created on 1 January 2006 as a subsidiary of La Poste, the national postal service.

## BIP overview 
[Main reference BIP][2]

|                             |                                      |
|-----------------------------|--------------------------------------|
| **Country of origin**       | France                               | 
| **Site Id**                 | 6306b446-a24f-11e9-a2a3-2a2ae2dbcce4 |
| **Standard**                | [STET Standard][3]                   |
| **AIS Standard version**    | 1.4.2.4.4                            |
| **PISP Standard version**   | 1.4.2.10                             |
| **Account types**           | CURRENT_ACCOUNT                      |
| **Requires PSU IP address** | Yes                                  |
| **Repository**              | https://git.yolt.io/providers/stet   |

## Links - production 
|                   |                                                                                                            |
|-------------------|------------------------------------------------------------------------------------------------------------|
| **Login domains** | [m.labanquepostale.fr](m.labanquepostale.fr) <br> [oauth2.labanquepostale.com](oauth2.labanquepostale.com) | 

## Client configuration overview
|                                  |                                                                            |
|----------------------------------|----------------------------------------------------------------------------|
| **Transport key id**             | eIDAS transport key id                                                     |
| **Transport certificate**        | eIDAS transport certificate                                                |
| **Client id**                    | used in AISP/PISP authorization flow, created during autoonboarding        |
| **Client secret**                | used in PISP authorization flow, created during autoonboarding             |
| **Client contact email address** | used for autoonboarding as a mean of communication                         |
| **Client api portal username**   | used for autoonboarding call authorization                                 |
| **Client api portal password**   | used for autoonboarding call authorization                                 |
| **Client name**                  | registration creates an app. This is the app name which needs to be unique |

## Registration details
Autoonboarding is in place, but it's not typical dynamic registration. The required authorization of the registration request consists of our credentials from developer portal. Each registration creates inside the portal a new app under a unique name, which have a corresponding pair of clientId/clientSecret.
The description of the endpoint can be found [here][5]. After succesful registration it will still not work! You have to subscribe onto the concrete API's manually in the developer portal. 

### Delete registration
Deletion of registration was not recognized, therefore no REST communication with the bank in order to delete the registration.
Please note that the current request to delete the registration will be performed by deleting it only on our side.

### Certificate rotation

## Connection Overview
Useful information can be found on the [Developer Portal][4], [STET Standard][3] and in the connections@yolt.com mailbox 

**Consent validity rules**

Consent validity rules has been implemented for La Banque Postal AIS.

  
## External links
* [Current open problems on our end][1]
* [Main reference BIP][2]
* [STET Standard][3]
* [Developer Portal][4]
 
[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20LA_BANQUE_POSTALE%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: <https://yolt.atlassian.net/wiki/spaces/LOV/pages/3908622/BIP%3A+La+Banque+Postale>
[3]: <https://www.stet.eu/en/psd2/>
[4]: <https://developer.labanquepostale.com/>
[5]: <https://developer.labanquepostale.com/api/dsp2/register>


### Testing against sandbox

Additional changes were applied to registration request based on the email from bank itself
```text
The problem comes from your request Body, you have to :
- delete the “client metadata” level
- add a “description” attribute like this : "description": "Modèle de création APP LBP"
```

Therefore, `swagger/labanquepostale-register-1.0.2.yaml` is not the same as on their website.

## PIS
They informed us that minimal amount to create payment via their API is 1,5 EUR.

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method.

## Business and technical decisions
LBP maximum transaction fetch date time period cannot exceed 90 days. We decided to add a proper validation which formats the `dateFrom` parameter if exceeds 89 days.

C4PO-9794
We decided to filter pending transactions, because we had an issue with empty bookingDate.
And these transactions are filtered later by core team.