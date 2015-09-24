# StudentContest.APIThrottleHandler

WSO2 APIThrottleHandler used for by the Student Contest project

This project exists out of a custom API Throttle Handler for the WSO2 API Manager that was used initially for the i8c Student Contest in the spring of 2015.

The purpose of this throttle handler was to limit the number of API calls a student could perform via a web page to one per minute. Each student was uniquely identified by his/her school email address in the JSON payload of each submited HTTP request.

More info on creating custom handlers for WSO2 API Manager can be found here: https://docs.wso2.com/display/AM180/Writing+Custom+Handlers#WritingCustomHandlers-Writingacustomhandler

To build this project, create a new project in eclipse by importing the source files in the "core" folder as a new maven project. This custom handler was written for API Manager v1.8.0.

To install, build the project as a jar files and copy the jar file to the repository\components\lib folder of the WSO2 API Manager installation. Update the axis2.xml configuration file of the WSO2 API Manage under repository\conf\axis2 so that it uses NIO HTTP instead of passthru (see also example config in samples folder).

To test it, use the samples in the "samples" folder:
  - StudentContestWeb
    - StudentContestWSO2APIMgr-soapui-project.xml: a SOAP-UI project that contains a client request to test the throttle handler + a mock service that can be called by the WS02 API Manager
    - StudentContestWebAPI.xml: the WSO2 API Manager API metadata of the StudentContestWeb API created via the store
    - SynapseDefinitions.xml: an example of the full synapse config of a working installation, including the customer API ThrottleHandler
    - axis2.xml: an example of a WSO2 API Manager axis2 configuration file that uses NIO HTTP







