@echo off
echo :
echo :
echo : ----------------------- Stateful SIP Proxy -----------------------
echo :
@echo on
java -classpath lib/sip.jar;lib/server.jar local.server.StatefulProxy -f config\server.cfg %1 %2 %3 %4 %5 %6 %7 %8 %9