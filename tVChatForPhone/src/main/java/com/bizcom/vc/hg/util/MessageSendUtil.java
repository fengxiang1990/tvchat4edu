package com.bizcom.vc.hg.util;

import android.content.Context;

import com.bizcom.db.provider.ChatMessageProvider;
import com.bizcom.request.V2ChatRequest;
import com.bizcom.util.MessageUtil;
import com.bizcom.vo.User;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.meesage.VMessageAbstractItem;
import com.config.V2GlobalConstants;

public class MessageSendUtil {

    private Context context;
    private V2ChatRequest mChat = new V2ChatRequest();

    public MessageSendUtil(Context context) {
        this.context = context;
    }

    /**
     * send chat message to remote
     */
    public boolean sendMessageToRemote(String centent, User toUser) {
        VMessage vm = MessageUtil.buildChatMessage(context, centent, V2GlobalConstants.GROUP_TYPE_USER, 0, toUser);
        if (vm == null)
            return false;
        // Save message
        vm.setmXmlDatas(vm.toXml());
        vm.setState(VMessageAbstractItem.TRANS_TRANSING);
        ChatMessageProvider.saveChatMessage(vm);
        ChatMessageProvider.saveFileVMessage(vm);
        ChatMessageProvider.saveBinaryVMessage(vm);
        mChat.requestSendChatMessage(vm);
        return true;
    }

}
