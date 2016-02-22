README
------------------------------------------------------

- The Transkribus is contained in the main jar file Transkribus-<version>.jar
- Java 7 is needed to run the program. Make sure Java 7 is either installed system wide
or copy a JRE into the program directory!

- To run the program from command line type:
	java -jar Transkribus-<version>.jar
	
- To start the program from the user interface via doubleclick the following scripts are available:
	Windows: Transkribus.bat
	MAC: Transkribus.command
	Linux: Transkribus.sh
	
	Note: To run the scripts in Mac (or Linux) you may have to make them executable from the command line:
		chmod +x Transkribus.command 		(or chmod +x Transkribus.sh for Linux!)
		
- Notes for launching on MAC:
	- If you run the program the first time, it may not start because it is a non-signed application ("... can't be opened because it is from an unidentified developer" message)
	- right-click (or control-click) the application in this case and choose "Open". In the appearing dialog click "Open" again!

- config.properties can be modified to adjust simple appearance properties
- virtualKeyboards.xml can be used to specify a set of virtual keyboards
- logback.xml can be modified to adjust logging properties (for expert users only)
 
- The 'libs' subfolder contains the necessary libraries for all platforms. Currently supported are:
		- Windows 32/64 bit
		- Linux 32/64 bit
		- OSX 64 bit
		
- Usage Notes:
	- Read USAGE.txt
- Version Changelog:
	- Read CHANGES.txt