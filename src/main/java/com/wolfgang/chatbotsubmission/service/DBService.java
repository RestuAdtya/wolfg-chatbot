package com.wolfgang.chatbotsubmission.service;

import com.wolfgang.chatbotsubmission.database.Dao;
import com.wolfgang.chatbotsubmission.model.JointEvents;
import com.wolfgang.chatbotsubmission.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DBService {

    @Autowired
    private Dao mDao;

    public int regLineID(String aUserId, String aLineId, String aDisplayName){
        if(findUser(aUserId) == null)
        {
            return mDao.registerLineId(aUserId, aLineId, aDisplayName);
        }

        return -1;
    }

    public String findUser(String aUserId){
        List<User> self=mDao.getByUserId("%"+aUserId+"%");

        if(self.size() > 0)
        {
            return self.get(0).line_id;
        }

        return null;
    }

    public int joinEvent(String eventID, String userId, String lineID, String displayname){
        JointEvents joinedEvent = isUserJoinedEvent(eventID, userId);

        if(joinedEvent == null) {
            return mDao.joinEvent(eventID, userId, lineID, displayname);
        }

        return -1;
    }

    private JointEvents isUserJoinedEvent(String eventID, String userID){
        List<JointEvents> result = mDao.getByJoin(eventID, userID);

        if (result.size() > 0) {
            return result.get(0);
        }

        return null;
    }

    public List<JointEvents> getJoinedEvent(String eventID){
        return mDao.getByEventId(eventID);
    }
}
