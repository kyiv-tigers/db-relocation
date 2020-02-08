package kyiv.tigers;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static kyiv.tigers.FileToString.stringFromFile;

@Service
public class OrganizationService {

    private static final String SELECT_OBJECTS = stringFromFile("sql/publicbid/selectObjects");
    private static final String INSERT_ORGANIZATION = stringFromFile("sql/opentender/insertOrganizations");
    private static final String UPDATE_OBJECT_OPENTENDER_ID = stringFromFile("sql/publicbid/updateObjectOpentenderId.sql");
    private static final String SELECT_CONTACT_POINTS = stringFromFile("sql/publicbid/selectContactPoints.sql");
    private static final String INSERT_ORGANIZATION_IDENTIFIER = stringFromFile("sql/opentender/insertOrganizationIdentifier");
    private static final String INSERT_ORGANIZATION_CONTACT_POINT = stringFromFile("sql/opentender/insertOrganizationContactPoint");
    private static final String SELECT_OBJECT_BANK = stringFromFile("sql/publicbid/selectObjectBankAccount.sql");
    private static final String UPDATE_ORGANIZATION_BANK_ACCOUNT = stringFromFile("sql/opentender/updateOrganizationBankAccount.sql");



    private final NamedParameterJdbcTemplate publicbidJdbcTemplate;
    private final NamedParameterJdbcTemplate opentenderJdbcTemplate;

    public OrganizationService(@Qualifier("publicbidJdbcTemplate") NamedParameterJdbcTemplate publicbidJdbcTemplate,
                               @Qualifier("opentenderJdbcTemplate") NamedParameterJdbcTemplate opentenderJdbcTemplate) {
        this.publicbidJdbcTemplate = publicbidJdbcTemplate;
        this.opentenderJdbcTemplate = opentenderJdbcTemplate;
    }

    public void start(){
        List<Map<String, Object>> objects = publicbidJdbcTemplate.queryForList(SELECT_OBJECTS, new MapSqlParameterSource());
        objects.forEach(m -> {
            UUID publicbidID = (UUID) m.get("id");

            KeyHolder keyHolder = new GeneratedKeyHolder();
            opentenderJdbcTemplate.update(
                    INSERT_ORGANIZATION,
                    new MapSqlParameterSource()
                            .addValue("name", m.get("name"))
                            .addValue("name_ru", m.get("name_ru"))
                            .addValue("name_en", m.get("name_en"))
                            .addValue("identifier_scheme", m.get("identifier_scheme"))
                            .addValue("identifier_code", m.get("identifier_code"))
                            .addValue("identifier_legal_name", m.get("identifier_legal_name"))
                            .addValue("identifier_legal_name_ru", m.get("identifier_legal_name_ru"))
                            .addValue("identifier_legal_name_en", m.get("identifier_legal_name_en"))
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
            Integer opentenderID = (Integer) keyHolder.getKeyList().get(0).get("id");

            Map<String, Object> bank = publicbidJdbcTemplate.queryForMap(
                    SELECT_OBJECT_BANK,
                    new MapSqlParameterSource()
                            .addValue("publicbid_id", publicbidID)
            );
            opentenderJdbcTemplate.update(
                    UPDATE_ORGANIZATION_BANK_ACCOUNT,
                    new MapSqlParameterSource()
                            .addValue("bank_account", bank.get("bank_account"))
                            .addValue("bank_name", bank.get("bank_name"))
                            .addValue("bank_mfo", bank.get("bank_mfo"))
                            .addValue("opentender_id", opentenderID)
            );

            opentenderJdbcTemplate.update(
                    INSERT_ORGANIZATION_IDENTIFIER,
                    new MapSqlParameterSource()
                            .addValue("organization_id", opentenderID)
                            .addValue("scheme", m.get("identifier_scheme"))
                            .addValue("code", m.get("identifier_code"))
                            .addValue("legal_name", m.get("identifier_legal_name"))
                            .addValue("legal_name_ru", m.get("identifier_legal_name_ru"))
                            .addValue("legal_name_en", m.get("identifier_legal_name_en"))
            );

            publicbidJdbcTemplate.update(
                    UPDATE_OBJECT_OPENTENDER_ID,
                    new MapSqlParameterSource()
                            .addValue("publicbid_id", publicbidID)
                            .addValue("opentender_id", opentenderID)
            );

            List<Map<String, Object>> contactPoints = publicbidJdbcTemplate.queryForList(
                    SELECT_CONTACT_POINTS,
                    new MapSqlParameterSource()
                            .addValue("publicbid_id", publicbidID)
            );

            contactPoints.forEach(c -> opentenderJdbcTemplate.update(
                    INSERT_ORGANIZATION_CONTACT_POINT,
                    new MapSqlParameterSource()
                            .addValue("organization_id", opentenderID)
                            .addValue("name", c.get("name"))
                            .addValue("name_ru", c.get("name_ru"))
                            .addValue("name_en", c.get("name_en"))
                            .addValue("email", c.get("email"))
                            .addValue("phone", c.get("phone"))
            ));
        });
    }


}
