package kyiv.tigers;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.logging.Logger;

@SpringBootApplication
public class Application {

    public static void main(String[] args){
        SpringApplicationBuilder app = new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE);
        ApplicationContext context = app.run(args);
        OrganizationService organizationService = context.getBean(OrganizationService.class);
        //organizationService.start();

        TruncateService truncateService = context.getBean(TruncateService.class);
        PlanService planService = context.getBean(PlanService.class);
        TenderService tenderService = context.getBean(TenderService.class);
        AwardService awardService = context.getBean(AwardService.class);
        ContractService contractService = context.getBean(ContractService.class);
        AgreementService agreementService = context.getBean(AgreementService.class);

        truncateService
                .setNext(planService)
                .setNext(tenderService)
                .setNext(awardService)
                .setNext(contractService)
                .setNext(agreementService);

        truncateService.start();
    }
}
