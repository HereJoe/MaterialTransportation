package com.adl.genius.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.adl.genius.entity.Response;
import com.adl.genius.entity.UserContext;
import com.adl.genius.entity.po.Dialog;
import com.adl.genius.entity.po.QA;
import com.adl.genius.entity.vo.*;
import com.adl.genius.mapper.DialogMapper;
import com.adl.genius.mapper.QAMapper;
import com.adl.genius.mapstruct.DialogMapping;
import com.adl.genius.mapstruct.QAMapping;
import com.adl.genius.service.ChatService;
import com.adl.genius.service.retrofit.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private DialogMapper dialogMapper;
    @Autowired
    private QAMapper qaMapper;
    @Autowired
    private ModelService modelService;

    @Override
    public List<DialogVO> dialogs(UserContext userContext) {
        Wrapper<Dialog> wrapper = Wrappers.<Dialog>lambdaQuery().eq(Dialog::getUserId, userContext.getId());
        List<Dialog> dialogs = dialogMapper.selectList(wrapper);
        return dialogs.stream()
                .map(DialogMapping.M::dialog2DialogVO)
                .sorted((d1, d2) -> -d1.getLastActiveTime().compareTo(d2.getLastActiveTime()))
                .collect(Collectors.toList());
    }

    @Override
    public List<QAVO> dialog(UserContext userContext, int dialogId) {
        Wrapper<QA> wrapper = Wrappers.<QA>lambdaQuery()
                .eq(QA::getUserId, userContext.getId())
                .eq(QA::getDialogId, dialogId);
        List<QA> qaList = qaMapper.selectList(wrapper);
        return qaList.stream()
                .map(QAMapping.M::QA2QAVO)
                .sorted(Comparator.comparing(QAVO::getCreateTime))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void deleteDialog(UserContext userContext, int dialogId) {
        Wrapper<Dialog> dialogWrapper = Wrappers.<Dialog>lambdaQuery()
                .eq(Dialog::getId, dialogId)
                .eq(Dialog::getUserId, userContext.getId());
        Dialog dialog = dialogMapper.selectOne(dialogWrapper);
        if (dialog == null) {
            return null;
        }

        dialogMapper.deleteById(dialogId);
        Wrapper<QA> qaWrapper = Wrappers.<QA>lambdaQuery()
                .eq(QA::getDialogId, dialogId)
                .eq(QA::getUserId, userContext.getId());
        qaMapper.delete(qaWrapper);

        return null;
    }

    @Override
    public Void title(UserContext userContext, DialogTitleVO dialogTitleVO) {
        Wrapper<Dialog> wrapper = Wrappers.<Dialog>lambdaUpdate()
                .set(Dialog::getTitle, dialogTitleVO.getTitle())
                .eq(Dialog::getId, dialogTitleVO.getId())
                .eq(Dialog::getUserId, userContext.getId());
        dialogMapper.update(wrapper);
        return null;
    }

    @Override
    public DialogOutputVO chat(UserContext userContext, DialogInputVO dialogInputVO) {
        Dialog dialog;
        List<String[]> qaList;
        if (dialogInputVO.getDialogId() == null) {
            dialog = new Dialog();
            dialog.setTitle(generateTitle(dialogInputVO.getUserInput()));
            dialog.setUserId(userContext.getId());
            qaList = new ArrayList<>();
        } else {
            Wrapper<Dialog> dialogWrapper = Wrappers.<Dialog>lambdaQuery()
                    .eq(Dialog::getId, dialogInputVO.getDialogId())
                    .eq(Dialog::getUserId, userContext.getId());
            dialog = dialogMapper.selectOne(dialogWrapper);
            if (dialog == null) {
                throw new RuntimeException("Dialog doesn't exist.");
            }
            Wrapper<QA> wrapper = Wrappers.<QA>lambdaQuery()
                    .eq(QA::getUserId, userContext.getId())
                    .eq(QA::getDialogId, dialogInputVO.getDialogId());
            qaList = qaMapper.selectList(wrapper).stream().
                    sorted(Comparator.comparing(QA::getCreateTime))
                    .map(e -> new String[]{e.getUserInput(), e.getModelOutput()})
                    .collect(Collectors.toList());
        }

        qaList.add(new String[]{dialogInputVO.getUserInput(), ""});
        Response<String> response = modelService.chat(qaList);
        if (response.getCode() != Response.CODE_SUCCESS) {
            throw new RuntimeException(response.getMsg());
        }
        String modelOutput = response.getData();
        QA qa = new QA(null, dialog.getId(), userContext.getId(), dialogInputVO.getUserInput(), modelOutput, null);

        // transaction
        chatTransaction(userContext, dialogInputVO, dialog, qa);

        dialog = dialogMapper.selectById(dialog.getId());

        return new DialogOutputVO(DialogMapping.M.dialog2DialogVO(dialog), modelOutput);
    }

    @Transactional(rollbackFor = Exception.class)
    public void chatTransaction(UserContext userContext, DialogInputVO dialogInputVO, Dialog dialog, QA qa) {
        if (dialogInputVO.getDialogId() == null) {
            dialogMapper.insert(dialog);
        }
        qa.setDialogId(dialog.getId());
        qaMapper.insert(qa);

        Wrapper<Dialog> dialogWrapper = Wrappers.<Dialog>lambdaUpdate()
                .setSql("qa_count = qa_count + 1")
                .eq(Dialog::getId, dialog.getId())
                .eq(Dialog::getUserId, userContext.getId());
        dialogMapper.update(dialogWrapper);
    }

    private static String generateTitle(String userInput) {
        if (userInput.length() <= 30) {
            return userInput;
        }
        return userInput.substring(0, 30);
    }
}
