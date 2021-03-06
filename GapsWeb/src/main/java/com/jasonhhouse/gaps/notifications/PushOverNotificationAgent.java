/*
 * Copyright 2020 Jason H House
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.jasonhhouse.gaps.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonhhouse.gaps.NotificationType;
import com.jasonhhouse.gaps.properties.PushOverProperties;
import com.jasonhhouse.gaps.service.FileIoService;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.jasonhhouse.gaps.notifications.NotificationStatus.FAILED_TO_PARSE_JSON;
import static com.jasonhhouse.gaps.notifications.NotificationStatus.SEND_MESSAGE;
import static com.jasonhhouse.gaps.notifications.NotificationStatus.TIMEOUT;

public final class PushOverNotificationAgent extends AbstractNotificationAgent<PushOverProperties> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PushOverNotificationAgent.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient client;

    public PushOverNotificationAgent(FileIoService fileIoService) {
        super(fileIoService);

        client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public int getId() {
        return 5;
    }

    @Override
    public String getName() {
        return "PushOver Notification Agent";
    }

    @Override
    public boolean sendMessage(NotificationType notificationType, String level, String title, String message) {
        LOGGER.info(SEND_MESSAGE, level, title, message);

        if (sendPrepMessage(notificationType)) {
            return false;
        }

        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("api.pushover.net")
                .addPathSegment("1")
                .addPathSegment("messages.json")
                .build();

        PushOver pushOver = new PushOver(t.getToken(), t.getUser(), t.getPriority(), t.getSound(), title, message, t.getRetry(), t.getExpire());

        String pushOverMessage;
        try {
            pushOverMessage = objectMapper.writeValueAsString(pushOver);
        } catch (JsonProcessingException e) {
            LOGGER.error(String.format(FAILED_TO_PARSE_JSON, getName()), e);
            return false;
        }

        LOGGER.info("pushOverMessage {}", pushOverMessage);
        RequestBody body = RequestBody.create(pushOverMessage, MediaType.get(org.springframework.http.MediaType.APPLICATION_JSON_VALUE));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                LOGGER.info("PushOver message sent via {}", url);
                return true;
            } else {
                LOGGER.error("Error with PushOver Url: {} Body returned {}", url, response.body());
                return false;
            }

        } catch (IOException e) {
            LOGGER.error(String.format("Error with PushOver Url: %s", url), e);
            return false;
        }
    }

    @Nullable
    @Override
    public PushOverProperties getNotificationProperties() {
        return fileIoService.readProperties().getPushOverProperties();
    }

    private static final class PushOver {

        private final String token;
        private final String user;
        private final Integer priority;
        private final String sound;
        private final String title;
        private final String message;
        private final Integer retry;
        private final Integer expire;

        public PushOver(String token, String user, Integer priority, String sound, String title, String message, Integer retry, Integer expire) {
            this.token = token;
            this.user = user;
            this.priority = priority;
            this.sound = sound;
            this.title = title;
            this.message = message;
            this.retry = retry;
            this.expire = expire;
        }

        public String getToken() {
            return token;
        }

        public String getUser() {
            return user;
        }

        public Integer getPriority() {
            return priority;
        }

        public String getSound() {
            return sound;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public Integer getRetry() {
            return retry;
        }

        public Integer getExpire() {
            return expire;
        }
    }
}
