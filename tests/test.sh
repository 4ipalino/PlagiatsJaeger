#!/bin/bash


cd /var/www/tests

phpunit --coverage-html /usr/share/tomcat7/.jenkins/jobs/PlagiatsJaegerPHP/workspace/reports --coverage-clover /usr/share/tomcat7/.jenkins/jobs/PlagiatsJaegerPHP/workspace/reports/clover.xml ./

