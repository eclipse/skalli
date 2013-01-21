###############################################################################
# Copyright (c) 2010, 2011 SAP AG and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     SAP AG - initial API and implementation
###############################################################################

# start skalli

# define java system properties
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Declipse.ignoreApp=true"
JAVA_OPTS="$JAVA_OPTS -Dosgi.noShutdown=true"
JAVA_OPTS="$JAVA_OPTS -Dequinox.ds.print=true"
JAVA_OPTS="$JAVA_OPTS -Djetty.home=./jetty"
JAVA_OPTS="$JAVA_OPTS -Dlogback.configurationFile=./jetty/etc/logback.xml"

# proxy settings
#JAVA_OPTS="$JAVA_OPTS -Dhttp.proxyHost=proxy.example.org"
#JAVA_OPTS="$JAVA_OPTS -Dhttp.proxyPort=8080"
#JAVA_OPTS="$JAVA_OPTS -Dproxy.nonProxyHosts=*.sap.corp"

# define start arguments, clear config area, enable console
SKALLI_OPTS=
SKALLI_OPTS="$SKALLI_OPTS -clear"
SKALLI_OPTS="$SKALLI_OPTS -console"
SKALLI_OPTS="$SKALLI_OPTS -consoleLog"
SKALLI_OPTS="$SKALLI_OPTS -configuration ./configuration"

# define database arguments (Derby)
#JAVA_OPTS="$JAVA_OPTS -Dskalli.persistence.eclipselink.target-database=Derby"
#JAVA_OPTS="$JAVA_OPTS -Dskalli.persistence.javax.persistence.jdbc.driver=org.apache.derby.jdbc.EmbeddedDriver"
#JAVA_OPTS="$JAVA_OPTS -Dskalli.persistence.javax.persistence.jdbc.url=jdbc:derby:/skalli/SkalliDB;create=true"
#JAVA_OPTS="$JAVA_OPTS -Dskalli.persistence.javax.persistence.jdbc.user=skalli"
#JAVA_OPTS="$JAVA_OPTS -Dskalli.persistence.javax.persistence.jdbc.password=skalli"

# start from one directory above as path references are relative to SKALLI_HOME
cd ..
java $JAVA_OPTS -jar plugins/org.eclipse.osgi_*.jar $SKALLI_OPTS
