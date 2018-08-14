@echo off
REM find java.exe in subdirectory - elsewise assume its on the path
set _myjava=
for /f "delims=" %%i in ('dir java.exe /b /s') do set _myjava=%%i
if defined _myjava (
	echo Java found: %_myjava%
)else (
	echo Java not found in local directory - assuming it's on the path!
	set _myjava=java
)
echo Java command used: %_myjava%

REM start Transkribus with the java bin set in _myjava
"%_myjava%" -Dfile.encoding=UTF-8 -jar ${project.build.finalName}.jar
