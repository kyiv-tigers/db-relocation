package kyiv.tigers;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static kyiv.tigers.FileToString.stringFromFile;

@Service
public class OrganizationService implements Importer{

    private static final String SELECT_DATA = stringFromFile("sql/publicbid/selectOrganizations");
    private static final String INSERT_DATA = stringFromFile("sql/opentender/insertOrganizations");

    private final NamedParameterJdbcTemplate publicbidJdbcTemplate;
    private final NamedParameterJdbcTemplate opentenderJdbcTemplate;

    private Importer next;

    public OrganizationService(@Qualifier("publicbidJdbcTemplate") NamedParameterJdbcTemplate publicbidJdbcTemplate,
                               @Qualifier("opentenderJdbcTemplate") NamedParameterJdbcTemplate opentenderJdbcTemplate) {
        this.publicbidJdbcTemplate = publicbidJdbcTemplate;
        this.opentenderJdbcTemplate = opentenderJdbcTemplate;
    }

    @Override
    public Importer setNext(Importer next) {
        this.next = next;
        return next;
    }

    public boolean start(){
        try{
            List<Map<String, Object>> list = publicbidJdbcTemplate.queryForList(SELECT_DATA, new MapSqlParameterSource());
            list.forEach(m -> {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                int result = opentenderJdbcTemplate.update(
                        INSERT_DATA,
                        new MapSqlParameterSource()
                                .addValue("name", m.get("name"))
                                .addValue("name_ru", m.get("name_ru"))
                                .addValue("name_en", m.get("name_en"))
                                .addValue("identifier_scheme", m.get("identifier_scheme"))
                                .addValue("identifier_code", m.get("identifier_code"))
                                .addValue("identifier_legal_name", m.get("identifier_legal_name"))
                                .addValue("identifier_legal_name_ru", m.get("identifier_legal_name_ru"))
                                .addValue("identifier_legal_name_en", m.get("identifier_legal_name_en"))
                                .addValue("bank_account", m.get("bank_account"))
                                .addValue("bank_name", m.get("bank_name"))
                                .addValue("bank_mfo", m.get("bank_mfo"))
                                .addValue("country_code", m.get("country_code"))
                                .addValue("country_name", m.get("country_name"))
                                .addValue("region_code", m.get("region_code"))
                                .addValue("region_name", m.get("region_name"))
                                .addValue("locality_code", m.get("locality_code"))
                                .addValue("locality_name", m.get("locality_name"))
                                .addValue("street_address", m.get("street_address"))
                                .addValue("postal_code", m.get("postal_code"))
                                .addValue("scale", m.get("scale")),
                        keyHolder
                );
                Long organizationID = (Long) keyHolder.getKeyList().get(0).get("id");
            });



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
