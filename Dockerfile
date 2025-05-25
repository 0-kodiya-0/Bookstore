# Dockerfile
# Location: Put this file in your repository ROOT directory
# Purpose: Tells Docker how to build a container with your WAR file

# Use official Tomcat 9.0.100 with JDK 8
FROM tomcat:9.0.100-jdk8-corretto

# Set metadata for the image
LABEL maintainer="Your Name <your.email@example.com>"
LABEL application="Bookstore"
LABEL version="1.0.0"

# Remove default Tomcat webapps for security and performance
# These are sample apps that come with Tomcat that we don't need
RUN rm -rf /usr/local/tomcat/webapps/ROOT \
           /usr/local/tomcat/webapps/docs \
           /usr/local/tomcat/webapps/examples \
           /usr/local/tomcat/webapps/host-manager \
           /usr/local/tomcat/webapps/manager

# Copy your WAR file into the container
# GitHub Actions will download this file from your release and place it here
COPY bookstore-1.0.0.war /usr/local/tomcat/webapps/bookstore.war

# Set proper file ownership for security
# The tomcat user should own the WAR file
RUN chown tomcat:tomcat /usr/local/tomcat/webapps/bookstore.war && \
    chmod 644 /usr/local/tomcat/webapps/bookstore.war

# Configure JVM settings optimized for containers
# These settings help Java work better in containerized environments
ENV JAVA_OPTS="-Djava.awt.headless=true \
               -Djava.security.egd=file:/dev/./urandom \
               -Xms256m \
               -Xmx512m \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC"

# Set Tomcat-specific options
ENV CATALINA_OPTS="-server"

# Expose port 8080 (the port Tomcat runs on)
EXPOSE 8080

# Add health check so App Platform knows if your app is working
# This tries to access your app's home page every 30 seconds
HEALTHCHECK --interval=30s \
            --timeout=10s \
            --start-period=60s \
            --retries=3 \
            CMD curl -f http://localhost:8080/bookstore/ || exit 1

# Start Tomcat when the container starts
# This is the main command that runs your application
CMD ["catalina.sh", "run"]

# What this Dockerfile creates:
# 1. A Linux container with Java 8 installed
# 2. Tomcat 9.0.100 web server installed and configured
# 3. Your bookstore.war application deployed and ready to run
# 4. Optimized memory settings for your application
# 5. Health monitoring so App Platform can restart if needed
