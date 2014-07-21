/*
 * Copyright (c) 2014 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.    You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,  WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.vmware.vchs.api.samples.ondemand.endtoend;

import java.util.List;

import com.vmware.vchs.api.samples.services.Billing;
import com.vmware.vchs.api.samples.services.IAM;
import com.vmware.vchs.api.samples.services.Metering;
import com.vmware.vchs.vms.billabledata.v5.BillableListType;
import com.vmware.vchs.vms.billabledata.v5.BillableType;
import com.vmware.vchs.vms.billabledata.v5.BillableUsageType;
import com.vmware.vchs.vms.billabledata.v5.BilledUsageType;
import com.vmware.vchs.vms.billabledata.v5.EntityType;

/**
 * MeteringAndBilling
 * 
 * This sample will log in to OnDemand with the provided username and password,
 * then use the provided arguments to retrieve the billing and metering results for the provided L1
 * and L2 services.
 * 
 * Parameters:
 * 
 * hostname          [required] : url of the vCHS onDeamn web service
 * username          [required] : username for the vCHS OnDemand authentication
 * password          [required] : password for the vCHS OnDemand authentication
 * version           [required] : version of the vCHS OnDemand API
 * vdcid             [required] : the VDC id to retrieve billing and metering for
 * vmid              [required] : the VM id to retrieve billing and metering for
 * serviceGroupId    [required] : the service group id to retrieve billing and metering for
 * serviceInstanceId [required] : the service instance id to retrieve billing and metering for
 * 
 * Argument Line:
 * 
 * --hostname [vCHS webservice url] --username [vCHS username] --password [vCHS password]
 * --version [vCHS API version] --vdcid [VDC id] --vmid [VM id] --servicegroupdid [service group id]
 * --serviceinstanceid [service instance id]
 */
public class MeteringAndBilling {
    private SampleCommandLineOptions options = null;
    private String authToken = null;

    public static void main(String[] args) {
        MeteringAndBilling instance = new MeteringAndBilling();
        instance.go(args);
    }

    private void go(String[] args) {
        // Disable Java 7 SNI SSL handshake bug as outlined here:
        // (http://stackoverflow.com/questions/7615645/ssl-handshake-alert-unrecognized-name-error-since-upgrade-to-java-1-7-0)
        System.setProperty("jsse.enableSNIExtension", "false");

        // process command line arguments
        options = new SampleCommandLineOptions();
        options.parseOptions(args);

        // Log in to vCHS API, getting a session in response if login is successful
        System.out.print("\nConnecting to vCHS...");

        authToken = IAM.login(options.hostname, options.username, options.password, options.version);

        System.out.println("Success\n");

        // Retrieve cost info for L1 and L2
        BilledUsageType usageType = Billing.getL1BilledUsage(options.hostname, authToken, options.version, options.sid, options.l1);

        if (null != usageType) {
            List<EntityType> entities = usageType.getEntity();
            if (null != entities && entities.size() > 0) {
                System.out.printf("%-38s %-30s %-20s %-20s %-20s\n", "Name", "Amount", "Currency", "Rate", "Unit");
                System.out.printf("%-38s %-30s %-20s %-20s %-20s\n", "----", "----", "--------", "----", "----");

                for (EntityType entity : entities) {
                    BillableListType blt = entity.getBillableList();
                    List<BillableType> bills = blt.getBillable();
                    for(BillableType bill : bills) {
                        System.out.printf("%-38s %-30s %-20s %-20s %-20s\n", bill.getName(), bill.getCost(), bill.getCurrency(), bill.getRate(), bill.getUnit());
                    }
                }
            }
        }

        // Retrieve usage for L1
        BillableUsageType billableUsageType = Metering.getBillableUsage(options.hostname, authToken,
                options.version, options.sid);

        if (null != billableUsageType) {
            List<EntityType> entities = billableUsageType.getEntity();
            if (null != entities && entities.size() > 0) {
                System.out.printf("%-38s %-30s\n", "Name", "Type");
                System.out.printf("%-38s %-30s\n", "----", "----");

                for (EntityType entity : entities) {
                    System.out.printf("%-38s %-30ss\n", entity.getName(), entity.getEntityType());

                    BillableListType billableList = entity.getBillableList();
                    if (null != billableList) {
                        List<BillableType> billableType = billableList.getBillable();
                        if (null != billableType && billableType.size() > 0) {
                            System.out.printf("%-40s %-20s %-20s %-20s %-20s %-20s\n", "Name",
                                    "Cost", "Rate", "Usage", "Unit", "Currency");
                            System.out.printf("%-40s %-20s %-20s %-20s %-20s %-20s\n", "----",
                                    "----", "----", "-----", "----", "--------");
                            for (BillableType billable : billableType) {
                                System.out.printf("%-40s %-20s %-20s %-20s %-20s\n",
                                        billable.getName(), billable.getCost(), billable.getRate(),
                                        billable.getUsage(), billable.getUnit(),
                                        billable.getCurrency());
                            }
                        }
                    }
                }
            }
        }
    }
}