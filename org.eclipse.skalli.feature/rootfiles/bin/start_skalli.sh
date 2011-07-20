# start skalli

# define java system properties
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Declipse.ignoreApp=true"
JAVA_OPTS="$JAVA_OPTS -Dosgi.noShutdown=true"
JAVA_OPTS="$JAVA_OPTS -Dequinox.ds.print=true"
JAVA_OPTS="$JAVA_OPTS -Djetty.home=./jetty"
JAVA_OPTS="$JAVA_OPTS -Dlogback.configurationFile=./jetty/etc/logback.xml"

# define start arguments, clear config area, enable console
SKALLI_OPTS=
SKALLI_OPTS="$SKALLI_OPTS -clear"
SKALLI_OPTS="$SKALLI_OPTS -console"
SKALLI_OPTS="$SKALLI_OPTS -consoleLog"
SKALLI_OPTS="$SKALLI_OPTS -configuration ./configuration"

# start from one directory above as path references are relative to SKALLI_HOME
cd ..
java $JAVA_OPTS -jar plugins/org.eclipse.osgi_*.jar $SKALLI_OPTS
