package kyiv.tigers.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static kyiv.tigers.utils.FileToString.stringFromFile;

/**
 * @author Mykola Danyliuk
 */
@Service
public class UserService implements Importer{

    private static final String SELECT_USER_SUBSCRIPTIONS = stringFromFile("sql/publicbid/selectUserSubscriptions.sql");
    private static final String INSERT_USER_SUBSCRIPTIONS = stringFromFile("sql/opentender/insertUserSubscriptions.sql");

    private final NamedParameterJdbcTemplate publicbidJdbcTemplate;
    private final NamedParameterJdbcTemplate opentenderJdbcTemplate;

    private Importer next;

    public UserService(@Qualifier("publicbidJdbcTemplate") NamedParameterJdbcTemplate publicbidJdbcTemplate,
                       @Qualifier("opentenderJdbcTemplate") NamedParameterJdbcTemplate opentenderJdbcTemplate) {
        this.publicbidJdbcTemplate = publicbidJdbcTemplate;
        this.opentenderJdbcTemplate = opentenderJdbcTemplate;
    }

    @Override
    public Importer setNext(Importer next) {
        this.next = next;
        return next;
    }

    @Override
    public boolean start(UUID organizationID) {
        return false;
    }
}
