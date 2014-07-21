package com.vmware.vchs.api.samples.ondemand.details;

import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.vmware.vchs.api.samples.ondemand.endtoend.SampleCommandLineOptions;
import com.vmware.vchs.api.samples.services.Compute;
import com.vmware.vchs.api.samples.services.IAM;
import com.vmware.vchs.api.samples.services.ServiceController;
import com.vmware.vchs.api.samples.services.helper.InstanceAttribute;
import com.vmware.vchs.sc.instance.v1.InstanceType;
import com.vmware.vcloud.api.rest.schema_v1_5.AvailableNetworksType;
import com.vmware.vcloud.api.rest.schema_v1_5.OrgListType;
import com.vmware.vcloud.api.rest.schema_v1_5.ReferenceType;
import com.vmware.vcloud.api.rest.schema_v1_5.VAppTemplateType;
import com.vmware.vcloud.api.rest.schema_v1_5.VdcType;

/**
 * This helper class will list all VDCs and VDC templates.
 */
public class ListVdcsAndTemplates {
    private SampleCommandLineOptions options = null;
    private String authToken = null;

    public static void main(String[] args) {
        ListVdcsAndTemplates instance = new ListVdcsAndTemplates();
        instance.go(args);
    }

    private void go(String[] args) {
        // Disable Java 7 SNI SSL handshake bug as outlined here:
        // (http://stackoverflow.com/questions/7615645/ssl-handshake-alert-unrecognized-name-error-since-upgrade-to-java-1-7-0)
        System.setProperty("jsse.enableSNIExtension", "false");

        options = new SampleCommandLineOptions();

        // process arguments
        options.parseOptions(args);

        // Log in to vCHS API, getting a session in response if login is successful
        System.out.print("\nConnecting to vCHS...");

        authToken = IAM
                .login(options.hostname, options.username, options.password, options.version);

        if (null != authToken) {
            System.out.println("Success\n");

            // Retrieve service controller instances available for authenticated user
            List<InstanceType> instances = ServiceController.getInstances(options.hostname,
                    options.version, authToken);
            InstanceType computeInstance = null;
            if (null != instances && instances.size() > 0) {
                for (InstanceType instance : instances) {
                    if (instance.getRegion().toLowerCase().equalsIgnoreCase(options.region)) {
                        computeInstance = instance;
                    }
                }
            }

            if (null != computeInstance) {
                Gson gson = new Gson();
                InstanceAttribute ia = gson.fromJson(computeInstance.getInstanceAttributes(),
                        InstanceAttribute.class);

                // Log in to compute API
                System.out.print("Logging in to compute...");
                String vcdToken = Compute.login(ia.getSessionUri(), options.username,
                        options.password, ia.getOrgName(), options.version);

                System.out.println("Success.\n");

                OrgListType org = Compute.getOrgDetails(computeInstance.getApiUrl(), vcdToken,
                        options.version);

                if (null != org) {
                    Collection<VdcType> vdcs = Compute.getVDCsForOrgs(org, vcdToken,
                            options.version);

                    if (null != vdcs && vdcs.size() > 0) {
                        VAppTemplateType matchedTemplate = null;
                        for (VdcType vdc : vdcs) {
                            // search for VDC that matches one we're looking for.
                            if (vdc.getName().equalsIgnoreCase(options.vdcname)
                                    || vdc.getId().toLowerCase().equalsIgnoreCase(options.vdcid)) {
                                System.out.println("WE FOUND A MATCHING VDC");
                                // we got it, so lets create a VM

                                // First we need to pull a template
                                Collection<VAppTemplateType> templates = Compute
                                        .getTemplatesForVdc(ia.getSessionUri(), vdc,
                                                options.version, vcdToken);

                                System.out.println("Templates for VDC " + vdc.getName() + ":");
                                for (VAppTemplateType template : templates) {
                                    System.out.println(template.getName() + "  "
                                            + template.getDescription());

                                    if (template.getName().toLowerCase()
                                            .equalsIgnoreCase(options.vmtemplatename)) {
                                        matchedTemplate = template;
                                    }
                                }

                                // If we found a matching template proceed
                                if (null != matchedTemplate) {
                                    // Check the available networks for the matched VDC

                                    AvailableNetworksType availableNetworks = vdc
                                            .getAvailableNetworks();
                                    List<ReferenceType> networks = availableNetworks.getNetwork();

                                    if (null != networks && networks.size() > 0) {
                                        System.out.println("At least one network");
                                        for (ReferenceType network : networks) {
                                            System.out.println("NAME: " + network.getName() + "  "
                                                    + network.getType());
                                        }
                                    }
                                }
                            }
                        }
                        if (null != matchedTemplate) {
                            System.out.println("Found matching template");
                        }
                    }
                }

                // Get the collection of ReferenceType instances that are vdcTemplate types. The
                // reason this returns a Collection<ReferenceType> instead of a
                // Collection<VdcTemplateType> is because to create a VDC from a VDC Template you
                // need the source reference that the VDC Template comes from. In this case, the
                // ReferenceType HREF is the source. Returning just the collection of
                // VdcTemplateTypes would still require another call to be made to get the
                // ReferenceType match for the specific VdcTemplateType that a VDC Template is to
                // be created from. There is no current compute API way to do this, so another
                // call to the same API that this call makes, returning the list of ReferenceType
                // objects would need to be iterated through to find a matching name again. Thus,
                // this call returns the ReferenceType and we simply iterate through it looking
                // for the matching options.vdctemplatename. The collection returned here is
                // filtered so only ReferenceTypes that are actually VdcTemplateType objects are
                // returned.
                Collection<ReferenceType> vdcTemplates = Compute.getVdcTemplates(
                        computeInstance.getApiUrl(), vcdToken, options.version);
                if (null != vdcTemplates && vdcTemplates.size() > 0) {
                    for (ReferenceType vdcTemplate : vdcTemplates) {
                        if (vdcTemplate.getName().equalsIgnoreCase(options.vdctemplatename)) {
                            System.out.println("WE HAVE A MATCHING VDC TEMPLATE NAME "
                                    + options.vdctemplatename);
                            break;
                        }
                    }
                }
            } else {
                System.out.println("No compute instance found");
            }
        } else {
            System.out.println("Could not log in with provided credentials.\n");
        }
    }
}
