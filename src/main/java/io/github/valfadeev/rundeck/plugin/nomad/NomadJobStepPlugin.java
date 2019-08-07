package io.github.valfadeev.rundeck.plugin.nomad;

import java.util.Map;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.dtolabs.rundeck.plugins.util.PropertyBuilder;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;

import io.github.valfadeev.rundeck.plugin.nomad.NomadNodeStepPlugin.Reason;
import io.github.valfadeev.rundeck.plugin.nomad.common.Driver;
import io.github.valfadeev.rundeck.plugin.nomad.common.SupportedDrivers;

@Driver(name = SupportedDrivers.DOCKER)
@Plugin(name = NomadJobStepPlugin.SERVICE_PROVIDER_NAME,
        service = ServiceNameConstants.WorkflowStep)
@PluginDescription(title = "Perform housekeeping operations on a Job",
                   description = "Perform Nomad housekeeping operations on a Job")

public class NomadJobStepPlugin implements StepPlugin, Describable {

    public static final String SERVICE_PROVIDER_NAME
            = "io.github.valfadeev.rundeck.plugin.nomad.NomadJobStepPlugin";
    
    public Description getDescription() {
        return DescriptionBuilder.builder()
            .name(SERVICE_PROVIDER_NAME)
            .title("NomadNodeStep")
            .description("Example WorkflowNode Step")
            .property(PropertyBuilder.builder()
                          .string("example")
                          .title("Example String")
                          .description("Example description")
                          .required(true)
                          .build()
            )
            .property(PropertyBuilder.builder()
                          .booleanType("exampleBoolean")
                          .title("Example Boolean")
                          .description("Example Boolean?")
                          .required(false)
                          .defaultValue("false")
                          .build()
            )
            .property(PropertyBuilder.builder()
                          .freeSelect("exampleFreeSelect")
                          .title("Example Free Select")
                          .description("Example Free Select")
                          .required(false)
                          .defaultValue("Blue")
                          .values("Blue", "Beige", "Black")
                          .build()
            )
            .build();
   }
    
   public void executeStep(final PluginStepContext context, final Map<String, Object> configuration) throws StepException {
        
        PluginLogger logger = context.getExecutionContext().getExecutionListener();
        logger.log(2, String.format("Doing nothing"));
        logger.log(2, context.toString());
        logger.log(2,"Example step extra config: " + configuration);
        logger.log(2,"Example step num: " + context.getStepNumber());
        logger.log(2,"Example step context: " + context.getStepContext());
        logger.log(2, context.getNodes().toString());

   }
}
