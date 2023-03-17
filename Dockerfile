ARG APP_INSIGHTS_AGENT_VERSION=3.4.9
ARG PLATFORM=""
FROM hmctspublic.azurecr.io/base/java${PLATFORM}:17-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/rd-profile-sync.jar /opt/app/

EXPOSE 8092

CMD [ "rd-profile-sync.jar" ]