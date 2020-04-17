package kyiv.tigers;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

import static kyiv.tigers.FileToString.stringFromFile;

/**
 * @author Roman Malyarchuk
 * @project db-relocation
 * @since 08/02/20 17:28
 */
@Service
public class AgreementService implements Importer{

    private static final Logger logger = Logger.getLogger(AgreementService.class.getName());
    private static final String SELECT_DATA = stringFromFile("sql/publicbid/selectAgreements.sql");
    private static final String COUNT_QUERY = stringFromFile("sql/publicbid/selectCountAgreements.sql");
    private static final String INSERT_DATA = stringFromFile("sql/opentender/insertAgreements.sql");

    private final NamedParameterJdbcTemplate publicbidJdbcTemplate;
    private final NamedParameterJdbcTemplate opentenderJdbcTemplate;

    private Importer next;
    @Value("${plans.page.size}")
    private int pageSize = 1000;

    public AgreementService(@Qualifier("publicbidJdbcTemplate") NamedParameterJdbcTemplate publicbidJdbcTemplate,
                            @Qualifier("opentenderJdbcTemplate") NamedParameterJdbcTemplate opentenderJdbcTemplate) {
        this.publicbidJdbcTemplate = publicbidJdbcTemplate;
        this.opentenderJdbcTemplate = opentenderJdbcTemplate;
    }

    @Override
    public Importer setNext(Importer next) {
        this.next = next;
        return next;
    }

    private List<Map<String, Object>> selectValues(Pageable pageable){
        return publicbidJdbcTemplate.queryForList(SELECT_DATA, new MapSqlParameterSource()
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset()));
    }

    private Integer selectCount(){
        return publicbidJdbcTemplate.queryForObject(
                COUNT_QUERY,
                new MapSqlParameterSource(),
                Integer.class);
    }

    private int[] insertValues(List<Map<String, Object>> values){
        SqlParameterSource[] data = new SqlParameterSource[values.size()];

        for(int i = 0; i < values.size(); i++){
            data[i] =
                    new MapSqlParameterSource()
                            .addValue("user_id", new Random().nextInt())
                            .addValue("opid", values.get(i).get("opid"))
                            .addValue("tender_id", values.get(i).get("tender_id"))
                            .addValue("agreement_id", values.get(i).get("agreement_id"))
                            .addValue("token", values.get(i).get("token"))
                            .addValue("transfer_token", values.get(i).get("transfer_token"))
                            .addValue("user_date_modified", values.get(i).get("user_date_modified"));
        }

        return opentenderJdbcTemplate.batchUpdate(INSERT_DATA, data);
    }

    public boolean start(UUID organizationID){
        List<Map<String, Object>> values;
        int page = 0;
        int allUpdated = 0;

        try{
            Integer count = selectCount();

            long start = System.currentTimeMillis();
            while((values = selectValues(PageRequest.of(page, pageSize))).size() != 0){
                allUpdated += Arrays.stream(insertValues(values)).filter(val -> val >= 0).sum();
                page++;
                logger.info("Agreements: " + allUpdated + "/" + count + "(" + (100 * allUpdated/count) + "%)");
            }
            long end = System.currentTimeMillis();

            logger.info("Time elapsed: " + (end - start)/1000.0 + "s");

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
