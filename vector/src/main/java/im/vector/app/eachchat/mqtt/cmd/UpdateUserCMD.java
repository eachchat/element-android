package im.vector.app.eachchat.mqtt.cmd;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import im.vector.app.eachchat.contact.data.UpdateGroupValue;
import im.vector.app.eachchat.contact.event.MQTTEvent;
import im.vector.app.eachchat.mqtt.MessageConstant;
import im.vector.app.eachchat.mqtt.data.MQTTMsgBean;

/**
 * Created by zhouguanjie on 2019/10/31.
 */
public class UpdateUserCMD extends AbstractCMD {
    @Override
    public void execute(MQTTMsgBean bean) {
        if (bean == null || bean.getValue() == null) {
            return;
        }
        UpdateGroupValue value = new Gson().fromJson(bean.getValue().toString(), UpdateGroupValue.class);
        if (value == null) {
            return;
        }
        EventBus.getDefault().post(new MQTTEvent(MessageConstant.CMD_UPDATE_USER, String.valueOf(value.getUpdateTime()), 0));
    }

    @NonNull
    @Override
    public String getCMD() {
        return MessageConstant.CMD_UPDATE_USER;
    }
}
