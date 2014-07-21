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
package com.vmware.vchs.api.samples.services;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import com.vmware.vchs.api.samples.services.helper.HttpUtils;
import com.vmware.vchs.vms.billabledata.v5.BillableCostsType;
import com.vmware.vchs.vms.billabledata.v5.BillableUsageType;

/**
 * This helper class implements API calls to the metering and billing APIs. This particular class
 * focuses on the metering API calls.
 */
public class Metering {

    /**
     * Gets billable/current usage for the specified L1; Usage is shown only for the duration for
     * which bill is not yet generated. It will include details like entity details, metric name,
     * usage, unit, rate, currency and cost If query params are not specified, then it will default
     * to 'duration=BillToDate' Query params start and end are mutually exclusive with duration
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH2 token
     * @param version
     *            version of the API to invoke
     * @param serviceInstanceId
     *            the service instance ID
     * @param l1Id
     *            the L1 id
     * @return an instance of BillableUsageType or null
     */
    public static BillableUsageType getL1BillableUsage(String url, String authToken,
            String version, String serviceInstanceId, String l1Id) {

        HttpGet get = new HttpGet(url + "/api/metering/service-instance/" + serviceInstanceId
                + "/l1/" + l1Id + "/billable-usage");
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        get.setHeader(HttpHeaders.ACCEPT,
                "application/xml;class=vnd.vmware.vchs.metering.billableUsage;version=" + version);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                BillableUsageType bill = HttpUtils.unmarshal(response.getEntity(),
                        BillableUsageType.class);
                return bill;
            }
        }

        return null;
    }

    /**
     * Gets billable/current usage for the specified L2; Usage is shown only for durations for which
     * bill is not yet generated. It will include details like entity details, metric name, usage,
     * unit, rate, currency and cost If query params are not specified, then it will default to
     * 'duration=BillToDate' Query params start and end are mutually exclusive with duration
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH 2 token
     * @param version
     *            version of the API to invoke
     * @param serviceInstanceId
     *            the service instance ID
     * @param l2Id
     *            the L2 id
     * @return instance of BillableUsageType or null
     */
    public static BillableUsageType getL2BillableUsage(String url, String authToken,
            String version, String serviceInstanceId, String l2Id) {

        HttpGet get = new HttpGet(url + "/api/metering/service-instance/" + serviceInstanceId
                + "/l2/" + l2Id + "/billable-usage");
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        get.setHeader(HttpHeaders.ACCEPT,
                "application/xml;class=vnd.vmware.vchs.metering.billableUsage;version=" + version);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                BillableUsageType bill = HttpUtils.unmarshal(response.getEntity(),
                        BillableUsageType.class);
                return bill;
            }
        }

        return null;
    }

    /**
     * Gets billable/current usage for the specified service instance; Usage is shown only for
     * durations for which bill is not yet generated. It will include details like entity details,
     * metric name, usage, unit, rate, currency and cost If query params are not specified, then it
     * will default to 'duration=BillToDate' Query params start and end are mutually exclusive with
     * duration
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH 2 token
     * @param version
     *            version of the API to invoke
     * @param serviceInstanceId
     *            the service instance id
     * @return instance of BillableUsageType or null
     */
    public static BillableUsageType getBillableUsage(String url, String authToken, String version,
            String serviceInstanceId) {

        HttpGet get = new HttpGet(url + "/api/metering/service-instance/" + serviceInstanceId
                + "/billable-usage");
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        get.setHeader(HttpHeaders.ACCEPT,
                "application/xml;class=vnd.vmware.vchs.metering.billableUsage;version=" + version);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return HttpUtils.unmarshal(response.getEntity(), BillableUsageType.class);
            }
        }

        return null;
    }

    /**
     * Represent billable/current value of cost items associated with the specified service group;
     * Only those cost items are listed which are available after last bill cut/generation date. It
     * will support the following cost items - Support Cost and Service Credit
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH 2 token
     * @param version
     *            version of the API to invoke
     * @param serviceGroupId
     *            the service group id
     * @return instance of BillableCostsType or null
     */
    public static BillableCostsType getBillableCosts(String url, String authToken, String version,
            String serviceGroupId) {

        HttpGet get = new HttpGet(url + "/api/metering/service-group/" + serviceGroupId
                + "/billable-costs");
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        get.setHeader(HttpHeaders.ACCEPT,
                "application/xml;class=vnd.vmware.vchs.metering.billableCosts;version=" + version);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                BillableCostsType bill = HttpUtils.unmarshal(response.getEntity(),
                        BillableCostsType.class);
                return bill;
            }
        }

        return null;
    }
}