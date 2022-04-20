package im.vector.app.eachchat.mqtt;


import java.io.IOException;

import im.vector.app.eachchat.net.NetWorkManager;
import im.vector.app.eachchat.contact.api.UserService;

/**
 * Created by zhouguanjie on 2019/8/23.
 */
public class ApiService {

    public static UserService getUserService() throws IOException {
        return NetWorkManager.getInstance().getRetrofit().create(UserService.class);
    }

}
