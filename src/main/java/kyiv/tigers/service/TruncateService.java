package kyiv.tigers.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.logging.Logger;

import static kyiv.tigers.utils.FileToString.stringFromFile;

/**
 * @author Roman Malyarchuk
 * @project db-relocation
 * @since 09/02/20 19:45
 */
@Service
public class TruncateService implements Importer{
    private static final Logger logger = Logger.getLogger(TruncateService.class.getName());
    private static final String TRUNCATE_ALL = stringFromFile("sql/opentender/truncateAll.sql");
    private static final String NULLIFY_OPENTENDER_ID = stringFromFile("sql/publicbid/nullifyOpentenderId.sql");

    private final NamedParameterJdbcTemplate publicbidJdbcTemplate;
    private final NamedParameterJdbcTemplate opentenderJdbcTemplate;

    private Importer next;
    @Value("${plans.page.size}")
    private int pageSize = 1000;

    public TruncateService(@Qualifier("publicbidJdbcTemplate") NamedParameterJdbcTemplate publicbidJdbcTemplate,
                         @Qualifier("opentenderJdbcTemplate") NamedParameterJdbcTemplate opentenderJdbcTemplate) {
        this.publicbidJdbcTemplate = publicbidJdbcTemplate;
        this.opentenderJdbcTemplate = opentenderJdbcTemplate;
    }

    @Override
    public Importer setNext(Importer next) {
        this.next = next;
        return next;
    }

    private void nullifyOpentenderId(){
        publicbidJdbcTemplate.getJdbcTemplate().execute(NULLIFY_OPENTENDER_ID);
    }

    private void truncateAll(){
        opentenderJdbcTemplate.getJdbcTemplate().execute(TRUNCATE_ALL);
    }

    public boolean start(UUID organizationID){

        try{

            {
                long start = System.currentTimeMillis();
                logger.info("Truncate all...");
                truncateAll();
                long end = System.currentTimeMillis();
                logger.info("Time elapsed: " + (end - start)/1000.0 + "s");

            }

            {
                long start = System.currentTimeMillis();
                logger.info("Nullify all...");
                nullifyOpentenderId();
                long end = System.currentTimeMillis();
                logger.info("Time elapsed: " + (end - start)/1000.0 + "s");
            }

            if(next != null){
                return next.start(organizationID);
            }

            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
}
