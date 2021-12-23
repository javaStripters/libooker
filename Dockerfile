FROM gradle:7-jdk16 as builder

WORKDIR /opt/libooker/app-build/

# Copy gradle settings
COPY    build.gradle    ./
COPY    settings.gradle ./
COPY    docker/ ./

# Copy project sources
COPY    src ./src

# Build jar
RUN gradle bootJar

FROM adoptopenjdk:16-openj9

# Set unified timezone
ENV     TZ Europe/Moscow

# Copy built jar

COPY    --from=builder --chown=root /opt/libooker/app-build/build/libs/*.jar /opt/libooker/lib/app.jar

# Copy launcher and some additional files if needed
COPY    docker /opt/libooker
CMD ["/opt/libooker/run.sh"]