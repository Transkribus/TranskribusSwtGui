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

reg Query "HKLM\Hardware\Description\System\CentralProcessor\0" | find /i "x86" > NUL && set OS=32BIT || set OS=64BIT

if %OS%==32BIT (
	echo Adding SWT 32bit dependency to classpath
	set swt="swt\swt-${swt.version}-win32.jar"
) else ( 
	echo Adding SWT 64bit dependency to classpath
	set swt="swt\swt-${swt.version}-win64.jar"
)

REM start Transkribus with the java bin set in _myjava
"%_myjava%" -cp %swt% -Dfile.encoding=UTF-8 -jar ${project.build.finalName}.jar
