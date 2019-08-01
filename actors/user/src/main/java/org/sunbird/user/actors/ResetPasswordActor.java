package org.sunbird.user.actors;

import org.sunbird.actor.core.BaseActor;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;
import org.sunbird.models.user.User;
import org.sunbird.services.sso.SSOManager;
import org.sunbird.services.sso.SSOServiceFactory;
import org.sunbird.user.dao.UserDao;
import org.sunbird.user.dao.impl.UserDaoImpl;

/** This actor process the request for reset password. */
@ActorConfig(
  tasks = {"resetPassword"},
  asyncTasks = {}
)
public class ResetPasswordActor extends BaseActor {

  private SSOManager ssoManager = SSOServiceFactory.getInstance();

  @Override
  public void onReceive(Request request) throws Throwable {
    String userId = (String) request.get(JsonKey.USER_ID);
    String password = (String) request.get(JsonKey.PASSWORD);
    resetPassword(userId, password);
  }

  private void resetPassword(String userId, String password) {
    ProjectLogger.log("ResetPasswordActor:resetPassword: method called.", LoggerEnum.INFO.name());
    UserDao userDao = new UserDaoImpl();
    User user = userDao.getUserById(userId);
    if (null != user) {
      if (ssoManager.updatePassword(userId, password)) {
        Response response = new Response();
        response.put(JsonKey.RESPONSE, JsonKey.SUCCESS);
        sender().tell(response, self());
      } else {
        ProjectLogger.log(
            "ResetPasswordActor:resetPassword : reset password failed", LoggerEnum.INFO.name());
        ProjectCommonException.throwServerErrorException(ResponseCode.SERVER_ERROR);
      }
    } else {
      ProjectCommonException.throwClientErrorException(ResponseCode.userNotFound);
    }
  }
}
