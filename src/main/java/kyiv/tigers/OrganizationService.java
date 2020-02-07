package kyiv.tigers;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static kyiv.tigers.FileToString.stringFromFile;

@Service
public class OrganizationService {

    private static final String SELECT_DATA = stringFromFile("sql/publicbid/selectOrganizations.sql");
    private static final String CLEAR_DATA = stringFromFile("sql/opentender/clearOrganizations.sql");
    private static final String INSERT_DATA = stringFromFile("sql/opentender/insertOrganizations.sql");

    private final JdbcTemplate publicbidJdbcTemplate;
    private final JdbcTemplate opentenderJdbcTemplate;

    public OrganizationService(@Qualifier("publicbidJdbcTemplate") JdbcTemplate publicbidJdbcTemplate, @Qualifier("opentenderJdbcTemplate") JdbcTemplate opentenderJdbcTemplate) {
        this.publicbidJdbcTemplate = publicbidJdbcTemplate;
        this.opentenderJdbcTemplate = opentenderJdbcTemplate;
    }

    public void start(){
        List<Map<String, Object>> list = publicbidJdbcTemplate.queryForList(SELECT_DATA);
        list.forEach(System.out::println);
    }

}
