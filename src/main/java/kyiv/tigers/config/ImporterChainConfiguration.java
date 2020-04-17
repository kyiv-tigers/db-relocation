package kyiv.tigers.config;

import kyiv.tigers.service.Importer;
import kyiv.tigers.service.OrganizationService;
import kyiv.tigers.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mykola Danyliuk
 */
@Configuration
public class ImporterChainConfiguration {

    @Bean(name = "chain")
    public Importer getImporter(
            OrganizationService organizationService,
            UserService userService
    ){
        Importer importer = organizationService;
        organizationService.setNext((Importer) userService);
        return importer;
    }

}
