package kyiv.tigers.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static kyiv.tigers.utils.FileToString.stringFromFile;

@Service
public class OrganizationService implements Importer{

    private static final String SELECT_OBJECT = stringFromFile("sql/publicbid/selectObject.sql");
    private static final String INSERT_ORGANIZATION = stringFromFile("sql/opentender/insertOrganizations");
    private static final String UPDATE_OBJECT_OPENTENDER_ID = stringFromFile("sql/publicbid/updateObjectOpentenderId.sql");
    private static final String SELECT_CONTACT_POINTS = stringFromFile("sql/publicbid/selectContactPoints.sql");
    private static final String INSERT_ORGANIZATION_IDENTIFIER = stringFromFile("sql/opentender/insertOrganizationIdentifier");
    private static final String INSERT_ORGANIZATION_CONTACT_POINT = stringFromFile("sql/opentender/insertOrganizationContactPoint");
    private static final String SELECT_OBJECT_BANK = stringFromFile("sql/publicbid/selectObjectBankAccount.sql");
    private static final String UPDATE_ORGANIZATION_BANK_ACCOUNT = stringFromFile("sql/opentender/updateOrganizationBankAccount.sql");

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

    public boolean start(UUID objectID){
        try{
            Map<String, Object> object = publicbidJdbcTemplate.
                    queryForMap(SELECT_OBJECT, new MapSqlParameterSource()
                            .addValue("id", objectID));

                KeyHolder keyHolder = new GeneratedKeyHolder();
                opentenderJdbcTemplate.update(
                        INSERT_ORGANIZATION,
                        new MapSqlParameterSource()
                                .addValue("name", object.get("name"))
                                .addValue("name_ru", object.get("name_ru"))
                                .addValue("name_en", object.get("name_en"))
                                .addValue("identifier_scheme", object.get("identifier_scheme"))
                                .addValue("identifier_code", object.get("identifier_code"))
                                .addValue("identifier_legal_name", object.get("identifier_legal_name"))
                                .addValue("identifier_legal_name_ru", object.get("identifier_legal_name_ru"))
                                .addValue("identifier_legal_name_en", object.get("identifier_legal_name_en"))
                                .addValue("country_code", object.get("country_code"))
                                .addValue("country_name", object.get("country_name"))
                                .addValue("region_code", object.get("region_code"))
                                .addValue("region_name", object.get("region_name"))
                                .addValue("locality_code", object.get("locality_code"))
                                .addValue("locality_name", object.get("locality_name"))
                                .addValue("street_address", object.get("street_address"))
                                .addValue("postal_code", object.get("postal_code"))
                                .addValue("scale", object.get("scale")),
                        keyHolder
                );
                Integer opentenderID = (Integer) keyHolder.getKeyList().get(0).get("id");

                Map<String, Object> bank = publicbidJdbcTemplate.queryForMap(
                        SELECT_OBJECT_BANK,
                        new MapSqlParameterSource()
                                .addValue("publicbid_id", objectID)
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
                                .addValue("scheme", object.get("identifier_scheme"))
                                .addValue("code", object.get("identifier_code"))
                                .addValue("legal_name", object.get("identifier_legal_name"))
                                .addValue("legal_name_ru", object.get("identifier_legal_name_ru"))
                                .addValue("legal_name_en", object.get("identifier_legal_name_en"))
                );

                publicbidJdbcTemplate.update(
                        UPDATE_OBJECT_OPENTENDER_ID,
                        new MapSqlParameterSource()
                                .addValue("publicbid_id", objectID)
                                .addValue("opentender_id", opentenderID)
                );

                List<Map<String, Object>> contactPoints = publicbidJdbcTemplate.queryForList(
                        SELECT_CONTACT_POINTS,
                        new MapSqlParameterSource()
                                .addValue("publicbid_id", objectID)
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

            if(next != null){
                return next.start(objectID);
            }

            return true;

        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    public Optional<UUID> getOrganizationIdByUserEmail(String email){
        return Optional.empty();
    }
}