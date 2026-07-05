package com.mdd.admin.service;

import java.util.List;

public interface IJPushJiGuangService {
    void sendNotice (String message, String title, List<String> registrationIds);
}
