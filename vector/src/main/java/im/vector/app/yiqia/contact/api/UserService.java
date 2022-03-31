package im.vector.app.yiqia.contact.api;

import java.util.List;
import java.util.Map;

import im.vector.app.eachchat.bean.PNSInput;
import im.vector.app.eachchat.bean.PNSOutput;
import im.vector.app.eachchat.bean.Response;
import im.vector.app.yiqia.contact.api.bean.AvatarUploadBean;
import im.vector.app.yiqia.contact.api.bean.BindDeviceInput;
import im.vector.app.yiqia.contact.api.bean.Department;
import im.vector.app.yiqia.contact.api.bean.DepartmentInput;
import im.vector.app.yiqia.contact.api.bean.LoginInput;
import im.vector.app.yiqia.contact.data.IncrementUpdateInput;
import im.vector.app.yiqia.contact.data.UpdateGroupValue;
import im.vector.app.yiqia.contact.data.User;
import im.vector.app.yiqia.contact.data.UserEnterpriseBean;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by zhouguanjie on 2019/8/23.
 */
public interface UserService {
    @POST("/api/services/auth/v1/login")
    Observable<retrofit2.Response<Response<Object, Object>>> login(@Body LoginInput model);

    @POST("/api/services/auth/v1/init")
    Call<Response<Object, Object>> bindDevice(@Body BindDeviceInput model);

    @POST("/api/services/auth/v1/pns")
    Observable<Response<PNSOutput, Object>> getPNS(@Body PNSInput model);

    @POST("/api/services/auth/v1/logout")
    Observable<Response> logout();

    @POST("/api/apps/org/v1/departments")
    Observable<retrofit2.Response<Response<UpdateGroupValue, List<Department>>>> getDepartments(@Body DepartmentInput departmentInput);

    @POST("/api/apps/org/v1/user/avatar")
    Observable<retrofit2.Response<Response<AvatarUploadBean, Object>>> uploadAvatar(@Body RequestBody body);

    @POST("/api/apps/org/v1/increment")
    Observable<retrofit2.Response<Response<UpdateGroupValue, List<User>>>> updateUsers(@Body IncrementUpdateInput input);

    @POST("/api/apps/org/v1/increment")
    Observable<retrofit2.Response<Response<UpdateGroupValue, List<Department>>>> updateDepartments(@Body IncrementUpdateInput input);

    @POST("/api/services/auth/v1/user/password")
    Observable<Response> resetPassword(@Body Map<String, String> input);

    @GET("/api/apps/org/v1/setting/enterprise")
    Observable<Response<UserEnterpriseBean, Object>> getUserEnterprise();

//    @GET("/api/apps/org/v1/log/login")
//    Observable<Response<Object, List<LatestDevices>>> getLoginLogs();

}
