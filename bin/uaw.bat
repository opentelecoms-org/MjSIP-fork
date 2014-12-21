@echo off
echo :
echo :
echo : ----------------- User Agent -----------------
echo :
@echo on
rem c:\programmi\jdk1.3.1\jre\bin\java -classpath lib/sip.jar;lib/ua.jar local.ua.GraphicalUA %1 %2 %3 %4 %5 %6 %7 %8 %9
java -classpath %CLASSPATH%;lib/sip.jar;lib/ua.jar local.ua.GraphicalUA %1 %2 %3 %4 %5 %6 %7 %8 %9