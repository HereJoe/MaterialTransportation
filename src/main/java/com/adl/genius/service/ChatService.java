package com.adl.genius.service;

import com.adl.genius.entity.UserContext;
import com.adl.genius.entity.vo.*;

import java.util.List;

public interface ChatService {

    List<DialogVO> dialogs(UserContext userContext);

    List<QAVO> dialog(UserContext userContext, int dialogId);

    Void deleteDialog(UserContext userContext, int dialogId);

    Void title(UserContext userContext, DialogTitleVO dialogTitleVO);

    DialogOutputVO chat(UserContext userContext, DialogInputVO dialogInputVO);
}
