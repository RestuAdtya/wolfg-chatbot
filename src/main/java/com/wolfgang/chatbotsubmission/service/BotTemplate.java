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
import com.wolfgang.chatbotsubmission.model.ListingGames;
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
        String message  = "Heyho %s! Selamat datang di Wolfg Project. Ini merupakan bot untuk berisikan informasi games terbaru ! Ayoo cek dan dapatkan informasi mengenai game favoritmu";
        String action   = "Cek game";

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

    public TemplateMessage carouselEvents(ListingGames listingGames) {
        int i;
        String image, owner, name, id, slug, released_date;
        CarouselColumn column;
        List<CarouselColumn> carouselColumn = new ArrayList<>();
        for (i = 0; i < listingGames.getResults().size(); i++){
            image = listingGames.getResults().get(i).getBackground_image();
            released_date = listingGames.getResults().get(i).getReleased();
            name = listingGames.getResults().get(i).getName();
            slug = listingGames.getResults().get(i).getSlug();

            id = String.valueOf(listingGames.getResults().get(i).getId());

            column = new CarouselColumn(image, name.substring(0, (name.length() < 40)?name.length():40), slug,
                    Arrays.asList(
                            new MessageAction("Deskripsi", "["+String.valueOf(i+1)+"]"+" Deskripsi : " + name+ "Dirilis : "+released_date),
                            new URIAction("Selengkapnya", "https://rawg.io/games/"+slug)
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
