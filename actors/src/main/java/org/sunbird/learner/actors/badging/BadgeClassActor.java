package org.sunbird.learner.actors.badging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.ActorOperations;
import org.sunbird.common.models.util.HttpUtil;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.request.ExecutionContext;
import org.sunbird.common.request.Request;
import org.sunbird.learner.actors.AbstractBaseActor;
import org.sunbird.learner.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BadgeClassActor extends AbstractBaseActor {
    @Override
    public void onReceive(Object message) throws Throwable {
        ProjectLogger.log("BadgeClassActor onReceive called");

        if (message instanceof Request) {
            try {
                Request actorMessage = (Request) message;
                Util.initializeContext(actorMessage, JsonKey.USER);
                ExecutionContext.setRequestId(actorMessage.getRequestId());

                if (actorMessage.getOperation().equalsIgnoreCase(ActorOperations.CREATE_BADGE_CLASS.getValue())) {
                    createBadgeClass(actorMessage);
                } else if (actorMessage.getOperation().equalsIgnoreCase(ActorOperations.GET_BADGE_CLASS.getValue())) {
                    getBadgeClass(actorMessage);
                } else if (actorMessage.getOperation().equalsIgnoreCase(ActorOperations.LIST_BADGE_CLASS.getValue())) {
                    listBadgeClass();
                } else if (actorMessage.getOperation().equalsIgnoreCase(ActorOperations.DELETE_BADGE_CLASS.getValue())) {
                    deleteBadgeClass(actorMessage);
                } else {
                    onReceiveUnsupportedOperation("BadgeClassActor");
                }
            } catch (Exception exception) {
                onReceiveException("BadgeClassActor", exception);
            }
        } else {
            onReceiveUnsupportedMessage("BadgeClassActor");
        }
    }

    private void createBadgeClass(Request actorMessage) {
        ProjectLogger.log("createBadgeClass called");

        try {
            Map<String, Object> requestData = actorMessage.getRequest();

            String issuerSlug = (String) requestData.get(JsonKey.ISSUER_SLUG);

            Map<String, String> formParams = (Map<String, String>) requestData.get(JsonKey.FORM_PARAMS);
            Map<String, byte[]> fileParams = (Map<String, byte[]>) requestData.get(JsonKey.FILE_PARAMS);

            Map<String, String> headers = BadgingUtil.getBadgrHeaders();

            String badgrResponseStr = HttpUtil.postFormData(formParams, fileParams, headers, BadgingUtil.getBadgeClassUrl(issuerSlug));

            Response response = new Response();
            ObjectMapper mapper = new ObjectMapper();

            Map<String , Object> badgrResponseMap  = mapper.readValue(badgrResponseStr, HashMap.class);
            response.putAll(badgrResponseMap);

            sender().tell(response, self());
        } catch (IOException e) {
            ProjectLogger.log("createBadgeClass: exception = ", e);

            sender().tell(e, self());
        }
    }

    private void getBadgeClass(Request actorMessage) {
        ProjectLogger.log("getBadgeClass called");

        try {
            Map<String, Object> requestData = actorMessage.getRequest();

            String issuerSlug = (String) requestData.get(JsonKey.ISSUER_SLUG);
            String badgeClassSlug = (String) requestData.get(JsonKey.BADGE_CLASS_SLUG);

            Map<String, String> headers = BadgingUtil.getBadgrHeaders();
            String badgrUrl = BadgingUtil.getBadgeClassUrl(issuerSlug, badgeClassSlug);

            String badgrResponseStr = HttpUtil.sendGetRequest(badgrUrl, headers);

            Response response = new Response();
            ObjectMapper mapper = new ObjectMapper();

            Map<String , Object> badgrResponseMap  = mapper.readValue(badgrResponseStr, HashMap.class);
            response.putAll(badgrResponseMap);

            sender().tell(response, self());
        } catch (IOException e) {
            ProjectLogger.log("getBadgeClass: exception = ", e);

            sender().tell(e, self());
        }
    }

    private void listBadgeClass() {
        ProjectLogger.log("listBadgeClass called");

        Response response = new Response();
        response.put(JsonKey.RESPONSE, JsonKey.SUCCESS);
        sender().tell(response, self());
    }

    private void deleteBadgeClass(Request actorMessage) {
        ProjectLogger.log("deleteBadgeClass called");

        try {
            Map<String, Object> requestData = actorMessage.getRequest();

            String issuerSlug = (String) requestData.get(JsonKey.ISSUER_SLUG);
            String badgeClassSlug = (String) requestData.get(JsonKey.BADGE_CLASS_SLUG);

            Map<String, String> headers = BadgingUtil.getBadgrHeaders();
            String badgrUrl = BadgingUtil.getBadgeClassUrl(issuerSlug, badgeClassSlug);

            String badgrResponseStr = HttpUtil.sendDeleteRequest(headers, badgrUrl);

            Response response = new Response();
            response.put(JsonKey.MESSAGE, badgrResponseStr.replaceAll("^\"|\"$", ""));

            sender().tell(response, self());
        } catch (IOException e) {
            ProjectLogger.log("deleteBadgeClass: exception = ", e);

            sender().tell(e, self());
        }
    }
}
