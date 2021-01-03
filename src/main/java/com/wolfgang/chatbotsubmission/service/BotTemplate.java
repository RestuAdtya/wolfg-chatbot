package com.wolfgang.chatbotsubmission.service;

import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.wolfgang.chatbotsubmission.model.ListingEvents;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class BotTemplate {

    public TemplateMessage createButton(String message, String actionTitle, String actionText) {
        ButtonsTemplate buttonsTemplate = new ButtonsTemplate(
                null,
                null,
                message,
                Collections.singletonList(new MessageAction(actionTitle, actionText))
        );

        return new TemplateMessage(actionTitle, buttonsTemplate);
    }

    public TemplateMessage greetingMessage(Source source, UserProfileResponse sender) {
        String message  = "Heyho %s! Selamat datang di Wolfg Project. Ayoo bergabung dengan temanmu yang lain, pada event yg tersedia disini !";
        String action   = "Cek event";

        if (source instanceof GroupSource) {
            message = String.format(message, "Group");
        } else if (source instanceof RoomSource) {
            message = String.format(message, "Room");
        } else if(source instanceof UserSource) {
            message = String.format(message, sender.getDisplayName());
        } else {
            message = "Unknown Message Source!";
        }

        return createButton(message, action, action);
    }

    public TemplateMessage carouselEvents(ListingEvents listingEvents) {
        int i;
        String image, owner, name, id, link;
        CarouselColumn column;
        List<CarouselColumn> carouselColumn = new ArrayList<>();
        for (i = 0; i < listingEvents.getData().size(); i++){
            image = listingEvents.getData().get(i).getImagePath();
            owner = listingEvents.getData().get(i).getOwnerDisplayName();
            name = listingEvents.getData().get(i).getName();
            id = String.valueOf(listingEvents.getData().get(i).getId());
            link = listingEvents.getData().get(i).getLink();

            column = new CarouselColumn(image, name.substring(0, (name.length() < 40)?name.length():40), owner,
                    Arrays.asList(
                            new MessageAction("Deskripsi", "["+String.valueOf(i+1)+"]"+" Deskripsi : " + name),
                            new URIAction("Selengkapnya", link),
                            new MessageAction("Join Event", "join event #"+id)
                    )
            );

            carouselColumn.add(column);
        }

        CarouselTemplate carouselTemplate = new CarouselTemplate(carouselColumn);
        return new TemplateMessage("Hasil pencarianmu", carouselTemplate);
    }

    public String escape(String text) {
        return  StringEscapeUtils.escapeJson(text.trim());
    }

    public String br2nl(String html) {
        Document document = Jsoup.parse(html);
        document.select("br").append("\\n");
        document.select("p").prepend("\\n");
        String text = document.text()
                .replace("\\n\\n", "\\n")
                .replace("\\n", "\n");

        return StringEscapeUtils.escapeJson(text.trim());
    }
}
