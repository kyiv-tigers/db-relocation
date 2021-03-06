select json_extract_path_text(obj.name::json, 'ua'::text)      as name,
       json_extract_path_text(obj.name::json, 'ru'::text)      as name_ru,
       json_extract_path_text(obj.name::json, 'en'::text)      as name_en,
       obj.c_state_r                                           as identifier_scheme,
       obj.tin                                                 as identifier_code,
       json_extract_path_text(obj.full_name::json, 'ua'::text) as identifier_legal_name,
       json_extract_path_text(obj.full_name::json, 'ru'::text) as identifier_legal_name_ru,
       json_extract_path_text(obj.full_name::json, 'en'::text) as identifier_legal_name_en,
       obj_state.c_state::text                                 as country_code,
       obj_state.name                                          as country_name,
       obj.scale                                               as scale,
       obj_region.c_reg::text                                  as region_code,
       obj_region.name                                         as region_name,
       obj_territ.c_territ::text                               as locality_code,
       obj_territ.name                                         as locality_name,
       json_extract_path_text(obj.streetaddress::json, 'ua'::text) as street_address,
       obj.zip_code                                            as postal_code
from bd00.t_bd00_objects obj
         left join etalon.e_state obj_state on obj.c_state = obj_state.c_state
         left join etalon.e_regions obj_region on obj.c_reg = obj_region.c_reg
         left join etalon.e_territ obj_territ on obj.c_territ = obj_territ.c_territ
where obj_state.lang = 'ua' and obj_region.lang = 'ua' and obj_territ.lang = 'ua' and obj.id = :id;