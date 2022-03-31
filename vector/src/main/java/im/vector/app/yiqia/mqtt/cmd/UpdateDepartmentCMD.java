package im.vector.app.yiqia.mqtt.cmd;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import im.vector.app.yiqia.contact.data.UpdateGroupValue;
import im.vector.app.yiqia.contact.event.MQTTEvent;
import im.vector.app.yiqia.mqtt.MessageConstant;
import im.vector.app.yiqia.mqtt.data.MQTTMsgBean;

/**
 * Created by zhouguanjie on 2019/10/31.
 */
public class UpdateDepartmentCMD extends AbstractCMD {
    @Override
    public void execute(MQTTMsgBean bean) {
        if (bean == null || bean.getValue() == null) {
            return;
        }
        UpdateGroupValue value = new Gson().fromJson(bean.getValue().toString(), UpdateGroupValue.class);
        if (value == null) {
            return;
        }
        EventBus.getDefault().post(new MQTTEvent(MessageConstant.CMD_UPDATE_DEPARTMENT, String.valueOf(value.getUpdateTime()), 0));
    }

    @NonNull
    @Override
    public String getCMD() {
        return MessageConstant.CMD_UPDATE_DEPARTMENT;
    }
}
