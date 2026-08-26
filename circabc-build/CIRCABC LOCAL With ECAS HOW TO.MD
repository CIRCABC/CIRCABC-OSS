1/Change ecasBaseUrl:
1.1/Change the value in ECAS client in WebLogic config.xml:

<sec:authentication-provider xmlns:sch="https://www.cc.cec/cas/schemas" xsi:type="sch:ecas-identity-asserter-v2Type">
        <sec:name>EcasIdentityAsserterV2</sec:name>
        <sch:control-flag>SUFFICIENT</sch:control-flag>
        <sch:ecas-base-url>https://webgate.ec.europa.eu</sch:ecas-base-url>
	<sch:excluded-context-path>/console</sch:excluded-context-path>
</sec:authentication-provider>

1.2/Change the value in circabc ecas-config.properties file:
eu.cec.digit.ecas.client.filter.ecasBaseUrl=https://webgate.ec.europa.eu

2/add JAVA OPTION eu.cec.digit.ecas.client.ignoreEcasHostnameDnsResolutionError=true in WebLogic setDomainEnv.cmd file:
set JAVA_OPTIONS=%JAVA_OPTIONS% -Deu.cec.digit.ecas.client.ignoreEcasHostnameDnsResolutionError=true

3/TO Avoid the problem with CEDProd Ldap causing org.alfresco.error.AlfrescoRuntimeException: 02310001 A module component 'circabcAmp.circabcComponent' failed to execute: No username specified when creating the person:
3.1/ First possibility: Set property build.circabc.release to oss in alfresco-global.properties (keep ent in angular environment.ts file to be able to authenticate with ECAS)
3.2/ Second possibility: First deploy the OSS version of the application that will insert some users in CBC_USERS table, undeploy the OSS version and Deploy the Enterprise version
3.3/ Third possibility: Provide a Mock Implementation of LDAP with some hardcoded users.