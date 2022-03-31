package im.vector.app.yiqia.mqtt.cmd;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import im.vector.app.yiqia.contact.event.MQTTEvent;
import im.vector.app.yiqia.mqtt.MessageConstant;
import im.vector.app.yiqia.mqtt.data.MQTTMsgBean;

/**
 * Created by chengww on 1/15/21
 *
 * @author chengww
 */
public class UpdateContactRoomCMD extends AbstractCMD {

    @Override
    public void execute(MQTTMsgBean bean) {
        EventBus.getDefault().post(new MQTTEvent(MessageConstant.CMD_UPDATE_CONTACT_ROOM, "0", 0));
    }

    @NonNull
    @Override
    public String getCMD() {
        return MessageConstant.CMD_UPDATE_CONTACT_ROOM;
    }
}