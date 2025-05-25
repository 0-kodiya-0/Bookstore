# Dockerfile
FROM tomcat:9.0.100-jdk8-corretto

# Build arguments for version tracking
ARG BUILD_DATE
ARG VCS_REF
ARG VERSION

# Set metadata with version information
LABEL maintainer="Sanithu Jayakody santihujayafiverr@gmail.com"
LABEL application="Bookstore"
LABEL version="${VERSION}"
LABEL org.opencontainers.image.version="${VERSION}"
LABEL org.opencontainers.image.created="${BUILD_DATE}"
LABEL org.opencontainers.image.revision="${VCS_REF}"
LABEL org.opencontainers.image.title="Bookstore Application"
LABEL org.opencontainers.image.description="Java Bookstore Web Application"

# Install curl for health checks (using yum since this is Amazon Linux)
RUN yum update -y && yum install -y curl && yum clean all

# Remove default Tomcat webapps for security and performance
RUN rm -rf /usr/local/tomcat/webapps/ROOT \
           /usr/local/tomcat/webapps/docs \
           /usr/local/tomcat/webapps/examples \
           /usr/local/tomcat/webapps/host-manager \
           /usr/local/tomcat/webapps/manager

# Copy your WAR file into the container
COPY bookstore-1.0.0.war /usr/local/tomcat/webapps/bookstore.war

# Set proper file permissions (root owns files, which is fine for containers)
RUN chmod 644 /usr/local/tomcat/webapps/bookstore.war

# Configure JVM settings optimized for containers
ENV JAVA_OPTS="-Djava.awt.headless=true \
               -Djava.security.egd=file:/dev/./urandom \
               -Xms256m \
               -Xmx512m \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC"

# Set Tomcat-specific options
ENV CATALINA_OPTS="-server"

# Expose port 8080
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s \
            --timeout=10s \
            --start-period=60s \
            --retries=3 \
            CMD curl -f http://localhost:8080/bookstore/ || exit 1

# Start Tomcat when the container starts
CMD ["catalina.sh", "run"]