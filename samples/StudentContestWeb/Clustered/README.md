##Clustered Configuration

In this directory are 2 example axis2.xml configuration files.

Both API Managers have been configured with the API found in the "/Other" directory use the metadata for configuration in the publisher.

For installation of 2 API managers running on the same HOST: using the <offset></offset> tag in the carbon.xml file.

 -Errors can occure on the ThriftClient's conflicting ports change in the api-manager.xml

Manipulation of the (on linux "/etc/hosts" ) hosts file is nessasary to make this configuration work.
Add the following to the hosts file:

127.0.0.1   wso2.node1

127.0.0.1   wso2.node2

Before adding the handler to the API on the API managers make sure you added the .jar file you build out of the core files in the "repository/components/lib" directory. Restart the server and validate in de "repository/components/dropins" if the jar is present for succesfull deployment.

After starting both the servers check for ‘member joined’ log messages in all both consoles to validate the clustered configuration.

After the handler has been implemented on both API managers. And applied in the synapseDefinition for the studentContest API. 
The Throttling per email adress should happen on cluster level. A second call with the same email adress to APIM1/APIM2 will be trottled out.

Import the StudentContestWSO2APIMgr-soapui-project.xml in SOAPUI to setup the mock service for testing purposes. Configure the endpoint of the API with the SOAPUI mock service to test for reply.