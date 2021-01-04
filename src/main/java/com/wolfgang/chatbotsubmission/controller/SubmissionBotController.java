package com.wolfgang.chatbotsubmission.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.ReplyEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.wolfgang.chatbotsubmission.model.*;
import com.wolfgang.chatbotsubmission.service.BotService;
import com.wolfgang.chatbotsubmission.service.BotTemplate;
import com.wolfgang.chatbotsubmission.service.DBService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@RestController
public class SubmissionBotController {

    @Autowired
    @Qualifier("lineSignatureValidator")
    private LineSignatureValidator lineSignatureValidator;

    @Autowired
    private BotService botService;

    @Autowired
    private BotTemplate botTemplate;

    @Autowired
    private DBService dbService;

    private UserProfileResponse sender = null;
    private ListingEvents listingEvents = null;
    private ListingGames listingGames = null;

    @RequestMapping(value="/webhook", method= RequestMethod.POST)
    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String xLineSignature,
            @RequestBody String eventsPayload)
    {
        try {
            if (!lineSignatureValidator.validateSignature(eventsPayload.getBytes(), xLineSignature)) {
                throw new RuntimeException("Invalid Signature Validation");
            }

            System.out.println(eventsPayload);
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            LineEventsModel eventsModel = objectMapper.readValue(eventsPayload, LineEventsModel.class);

            eventsModel.getEvents().forEach((event)->{
                if (event instanceof JoinEvent || event instanceof FollowEvent) {
                    String replyToken = ((ReplyEvent) event).getReplyToken();
                    handleJointOrFollowEvent(replyToken, event.getSource());
                } else if (event instanceof MessageEvent) {
                    handleMessageEvent((MessageEvent) event);
                }
            });

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path = "games", produces = "application/json")
    public void getGames(@RequestHeader HttpHeaders header) throws JsonProcessingException {
        try {
            getListingGamesData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void greetingMessage(String replyToken, Source source, String additionalMessage) {
        if(sender == null) {
            String senderId = source.getSenderId();
            sender          = botService.getProfile(senderId);
        }

        TemplateMessage greetingMessage = botTemplate.greetingMessage(source, sender);

        if (additionalMessage != null) {
            List<Message> messages = new ArrayList<>();
            messages.add(new TextMessage(additionalMessage));
            messages.add(greetingMessage);
            botService.reply(replyToken, messages);
        } else {
            botService.reply(replyToken, greetingMessage);
        }
    }

    private void handleJointOrFollowEvent(String replyToken, Source source) {
        greetingMessage(replyToken, source, null);
    }

    private void handleMessageEvent(MessageEvent event) {
        String replyToken      = event.getReplyToken();
        MessageContent content = event.getMessage();
        Source source          = event.getSource();
        String senderId        = source.getSenderId();
        sender                 = botService.getProfile(senderId);

        if(content instanceof TextMessageContent) {
            handleTextMessage(replyToken, (TextMessageContent) content, source);
        } else {
            greetingMessage(replyToken, source, null);
        }
    }

    private void handleTextMessage(String replyToken, TextMessageContent content, Source source) {
        if (source instanceof GroupSource) {
            handleGroupChats(replyToken, content.getText(), ((GroupSource) source).getGroupId());
        } else if (source instanceof RoomSource) {
            handleRoomChats(replyToken, content.getText(), ((RoomSource) source).getRoomId());
        } else if(source instanceof UserSource) {
            handleOneOnOneChats(replyToken, content.getText());
        } else {
            botService.replyText(replyToken,"Unknown Message Source!");
        }
    }

    private void handleGroupChats(String replyToken, String textMessage, String groupId) {
        String msgText = textMessage.toLowerCase();
        if (msgText.contains("bot leave")) {
            if (sender == null) {
                botService.replyText(replyToken, "Hi, silahkan tambahkan Wolfg Chatbot terlebih dahulu!");
            } else {
                botService.leaveGroup(groupId);
            }
        } else if (msgText.contains("id")
                || msgText.contains("find")
                || msgText.contains("join")
                || msgText.contains("teman")
        ) {
            processText(replyToken, textMessage);
        } else if (msgText.contains("cek game")) {
            showCarouselEvents(replyToken);
        } else if (msgText.contains("deskripsi")) {
            showEventSummary(replyToken, textMessage);
        } else {
            handleFallbackMessage(replyToken, new GroupSource(groupId, sender.getUserId()));
        }
    }

    private void handleRoomChats(String replyToken, String textMessage, String roomId) {
        String msgText = textMessage.toLowerCase();
        if (msgText.contains("bot leave")) {
            if (sender == null) {
                botService.replyText(replyToken, "Hi, silahkan tambahkan Wolfg Chatbot terlebih dahulu!");
            } else {
                botService.leaveRoom(roomId);
            }
        } else if (msgText.contains("id")
                || msgText.contains("find")
                || msgText.contains("join")
                || msgText.contains("teman")
        ) {
            processText(replyToken, msgText);
        } else if (msgText.contains("cek game")) {
            showCarouselEvents(replyToken);
        } else if (msgText.contains("deskripsi")) {
            showEventSummary(replyToken, textMessage);
        } else {
            handleFallbackMessage(replyToken, new RoomSource(roomId, sender.getUserId()));
        }
    }

    private void handleOneOnOneChats(String replyToken, String textMessage) {
        String msgText = textMessage.toLowerCase();
        if (msgText.contains("id")
                || msgText.contains("find")
                || msgText.contains("join")
                || msgText.contains("teman")
        ) {
            processText(replyToken, msgText);
        } else if (msgText.contains("cek game")) {
            showCarouselEvents(replyToken);
        } else if (msgText.contains("deskripsi")) {
            showEventSummary(replyToken, textMessage);
        } else {
            handleFallbackMessage(replyToken, new UserSource(sender.getUserId()));
        }
    }

    private void handleFallbackMessage(String replyToken, Source source) {
        greetingMessage(replyToken, source, "Heyho " + sender.getDisplayName() + ", pencarianmu tidak dapat ditemukan. Silahkan ikuti petunjuk ya ");
    }

    private void processText(String replyToken, String messageText) {
        String[] words = messageText.trim().split("\\s+");
        String intent  = words[0];

        if(intent.equalsIgnoreCase("id")) {
            handleRegisteringUser(replyToken, words);
        } else if (intent.equalsIgnoreCase("join")) {
            handleJoinWolfgEvent(replyToken, words);
        } else if (intent.equalsIgnoreCase("teman")) {
            handleShowFriend(replyToken, words);
        }
    }

    private void handleRegisteringUser(String replyToken, String[] words) {
        String registerMessage = null;
        String target=words.length > 1 ? words[1] : "";

        if (target.length()<=3){
            registerMessage = "Dibutuhkan lebih dari 3 karakter untuk mencari user tersebut";
        } else {
            String lineId = target.substring(target.indexOf("@") + 1);
            if(sender != null) {
                if (!sender.getDisplayName().isEmpty() && (lineId.length() > 0)) {
                    if (dbService.regLineID(sender.getUserId(), lineId, sender.getDisplayName()) != 0) {
                        showCarouselEvents(replyToken, "Yoo kamu berhasil terdaftar di Wolfg Project. Silahkan cek daftar event yang tersedia, semoga ada yg menarik minatmu "+sender.getDisplayName()+" :");
                    } else {
                        userNotFoundFallback(replyToken, "Upss maaf proses pendaftaranmu tidak berhasil! Ayo ikuti petunjuk berikut ini:");
                    }
                } else {
                    userNotFoundFallback(replyToken, "Datamu tidak dapat ditemukan. Silahkan tambahkan Wolfg Chatbot terlebih dahulu!");
                }
            } else {
                userNotFoundFallback(replyToken, "Hi, silahkan tambahkan Wolfg Chatbot terlebih dahulu!");
            }
        }
    }

    private void handleJoinWolfgEvent(String replyToken, String[] words) {
        String target       = words.length > 2 ? words[2] : "";
        String eventId      = target.substring(target.indexOf("#") + 1);
        String senderId     = sender.getUserId();
        String senderName   = sender.getDisplayName();
        String lineId       = dbService.findUser(senderId);

        int joinStatus = dbService.joinEvent(eventId, senderId, lineId, senderName);

        if (joinStatus == -1) {
            TemplateMessage buttonsTemplate = botTemplate.createButton(
                    "Heyho, "+senderName+" sepertinya kamu sudah tergabung di event ini",
                    "Lihat Teman",
                    "teman #" + eventId
            );
            botService.reply(replyToken, buttonsTemplate);
            return;
        }

        if (joinStatus == 1) {
            TemplateMessage buttonsTemplate = botTemplate.createButton(
                    "Yoo kamu berhasil mendaftar di event ini! Mari cek peserta lain, mungkin ada yang kamu kenal",
                    "Lihat Teman",
                    "teman #" + eventId
            );
            botService.reply(replyToken, buttonsTemplate);
            broadcastNewFriendJoined(eventId, senderId);
            return;
        }

        botService.replyText(replyToken, "yah, kamu gagal bergabung event");
    }

    private void handleShowFriend(String replyToken, String[] words) {
        String target       = StringUtils.join(words, " ");
        String eventId      = target.substring(target.indexOf("#") + 1).trim();

        List<JointEvents> jointEvents = dbService.getJoinedEvent(eventId);

        if (jointEvents.size() > 0) {
            List<String> friendList = jointEvents.stream()
                    .map((jointEvent) -> String.format(
                            "Display Name: %s\nLINE ID: %s\n",
                            jointEvent.display_name,
                            "http://line.me/ti/p/~" + jointEvent.line_id
                    ))
                    .collect(Collectors.toList());

            String replyText  = "Daftar teman di event #" + eventId + ":\n\n";
            replyText += StringUtils.join(friendList, "\n\n");

            botService.replyText(replyToken, replyText);
        } else {
            botService.replyText(replyToken, "Event tidak ditemukan");
        }
    }

    private void userNotFoundFallback(String replyToken) {
        userNotFoundFallback(replyToken, null);
    }

    private void userNotFoundFallback(String replyToken, String additionalInfo) {
        List<String> messages = new ArrayList<String>();

        if(additionalInfo != null) messages.add(additionalInfo);
        messages.add("Nah sebelum mencari lebih lanjut, beritahukan ID-mu agar temanmu yg lain tau jika kamu bergabung disini (pake \'id @\' ya)");
        messages.add("Contoh: id @wolfg");

        botService.replyText(replyToken, messages.toArray(new String[messages.size()]));
        return;
    }

    private void showCarouselEvents(String replyToken) {
        showCarouselEvents(replyToken, null);
    }

    private void showCarouselEvents(String replyToken, String additionalInfo) {
        String userFound = dbService.findUser(sender.getUserId());

        if(userFound == null) {
            userNotFoundFallback(replyToken);
        }

        if((listingGames == null) || (listingGames.getResults().size() < 1)){
//            getListingEventsData();
            getListingGamesData();
        }

        TemplateMessage carouselEvents = botTemplate.carouselEvents(listingGames);

        if (additionalInfo == null) {
            botService.reply(replyToken, carouselEvents);
            return;
        }

        List<Message> messageList = new ArrayList<>();
        messageList.add(new TextMessage(additionalInfo));
        messageList.add(carouselEvents);
        botService.reply(replyToken, messageList);
    }

    private void getListingEventsData() {
        // Act as client with GET method
        String URI = "https://www.dicoding.com/public/api/events?limit=10&active=-1";
        System.out.println("URI: " +  URI);

        try (CloseableHttpAsyncClient client = HttpAsyncClients.createDefault()) {
            client.start();
            //Use HTTP Get to retrieve data
            HttpGet get = new HttpGet(URI);

            Future<HttpResponse> future = client.execute(get, null);
            HttpResponse responseGet = future.get();
            System.out.println("HTTP executed");
            System.out.println("HTTP Status of response: " + responseGet.getStatusLine().getStatusCode());

            // Get the response from the GET request
            InputStream inputStream = responseGet.getEntity().getContent();
            String encoding = StandardCharsets.UTF_8.name();
            String jsonResponse = IOUtils.toString(inputStream, encoding);

            System.out.println("Got result");
            System.out.println(jsonResponse);

            ObjectMapper objectMapper = new ObjectMapper();
            listingEvents = objectMapper.readValue(jsonResponse, ListingEvents.class);
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getListingGamesData() {
        String URI = "https://api.rawg.io/api/games?page=1&page_size=10";
        System.out.println("URI: " +  URI);

        try (CloseableHttpAsyncClient client = HttpAsyncClients.createDefault()) {
            client.start();
            //Use HTTP Get to retrieve data
            HttpGet get = new HttpGet(URI);

            Future<HttpResponse> future = client.execute(get, null);
            HttpResponse responseGet = future.get();
            System.out.println("HTTP executed");
            System.out.println("HTTP Status of response: " + responseGet.getStatusLine().getStatusCode());

            // Get the response from the GET request
            InputStream inputStream = responseGet.getEntity().getContent();
            String encoding = StandardCharsets.UTF_8.name();
            String jsonResponse = IOUtils.toString(inputStream, encoding);

            System.out.println("Got result");
            System.out.println(jsonResponse);

            ObjectMapper objectMapper = new ObjectMapper();
            listingGames = objectMapper.readValue(jsonResponse, ListingGames.class);
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void broadcastNewFriendJoined(String eventId, String newFriendId) {
        List<String> listIds;
        List<JointEvents> jointEvents = dbService.getJoinedEvent(eventId);

        listIds = jointEvents.stream()
                .filter(jointEvent -> !jointEvent.user_id.equals(newFriendId))
                .map((jointEvent) -> jointEvent.user_id)
                .collect(Collectors.toList());

        Set<String> stringSet = new HashSet<String>(listIds);
        String msg = "Fren, ada teman baru telah bergabung di event " + eventId+", ayo cek. Siapa tau kalian saling kenal";
        TemplateMessage buttonsTemplate = botTemplate.createButton(msg, "Lihat Teman", "teman #" + eventId);
        botService.multicast(stringSet, buttonsTemplate);
    }

    private void showEventSummary(String replyToken, String userTxt) {
        try {
            if (listingGames == null) {
//                getListingEventsData();
                getListingGamesData();
            }

            int eventIndex = Integer.parseInt(String.valueOf(userTxt.charAt(1))) - 1;
            ModelGames eventData = listingGames.getResults().get(eventIndex);

            ClassLoader classLoader = getClass().getClassLoader();
            String encoding         = StandardCharsets.UTF_8.name();
            String flexTemplate     = IOUtils.toString(classLoader.getResourceAsStream("flex_event.json"), encoding);

            flexTemplate = String.format(flexTemplate,
                    botTemplate.escape(eventData.getBackground_image()),
                    botTemplate.escape(eventData.getName()),
                    botTemplate.escape(eventData.getSlug()),
                    botTemplate.br2nl(eventData.getReleased()),
                    eventData.getRating_top(),
                    eventData.getReviews_text_count(),
                    eventData.getMetacritic(),
                    eventData.getPlaytime(),
                    eventData.getRating(),
                    botTemplate.escape("https://rawg.io/games/"+eventData.getSlug()),
                    eventData.getId()
            );

            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            FlexContainer flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);
            botService.reply(replyToken, new FlexMessage("Wolfg Project", flexContainer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
