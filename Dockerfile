FROM gradle:7-jdk17 as builder

WORKDIR /opt/onboarder/app-build/

# Copy gradle settings
COPY    build.gradle    ./
COPY    settings.gradle ./
COPY    docker/ ./

# Copy project sources
COPY    src ./src

# Build jar
RUN gradle bootJar

FROM openjdk:17-slim-bullseye

# Set unified timezone
ENV     TZ Europe/Moscow

# Copy built jar

COPY    --from=builder --chown=root /opt/onboarder/app-build/build/libs/*.jar /opt/onboarder/lib/app.jar

# Copy launcher and some additional files if needed
COPY    docker /opt/onboarder
CMD ["/opt/onboarder/run.sh"]