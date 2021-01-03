package com.wolfgang.chatbotsubmission.database;

import com.wolfgang.chatbotsubmission.model.JointEvents;
import com.wolfgang.chatbotsubmission.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class DaoImpl implements Dao {

    private final static String userTable = "tbl_user";
    private final static String sqlSelectAll = "SELECT id, user_id, line_id, display_name FROM "+userTable;
    private final static String sqlGetByUserId = sqlSelectAll + " WHERE LOWER(user_id) LIKE LOWER(?);";
    private final static String sqlRegister="INSERT INTO "+userTable+" (user_id, line_id, display_name) VALUES (?, ?, ?);";

    private final static String eventTable = "tbl_event";
    private final static String sqlSelectAllEvent = "SELECT id, event_id, user_id, line_id, display_name FROM "+eventTable;
    private final static String sqlJoinEvent = "INSERT INTO "+eventTable+ " (event_id, user_id, line_id, display_name) VALUES (?, ?, ?, ?);";
    private final static String sqlGetByEventId = sqlSelectAllEvent + " WHERE LOWER(event_id) LIKE LOWER(?);";
    private final static String sqlGetByJoin=sqlSelectAllEvent + " WHERE event_id = ? AND user_id = ?;";

    private JdbcTemplate mJdbc;

    private final static ResultSetExtractor<User> SINGLE_RS_EXTRACTOR = new ResultSetExtractor<User>() {
        @Override
        public User extractData(ResultSet resultSet) throws SQLException, DataAccessException {

            while (resultSet.next()) {
                User user = new User(
                     resultSet.getLong("id"),
                     resultSet.getString("user_id"),
                     resultSet.getString("line_id"),
                     resultSet.getString("display_name")
                );
            }
            return null;
        }
    };

    private final static ResultSetExtractor<List<User>> MULTIPLE_RS_EXTRACTOR=new ResultSetExtractor< List<User> >() {
        @Override
        public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<User> list=new Vector<User>();
            while(rs.next())
            {
                User p=new User(
                        rs.getLong("id"),
                        rs.getString("user_id"),
                        rs.getString("line_id"),
                        rs.getString("display_name"));
                list.add(p);
            }
            return list;
        }
    };

    private final static ResultSetExtractor<JointEvents> SINGLE_RS_EXTRACTOR_EVENT = new ResultSetExtractor<JointEvents>() {
        @Override
        public JointEvents extractData(ResultSet rs) throws SQLException, DataAccessException {

            while(rs.next())
            {
                JointEvents joinEvents = new JointEvents(
                        rs.getLong("id"),
                        rs.getString("event_id"),
                        rs.getString("user_id"),
                        rs.getString("line_id"),
                        rs.getString("display_name"));

                return joinEvents;
            }
            return null;
        }
    };

    private final static ResultSetExtractor< List<JointEvents> > MULTIPLE_RS_EXTRACTOR_EVENT=new ResultSetExtractor<List<JointEvents>>()
    {
        @Override
        public List<JointEvents> extractData(ResultSet resultSet) throws SQLException, DataAccessException
        {
            List<JointEvents> list=new Vector<>();
            while(resultSet.next())
            {
                JointEvents jointEvents = new JointEvents(
                        resultSet.getLong("id"),
                        resultSet.getString("event_id"),
                        resultSet.getString("user_id"),
                        resultSet.getString("line_id"),
                        resultSet.getString("display_name"));
                list.add(jointEvents);
            }
            return list;
        }
    };

    public DaoImpl(DataSource aDataSource) { mJdbc = new JdbcTemplate(aDataSource); }


    @Override
    public List<User> get() {
        return mJdbc.query(sqlSelectAll, MULTIPLE_RS_EXTRACTOR);
    }

    @Override
    public List<User> getByUserId(String aUserId) {
        return mJdbc.query(sqlGetByUserId, new Object[]{"%"+aUserId+"%"}, MULTIPLE_RS_EXTRACTOR);
    }

    @Override
    public int registerLineId(String aUserId, String aLineId, String aDisplayName) {
        return mJdbc.update(sqlRegister, new Object[]{aUserId, aLineId,  aDisplayName});
    }

    @Override
    public int joinEvent(String aEventId, String aUserId, String aLineId, String aDisplayName) {
        return mJdbc.update(sqlJoinEvent, new Object[]{aEventId, aUserId,  aLineId, aDisplayName});
    }

    @Override
    public List<JointEvents> getEvent() {
        return mJdbc.query(sqlSelectAllEvent, MULTIPLE_RS_EXTRACTOR_EVENT);
    }

    @Override
    public List<JointEvents> getByEventId(String aEventId) {
        return mJdbc.query(sqlGetByEventId, new Object[]{"%"+aEventId+"%"}, MULTIPLE_RS_EXTRACTOR_EVENT);
    }

    @Override
    public List<JointEvents> getByJoin(String aEventId, String aUserId) {
        return mJdbc.query(sqlGetByJoin, new Object[]{aEventId, aUserId}, MULTIPLE_RS_EXTRACTOR_EVENT);
    }
}
