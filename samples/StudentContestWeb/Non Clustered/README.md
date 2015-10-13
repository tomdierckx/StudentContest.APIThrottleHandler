For deployment of the handler in a non cluster environment following steps should be taken.

Import the pom file in the core directory in Eclipse. Build a package using maven. Import this ".jar" file in the repository/components/lib directory of your API manager instance. 

Apply the same configuration in the repository/conf/axis2/axis2.xml as in the example axis2.xml configuration.
Focus on the Message builders/formatters and http NIO.

Load the StudentContestWSO2APIMgr-soapui-project.xml file in SOAPUI and start the mock object.

Start the WSO2 API Manager. Verify that the jar file is accepted by the API manager by checking the repository/components/dropins directory. 

Using the metadata in the StudentContestWebAPI.xml file create the studentContestAPI in the Publisher. Changing the endpoint towards the location where the SOAPUI mock object is running.

Add the handler to the API studentContest in the source View. This using the management console(:9443/carbon).

Example: <handler class="be.i8c.carbon.apimgt.gateway.handlers.throttling.APIThrottleHandler"/>

Use the SynapseDefinitions.xml file as example for adding the handler.

Send a POST to the http://xxx.xxx.xxx.xxx:8280/studentContestWeb/1.0.0/bet containing the following JSON:
{
"answers": {
	"multipleChoice": "Google",
	"tieBreaker": "123456"
	},
"student": {
	"firstName": "FirstNameTest",
	"lastName": "LastnameTest",
	"privateEmail": "test@testpriveMail.test",
	"schoolEmail": "testschoolmail@school.test",
	"mobilePhoneNumber": "TestingTheMail"
	}
}

