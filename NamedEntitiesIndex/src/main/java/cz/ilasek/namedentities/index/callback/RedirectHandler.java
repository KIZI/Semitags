package cz.ilasek.namedentities.index.callback;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowCallbackHandler;

public class RedirectHandler implements RowCallbackHandler {
    
    private static final String ARTICLE_ID_COLUMN = "rd_from";
    private static final String TARGET_COLUMN = "rdc_title";

    private final Map<Long, String> redirects = new HashMap<Long, String>();

    @Override
    public void processRow(ResultSet rs) throws SQLException {
        while (rs.next()) {
            redirects.put(rs.getLong(ARTICLE_ID_COLUMN), rs.getString(TARGET_COLUMN));
        }
    }

    public Map<Long, String> getRedirects() {
        return redirects;
    }
}
