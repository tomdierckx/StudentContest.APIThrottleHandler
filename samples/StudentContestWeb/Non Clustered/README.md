For deployment of the handler in a non cluster environment following steps should be taken.

Import the pom file in the core directory in Eclipse. Build a package using maven. Import this ".jar" file in the repository/components/lib directory of your API manager instance. 

Apply the same configuration in the repository/conf/axis2/axis2.xml as in the example axis2.xml configuration.
Focus on the Message builders/formatters and http NIO.

Using the metadata in the StudentContestWebAPI.xml file create the studentContestAPI in the Publisher.

Add the handler to the API studentContest in the source View. This using the management console(:9443/carbon).


<handler class="be.i8c.carbon.apimgt.gateway.handlers.throttling.APIThrottleHandler"/>

