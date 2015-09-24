# StudentContest.APIThrottleHandler

WSO2 APIThrottleHandler used for by the Student Contest project

This project exists out of a custom API Throttle Handler for the WSO2 API Manager that was used initially for the i8c Student Contest in the spring of 2015.

The purpose of this throttle handler was to limit the number of API calls a student could perform via a web page to one per minute. Each student was uniquely identified by his/her school email address in the JSON payload of each submited HTTP request.

To build this project, create a new project in eclipse by importing the source files in the core folder as a new maven project.







