package im.vector.app.yiqia.mqtt;


import im.vector.app.eachchat.net.NetWorkManager;
import im.vector.app.yiqia.contact.api.UserService;

/**
 * Created by zhouguanjie on 2019/8/23.
 */
public class ApiService {

    public static UserService getUserService() {
        return NetWorkManager.getInstance().getRetrofit().create(UserService.class);
    }

}
