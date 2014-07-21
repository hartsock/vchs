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
import com.vmware.vcloud.api.rest.schema_v1_5.OrgListType;
import com.vmware.vcloud.api.rest.schema_v1_5.VdcType;
import com.vmware.vcloud.api.rest.schema_v1_5.VmType;

/**
 * This helper class will list all the vdc ids and vm ids accessible to the logged in user. It's
 * primarily useful for getting the L1 and L2 ids for metering and billing API calls.
 */
public class ListVdcAndVmIds {
    private SampleCommandLineOptions options = null;
    private String authToken = null;

    public static void main(String[] args) {
        ListVdcAndVmIds instance = new ListVdcAndVmIds();
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
        authToken = IAM.login(options.hostname, options.username, options.password, options.version);

        if (null != authToken) {
            System.out.println("Success\n");

            // Retrieve service controller instances available for authenticated user
            List<InstanceType> instances = ServiceController.getInstances(options.hostname,
                    options.version, authToken);

            if (null != instances && instances.size() > 0) {
                for (InstanceType instance : instances) {
                    // for each instance that is a COMPUTE type, get all the VDCs, then for each
                    // get all VMs, displaying VDC and VM ids.
                    if (instance.getName().equalsIgnoreCase("Virtual Private Cloud OnDemand")) {
                        // get the compute service api and org info
                        Gson gson = new Gson();
                        InstanceAttribute ia = gson.fromJson(instance.getInstanceAttributes(),
                                InstanceAttribute.class);

                        // Log in to compute API
                        System.out.print("Logging in to compute instance - region: "
                                + instance.getRegion() + "...");
                        String vcdToken = Compute.login(ia.getSessionUri(), options.username,
                                options.password, ia.getOrgName(), options.version);
                        if (null != vcdToken) {
                            System.out.println("Success.\n");
                            OrgListType org = Compute.getOrgDetails(instance.getApiUrl(), vcdToken,
                                    options.version);

                            if (null != org) {
                                Collection<VdcType> vdcs = Compute.getVDCsForOrgs(org, vcdToken,
                                        options.version);

                                if (null != vdcs && vdcs.size() > 0) {
                                    for (VdcType vdc : vdcs) {
                                        System.out.println("VDC " + vdc.getName() + "  ID: "
                                                + parseVdcId(vdc.getId()));
                                        Collection<VmType> vms = Compute.getVmsForVdc(vdc,
                                                vcdToken, options.version);
                                        if (null != vms) {
                                            for (VmType vm : vms) {
                                                System.out.println("VM " + vm.getName() + "  ID: "
                                                        + parseVmId(vm.getId()));
                                            }
                                        } else {
                                            System.out.println("No VMs found.");
                                        }
                                    }
                                }

                                System.out.println();
                            }
                        } else {
                            System.out.println("Could not log in to compute with credentials\n");
                        }
                    }
                }
            }
        }
    }

    private static final String parseVdcId(String vdcId) {
        if (null != vdcId && vdcId.startsWith("urn:vcloud:vdc:")) {
            return vdcId.substring("urn:vcloud:vdc:".length());
        }

        return null;
    }

    private static final String parseVmId(String vdcId) {
        if (null != vdcId && vdcId.startsWith("urn:vcloud:vm:")) {
            return vdcId.substring("urn:vcloud:vm:".length());
        }

        return null;
    }
}