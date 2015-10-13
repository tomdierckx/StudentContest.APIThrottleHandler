##Clustered Configuration

In this directory 2 configuration files for 2 clustered API manager instances.

Both API Managers have been configured with the API found in the "/Other" directory.

For installation of 2 API managers running on the same HOST: using the <offset></offset> tag in the carbon.xml file.

Manipulation of the (on linux "/etc/hosts" ) hosts file is nessasary to make this configuration work.
Add following to the hosts file:

127.0.0.1   wso2.node1

127.0.0.1   wso2.node2

Before adding the handler to the API on the API managers make sure you added the .jar file you build out of the core files in the "repository/components/lib" directory. After adding to the "lib" directory restart the server. Validate in de "repository/components/dropins" if the jar is present for succesfull deployment.

After starting both the servers check for ‘member joined’ log messages in all consoles to validate the clustered configuration.

After the handler has been implemented on both API managers. And applied in the synapseDefinition for the studentContest API. 
The Throttling per email adress should happen on cluster level. A call second call to APIM1/APIM2 will be trottled out.