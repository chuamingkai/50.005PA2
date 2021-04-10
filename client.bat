@set option=%1
@shift
@set params=%1
@:loop
@shift
@if [%1]==[] goto afterloop
@set params=%params% %1
@goto loop
@:afterloop
javac Client%option%.java
java Client%option% %params%