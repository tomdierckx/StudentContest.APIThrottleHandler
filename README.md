# StudentContest.APIThrottleHandler

WSO2 Email Throttler.

This project is based on the original Throttle Handler used for the i8c Student Contest in the spring of 2015.

The purpose of this throttle handler was to limit the number of API calls a student could perform via a web page to one per minute. Each student was uniquely identified by his/her school email address in the JSON payload of each submitted HTTP request.

The problem with this handler was the lack of clustered support. In this variant there is support for clustering using the JCache library. And a complete overhaul of the handler flow based on the original IP throttlers found in WSO2 products. 

The extraction of the email address out of the Message Context has been tested with the following message builders:
    - v1.9.1 -> org.apache.synapse.commons.json.JsonBuilder
    - v1.8.0 -> org.apache.axis2.json.JSONBuilder
More info on these builders can be found here: https://docs.wso2.com/display/AM191/Transforming+API+Message+Payload

More info on creating custom handlers for WSO2 API Manager can be found here: https://docs.wso2.com/display/AM190/Writing+Custom+Handlers

To build this project, create a new project in eclipse by importing the source files in the "core" folder as a new maven project. This custom handler has been tested in API manager v1.8.0 and v1.9.1.

To install, build the project as a jar file and copy the jar file to the repository\components\lib folder of the WSO2 API Manager installation. Update the axis2.xml configuration file of the WSO2 API Manage under repository\conf\axis2 so that it uses NIO HTTP instead of passthru (see also example config in samples folder).

The Handler has been written to automatically detect the cluster environment. If this handler is deployed on multiple API managers running in clustered configuration. Throttling will happen on cluster level. It can however be deployed on a single instance.

In samples there are the config files for deployment in a cluster or single instance of an API manager v1.9.1 and readme files defining the basic setup.

Sample files for deployment non clustered environment:
    - StudentContestWSO2APIMgr-soapui-project.xml: a SOAP-UI project that contains a client request to test the throttle handler + a mock service that can be called by the WS02 API Manager
    - StudentContestWebAPI.xml: the WSO2 API Manager API metadata of the StudentContestWeb API created via the store
    - SynapseDefinitions.xml: an example of the full synapse config of a working installation, including the custom API ThrottleHandler
    - axis2.xml: an example of a WSO2 API Manager axis2 configuration file that uses NIO HTTP and the correct JSON builder/formatter.

Sample files for deployment clustered environment:
    - API Manager 1: Contains the configuration files for the first API manager in cluster
        - axis2.xml: WSO2 API Manager axis2 configuration file that uses NIO HTTP and the correct JSON builder/formatter. Clustering is enabled and configured for the second API manager instance.
        - SynapseDefinitions.xml: an example of the full synapse config of a working installation, including the custom API ThrottleHandler
        - README.md: specific install instructions for the first API manager
    - API Manager 2: Contains the configuration files for the second API manager in cluster 
        - axis2.xml: WSO2 API Manager axis2 configuration file that uses NIO HTTP and the correct JSON builder/formatter. Clustering is enabled and configured for the first API manager instance.
        - SynapseDefinitions.xml: an example of the full synapse config of a working installation, including the custom API ThrottleHandler
        - README.md: specific install instruction for the second API manager
    - Other
        - StudentContestWebAPI.xml: the WSO2 API Manager API metadata of the StudentContestWeb API created via the store
        - StudentContestWSO2APIMgr-soapui-project.xml: a SOAP-UI project that contains a client request to test the throttle handler + a mock service that can be called by the WS02 API Manager