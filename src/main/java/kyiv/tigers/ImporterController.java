package kyiv.tigers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author Mykola Danyliuk
 */
@RestController
@RequestMapping(value = "/importer")
public class ImporterController {

    private Importer chain;
    private OrganizationService organizationService;

    @Autowired
    public ImporterController(@Qualifier("chain") Importer chain){
        this.chain = chain;
    }

    @PostMapping(path = "/")
    public void importOrganization(String email) {
        UUID organizationID = organizationService.getOrganizationIdByUserEmail(email)
                .orElseThrow(NullPointerException::new);
        chain.start(organizationID);
    }

}
