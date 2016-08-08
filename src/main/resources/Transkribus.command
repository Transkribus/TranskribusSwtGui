#!/bin/bash

echoerr() { echo "$@" 1>&2; }

isValidJava() {
	#echo "checking java version of: "$1
	if [[ "$1" ]]; then
    version=$("$1" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    #echo "Java version = "$version
    if [[ "$version" < "1.7" ]]; then
    	#echo "false"
        return 1;
    else
    	#echo "true"
    	return 0;
    fi
	fi
	return 1;
}

cd ${0%/*}

echo "Starting Transkribus - first trying to find java, your OS = "$OSTYPE

LOCAL_JAVA=`find "./" -name 'java' | head -1` # try to find java in local dir

if [[ $OSTYPE == 'darwin'* ]]; then
	JAVA_HOME2=`/usr/libexec/java_home -v 1.7+ | head -1` # try to find java with java_home prog for mac
fi

# determine a suitable java in the following order:
# 1 try to find a java installation in the local folder, 
# 2 try to determine a suitable java using java_home when on mac
# 3 try to find a java using the JAVA_HOME env variable
# 4 try to find java on the path
if ! [ -z $LOCAL_JAVA ] && (isValidJava $LOCAL_JAVA); then
	echo "Found java executable in program directory"
	_java=$LOCAL_JAVA
elif ! [ -z $JAVA_HOME2 ] && (isValidJava $JAVA_HOME2/bin/java); then
	echo "Found suitable java with /usr/libexec/java_home"
	_java="$JAVA_HOME2/bin/java"
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo "Found java executable in JAVA_HOME"
    _java="$JAVA_HOME/bin/java"
elif type -p java 1>/dev/null; then
    echo "Found java executable in PATH"
    _java=java
else
    echoerr "Java was not found!"
    exit 1
fi

echo "Java bin = "$_java

# determine and check java version (must be >= 1.7!):
if ! isValidJava $_java; then
	echoerr "Java version >= 1.7 required!"
    exit 1
fi

# set some java flags
# set max heap space to 2 GB
java_flags="-Xmx2048m " 
# add -XstartOnFirstThread flag for mac-os (i.e. $OSTYPE == 'darwin'*)
# since elsewise there will be a deadlock on startup: 
if [[ $OSTYPE == 'darwin'* ]]; then
java_flags=$java_flags" -XstartOnFirstThread"
fi
echo "java flags are: "$java_flags

# start the bloody thing:
$_java -jar $java_flags ${project.build.finalName}.jar
