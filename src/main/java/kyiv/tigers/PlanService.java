package kyiv.tigers;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

import static kyiv.tigers.FileToString.stringFromFile;

/**
 * @author Roman Malyarchuk
 * @project db-relocation
 * @since 08/02/20 13:51
 */
@Service
public class PlanService implements Importer{

    private static final Logger logger = Logger.getLogger(PlanService.class.getName());
    private static final String SELECT_DATA = stringFromFile("sql/publicbid/selectPlans.sql");
    private static final String COUNT_QUERY = stringFromFile("sql/publicbid/selectCountPlans.sql");
    private static final String INSERT_DATA = stringFromFile("sql/opentender/insertPlans.sql");
    private static final String UPDATE_DATA = stringFromFile("sql/publicbid/updatePlan.sql");

    private final NamedParameterJdbcTemplate publicbidJdbcTemplate;
    private final NamedParameterJdbcTemplate opentenderJdbcTemplate;

    private Importer next;
    @Value("${plans.page.size}")
    private int pageSize = 1000;

    public PlanService(@Qualifier("publicbidJdbcTemplate") NamedParameterJdbcTemplate publicbidJdbcTemplate,
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
                        .addValue("token", values.get(i).get("token"))
                        .addValue("plan_id", values.get(i).get("plan_id"))
                        .addValue("transfer_token", values.get(i).get("transfer_token"))
                        .addValue("user_date_modified", values.get(i).get("user_date_modified"));
        }

        return opentenderJdbcTemplate.batchUpdate(INSERT_DATA, data);
    }

    private SqlParameterSource insertValue(Map<String, Object> value){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        opentenderJdbcTemplate.update(
                INSERT_DATA,
                new MapSqlParameterSource()
                        .addValue("user_id", new Random().nextInt())
                        .addValue("opid", value.get("opid"))
                        .addValue("token", value.get("token"))
                        .addValue("plan_id", value.get("plan_id"))
                        .addValue("transfer_token", value.get("transfer_token"))
                        .addValue("user_date_modified", value.get("user_date_modified")),
                keyHolder
        );

        int generatedId = (Integer) keyHolder.getKeyList().get(0).get("id");
        return new MapSqlParameterSource()
                .addValue("opentender_id", generatedId)
                .addValue("id_cbd", value.get("opid"));
    }

    private int updateValue(SqlParameterSource data){
        return publicbidJdbcTemplate.update(
                UPDATE_DATA,
                data
        );
    }

    public boolean start(){
        List<Map<String, Object>> values;
        int page = 0;
        int allUpdated = 0;

        try{
            Integer count = selectCount();

            long start = System.currentTimeMillis();
            while((values = selectValues(PageRequest.of(page, pageSize))).size() != 0){
                allUpdated += values.stream().map(this::insertValue).map(this::updateValue).reduce((a, b)-> a+b).orElse(0);
                page++;
                logger.info("Plans: " + allUpdated + "/" + count + "(" + (100 * allUpdated/count) + "%)");
            }
            long end = System.currentTimeMillis();

            logger.info("Time elapsed: " + (end - start)/1000.0 + "s");

            if(next != null){
                return next.start();
            }

            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
}

