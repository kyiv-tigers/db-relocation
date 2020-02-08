select (((jsonb_extract_path_text(u.surname, 'ua') || ' '::text) ||
         jsonb_extract_path_text(u.name, 'ua')) || ' '::text) ||
       COALESCE(jsonb_extract_path_text(u.middle_name, 'ua'), ''::text)     as name,
       (((jsonb_extract_path_text(u.surname, 'ru') || ' '::text) ||
         jsonb_extract_path_text(u.name, 'ru')) || ' '::text) ||
       COALESCE(jsonb_extract_path_text(u.middle_name, 'ru'), ''::text)     as name_ru,
       (((jsonb_extract_path_text(u.surname, 'en') || ' '::text) ||
         jsonb_extract_path_text(u.name, 'en')) || ' '::text) ||
       COALESCE(jsonb_extract_path_text(u.middle_name, 'en'), ''::text)     as name_en,
       u.email                                                              as email,
       u.phone                                                              as phone
from bd00.t_bd00_objects obj
         left join bd00.v_bd00_user_objects u_o on u_o.id_object = obj.id
         left join bd00.t_bd00_users u on u.id = u_o.id_user
where obj.id = :publicbid_id;