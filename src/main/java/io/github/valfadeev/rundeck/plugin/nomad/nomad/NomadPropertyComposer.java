package io.github.valfadeev.rundeck.plugin.nomad.nomad;

import java.io.ByteArrayOutputStream;

import com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants;
import com.dtolabs.rundeck.core.storage.ResourceMeta;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.dtolabs.rundeck.plugins.util.PropertyBuilder;
import io.github.valfadeev.rundeck.plugin.nomad.common.PropertyComposer;

import static io.github.valfadeev.rundeck.plugin.nomad.nomad.NomadConfigOptions.*;

public class NomadPropertyComposer extends PropertyComposer {

    @Override
    public DescriptionBuilder addProperties(DescriptionBuilder builder) {
        return builder
                .property(PropertyBuilder.builder()
                        .string(NOMAD_URL)
                        .title("Nomad agent URL")
                        .description("URL of the Nomad agent to submit job (including url scheme and port)")
                        .required(true)
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(ACL_TOKEN)
                        .title("Select Nomad ACL token")
                        .description("Authentication token for cluster access.")
                        .required(false)
                        .renderingOption(StringRenderingConstants.SELECTION_ACCESSOR_KEY,
                            StringRenderingConstants.SelectionAccessor.STORAGE_PATH)
                        .renderingOption(StringRenderingConstants.STORAGE_PATH_ROOT_KEY, "keys")
                        .renderingOption(StringRenderingConstants.STORAGE_FILE_META_FILTER_KEY, "Rundeck-data-type=password")
                        .renderingOption(StringRenderingConstants.VALUE_CONVERSION_KEY,
                                StringRenderingConstants.ValueConversion.STORAGE_PATH_AUTOMATIC_READ)
                        .defaultValue("")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(NOMAD_DATACENTER)
                        .title("Nomad datacenter")
                        .description("A list of datacenters in the region "
                                + "which are eligible for task placement. "
                                + "Defaults to the datacenter of the local agent")
                        .required(false)
                        .defaultValue("")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(NOMAD_REGION)
                        .title("Nomad region")
                        .description("The region in which to execute the job.")
                        .required(false)
                        .defaultValue("")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .integer(NOMAD_GROUP_COUNT)
                        .title("Count")
                        .description("Number of container instances "
                                + "to be run on Nomad cluster")
                        .required(true)
                        .defaultValue("1")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .select(NOMAD_JOB_TYPE)
                        .title("Type of job")
                        .description("Specifies the Nomad scheduler to use.")
                        .required(true)
                        .values("batch",
                                "service"
                        )
                        .defaultValue("batch")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .longType(NOMAD_MAX_FAIL_PCT)
                        .title("Max allowed failed instances, %")
                        .description("Maximum number of job allocations allowed to fail")
                        .required(true)
                        .defaultValue("0")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(NOMAD_ENV_VARS)
                        .title("Environment variables")
                        .description("A list of newline separated environment "
                                + "variable assignments. Example: FOO=foo\\nBAR=bar")
                        .required(false)
                        .defaultValue("")
                        .renderingOption("displayType",
                                StringRenderingConstants.DisplayType.MULTI_LINE)
                        .build()
                )

                .property(PropertyBuilder.builder()
                        .string(NOMAD_MAX_PARALLEL)
                        .title("Max number of parallel updates")
                        .description("Specifies the number of task groups "
                                + "that can be updated at the same time.")
                        .required(false)
                        .defaultValue("")
                        .renderingOption("groupName", "Update Strategy")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .select(NOMAD_HEALTH_CHECK)
                        .title("Type of health check")
                        .description("Specifies the mechanism in which "
                                + "allocations health is determined.")
                        .required(false)
                        .defaultValue("")
                        .values("checks",
                                "task_states",
                                "manual"
                        )
                        .defaultValue("checks")
                        .renderingOption("groupName", "Update Strategy")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .longType(NOMAD_MIN_HEALTHY_TIME)
                        .title("Min time in healthy state")
                        .description("Specifies the minimum time the allocation "
                                + "must be in the healthy state before it is marked "
                                + "as healthy and unblocks further allocations "
                                + "from being updated")
                        .required(false)
                        .defaultValue("")
                        .renderingOption("groupName", "Update Strategy")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .longType(NOMAD_HEALTHY_DEADLINE)
                        .title("Deadline for a healthy status")
                        .description("Specifies the deadline in which the allocation "
                                + "must be marked as healthy after which the "
                                + "allocation is automatically transitioned to unhealthy")
                        .required(false)
                        .defaultValue("")
                        .renderingOption("groupName", "Update Strategy")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .booleanType(NOMAD_AUTO_REVERT)
                        .title("Auto-revert flag")
                        .description("Specifies if the job should auto-revert "
                                + "to the last stable job on deployment failure.")
                        .required(false)
                        .defaultValue("")
                        .renderingOption("groupName", "Update Strategy")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .integer(NOMAD_CANARY)
                        .title("Number of canaries")
                        .description("Specifies that changes to the job that would "
                                + "result in destructive updates should create "
                                + "the specified number of canaries without stopping "
                                + "any previous allocations.")
                        .required(false)
                        .defaultValue("")
                        .renderingOption("groupName", "Update Strategy")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .integer(NOMAD_STAGGER)
                        .title("Stagger")
                        .description("Specifies the delay between migrating allocations "
                                + "off nodes marked for draining.")
                        .required(false)
                        .defaultValue("")
                        .renderingOption("groupName", "Update Strategy")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(NOMAD_DYNAMIC_PORTS)
                        .title("Dynamic port labels")
                        .description("A comma-separated list of labels"
                                + " for dynamically allocated ports")
                        .required(false)
                        .defaultValue("")
                        .renderingOption("groupName", "Networking")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(NOMAD_RESERVED_PORTS)
                        .title("Reserved port labels")
                        .description("A newline-separated key-value map of labels "
                                + "and values for statically allocated ports")
                        .required(false)
                        .defaultValue("")
                        .renderingOption("displayType",
                                StringRenderingConstants.DisplayType.MULTI_LINE)
                        .renderingOption("groupName", "Networking")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .integer(NOMAD_TASK_CPU)
                        .title("CPU limit")
                        .description("Specifies the CPU required to run this task in MHz")
                        .required(true)
                        .defaultValue("100")
                        .renderingOption("groupName", "Resource constraints")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .integer(NOMAD_TASK_MEMORY)
                        .title("Memory limit")
                        .description("Specifies the memory required in MB")
                        .required(true)
                        .defaultValue("256")
                        .renderingOption("groupName", "Resource constraints")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .integer(NOMAD_TASK_IOPS)
                        .title("IOPS limit")
                        .description("Specifies the number of IOPS required "
                                + "given as a weight between 0-1000.")
                        .required(true)
                        .defaultValue("0")
                        .renderingOption("groupName", "Resource constraints")
                        .renderingOption("grouping", "secondary")
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .longType(NOMAD_NETWORK_BANDWIDTH)
                        .title("Network bandwidth, MBits")
                        .description("Specifies the bandwidth required in MBits")
                        .required(false)
                        .defaultValue("10")
                        .renderingOption("groupName", "Resource constraints")
                        .renderingOption("grouping", "secondary")
                        .build()
                );
    }

}

