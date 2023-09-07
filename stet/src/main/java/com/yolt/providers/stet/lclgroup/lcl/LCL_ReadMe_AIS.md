## LCL (AIS)

## BIP overview

|                                       |                                      |
|---------------------------------------|--------------------------------------|
| **Country of origin**                 | France                               | 
| **Site Id**                           | 3505ed3f-75e9-4402-804c-299b4097cecb |
| **Standard**                          | [STET Standard][2]                   |
| **Contact**                           |                                      |
| **Developer Portal**                  |                                      | 
| **Account SubTypes**                  | CURRENT_ACCOUNT                      |
| **IP Whitelisting**                   |                                      |
| **AIS Standard version**              |                                      |
| **Auto-onboarding**                   |                                      |
| **Requires PSU IP address**           |                                      |
| **Type of certificate**               | eIDAS                                |
| **Signing algorithms used**           |                                      |
| **Mutual TLS Authentication Support** |                                      |
| **Repository**                        | https://git.yolt.io/providers/stet   |

## User Site deletion

This provider does NOT implement `onUserSiteDelete` method.

## Registration Removal

The delete endpoint is not implemented, it is not possible to remove the registration on the bank side.

## Certificate Rotation

During certificate rotation for CASY (June 2022) it was found out that our implementation of registration update is
invalid. This is because it consists of two calls: GET /register and PUT /register/<client_id>. The former requires old
certificates, the latter new ones. This is impossible without doubling authentication means and adjusting the code.

What's more - the CASY registration had 8 redirect urls, we had to remove "https://client-redirect.yts.yolt.io" as it is
used only by unlicensed clients and the API used to return http-400 bad request indicating that this exact redirect url
doesn't pass the regex validation (missing trailing slash, although it's already been registered like that). It was much
faster to get rid of this redirect url than to wait for a bank to fix this.
