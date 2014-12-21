@echo off
echo :
echo :
echo : ----------------------- QSIP Proxy -----------------------
echo :
@echo on
java -classpath lib/sip.jar;lib/server.jar;lib/qsip.jar local.qos.MyQSipProxy -f config\server.cfg %1 %2 %3 %4 %5 %6 %7 %8 %9