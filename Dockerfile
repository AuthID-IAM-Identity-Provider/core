# Use the official Oracle image as a base
FROM container-registry.oracle.com/database/free:latest

# Patch the broken script inside the image to fix the startup bug
RUN sed -i '242s|source /opt/oracle/|source /opt/oracle/oraenv|' /opt/oracle/runOracle.sh.orig || true