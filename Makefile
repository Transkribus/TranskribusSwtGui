all:
	mvn clean install -f ../PdfUtils/pom.xml
	mvn clean install -f ../TrpCore/pom.xml
	mvn clean install -f ../TrpClient/pom.xml
	mvn clean install

core:	
	mvn clean install -f ../TrpCore/pom.xml

gui:
	mvn clean install
		
deploy:
	mvn antrun:run # this copies the currently built (!) version to the deploy folders / servers	
		
clean:
	mvn clean
