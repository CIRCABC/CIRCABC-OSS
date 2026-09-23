# EU CAPTCHA

EU CAPTCHA embodies the results and work in progress of [ISA² Action 2018.08 EU-Captcha](https://ec.europa.eu/isa2/actions/developing-open-source-captcha_en). 
A CAPTCHA is a test intended to distinguish human from machine input. The objective of this action is to offer to the Member States an open source CAPTCHA released under the EUPL (European Union Public License) that is maintained, secure, user friendly and multilingual. It will be delivered as a component that can be operated as a service. A CAPTCHA with such characteristics does not exist on the market. The delivered solution will be published on GitHub so that it can be reviewed and maintained by the open source community.

EU CAPTCHA supports two types of CAPTCHA:
- Alphanumeric CAPTCHA with audio transcription
- Image rotation CAPTCHA 

## Installation

EU CAPTCHA can be installed in two ways:
- by integrating with the managed service
- by deploying the code (.WAR file) on your server/environment.

Manuals and documentation for both installation methods can be found in the respective GitHub folder. 

## Objectives of the Action
A CAPTCHA is a test intended to distinguish human from machine input in order to thwart spam and automatic submission or extraction of data. The user is typically challenged to solve a puzzle that relies on expected capacities of the human brains but whose resolution is complex to automate. 
Users and, in particular, disabled people are known to dislike CAPTCHAs that are perceived as hindrances. However, no better solution was found so far to protect information systems against malicious automated processes. 
The characteristics of a good CAPTCHA are: 
- Security – The number of non-human users able to solve the puzzle and therefore wrongly identified as being human must be minimised, which implies that the puzzle should be highly complex to automate; 
- User friendliness – The number of human users unable to solve the puzzle and therefore wrongly identified as being non-human must be minimised, which implies that the puzzle should be very easy to solve in a short timeframe by any human being. 

Several CAPTCHA solutions exist on the market, either provided as components or as services. Unfortunately, they all have one or more of the following shortcomings: 
- They provide an insufficient level of security with a high rate of false positives; 
- They provide an insufficient level of user friendliness with a high rate of false negatives; 
- They are not or insufficiently maintained; 
- They do not support internationalisation or multilingualism and, in particular, they do not support all official languages of the European Union; 
- They do not support users with disabilities; 
- They do not have a licensing model that is compatible with EUPL and, in particular, they cannot be distributed as part of systems provided by public administrations; 
- They raise ethical concerns because they collect private data or provide puzzles whose resolution creates commercial value. 

The objective of the action is to provide an open source CAPTCHA that is: 
1. released under the EUPL (European Union Public License); 
2. available as a component and operable as a service; 
3. secure; 
4. user friendly; 
5. multilingual with support for all official languages from the European Union; 
6. accessible by users with disabilities; 
7. compliant with data protection rules and best practices; 
8. maintained with continuous support for subsequent versions of the Java Virtual Machine. 

## License

[European Union Public License (EUPL) 1.2](https://github.com/pwc-technology-be/EU-CAPTCHA/blob/master/LICENSE.md)
