FROM tomcat
ADD target/grimoire.war /usr/local/tomcat/webapps
RUN /usr/local/tomcat/bin/startup.sh