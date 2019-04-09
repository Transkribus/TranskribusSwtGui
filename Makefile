# maven vars:
mvn=mvn
test=true
mvn_flags=

# set maven test flags depending on value of "test" variable
ifneq ($(tests), true)
 $(info Not performing tests!)
 mvn_test_flags = -Dmaven.test.skip=true
endif

all: pdfutils intf core client gui

pdfutils:
	$(mvn) clean install -f ../PdfUtils/pom.xml $(mvn_test_flags)

intf:
	$(mvn) clean install -f ../TranskribusInterfaces/pom.xml -Dmaven.test.skip=true

core:	
	$(mvn) clean install -f ../TranskribusCore/pom.xml $(mvn_test_flags)

client:
	$(mvn) clean install -f ../TranskribusClient/pom.xml $(mvn_test_flags)

gui:
	$(mvn) clean install $(mvn_test_flags)
		
deploy:
	$(mvn) antrun:run # this copies the currently built (!) version to the deploy folders
		
clean:
	$(mvn) clean
	
#start_gui:
#	TODO
