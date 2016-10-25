all: pdfutils core client gui

pdfutils:
	mvn clean install -f ../PdfUtils/pom.xml

core:	
	mvn clean install -f ../TranskribusCore/pom.xml

client:
	mvn clean install -f ../TranskribusClient/pom.xml

gui:
	mvn clean install
		
deploy:
	mvn antrun:run # this copies the currently built (!) version to the deploy folders
		
clean:
	mvn clean
	
#start_gui:
#	TODO
