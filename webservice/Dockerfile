FROM 		docker-registry.htrc.indiana.edu/tomcat9

MAINTAINER Data to Insight Center <d2i@indiana.edu>

USER root

WORKDIR /opt

WORKDIR /opt/tomcat/webapps/
ADD target/sloan-ws*.war /opt/tomcat/webapps/sloan-ws.war
RUN unzip -qq sloan-ws.war -d sloan-ws

# Create directory to keep DC API configuration files
RUN mkdir -p /etc/htrc/dcapi

WORKDIR /opt/tomcat

RUN chown -R tomcat webapps/

USER tomcat