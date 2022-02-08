package com.geekbrains.cloud.jan;

import com.geekbrains.cloud.jan.model.CloudMessage;

public interface MessageProcessor {

    void processMessage(CloudMessage msg);

}
