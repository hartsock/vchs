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
import com.vmware.vchs.vms.billabledata.v5.BilledCostsType;
import com.vmware.vchs.vms.billabledata.v5.BilledUsageType;
import com.vmware.vchs.vms.billabledata.v5.ServiceGroupType;
import com.vmware.vchs.vms.billabledata.v5.ServiceGroupsType;

/**
 * This helper class implements API calls to the metering and billing APIs. This particular class
 * focuses on the billing API calls.
 */
public class Billing {
    /**
     * List all service-groups for a given company. It will include company details and service
     * group list. It uses auth token to determine the company for which service-groups will be
     * returned
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH 2 token
     * @param version
     *            version of the API to invoke
     * @return instance of ServiceGroupsType or null
     */
    public static ServiceGroupsType listServiceGroups(String url, String authToken, String version) {
        HttpGet get = new HttpGet(url + "/api/billing/service-groups");
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        get.setHeader(HttpHeaders.ACCEPT,
                "application/xml;class=vnd.vmware.vchs.billing.serviceGroups;version=" + version);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return HttpUtils.unmarshal(response.getEntity(), ServiceGroupsType.class);
            }
        }

        return null;
    }

    /**
     * Retrieve data associated with the specified service group. It will include details like
     * service group id, service group display name, billing currency, billing attributes and
     * anniversary dates
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH 2 token
     * @param version
     *            version of the API to invoke
     * @param serviceGroupId
     *            the service group id
     * @return instance of ServiceGroupType or null
     */
    public static ServiceGroupType getServiceGroupDetails(String url, String authToken,
            String version, String serviceGroupId) {
        HttpGet get = new HttpGet(url + "/api/billing/service-group/" + serviceGroupId);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        get.setHeader(HttpHeaders.ACCEPT,
                "application/xml;class=vnd.vmware.vchs.billing.serviceGroup;version=" + version);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return HttpUtils.unmarshal(response.getEntity(), ServiceGroupType.class);
            }
        }

        return null;
    }

    /**
     * List cost items associated with the specified service group for a given billing month; Cost
     * items are shown only for the month for which bill is generated. It will support the following
     * cost items - Support Cost and Service Credit If query params are not specified, then it will
     * default to 'month={last billed month} and year={last billed year}'
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH2 token
     * @param version
     *            the version of the API to invoke
     * @param serviceGroupId
     *            service group id
     * @return an instance of BilledCostsType or null
     */
    public static BilledCostsType getBilledCosts(String url, String authToken, String version,
            String serviceGroupId) {
        HttpGet get = new HttpGet(url + "/api/billing/service-group/" + serviceGroupId
                + "/billed-costs");
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        get.setHeader(HttpHeaders.ACCEPT,
                "application/xml;class=vnd.vmware.vchs.billing.billedCosts;version=" + version);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return HttpUtils.unmarshal(response.getEntity(), BilledCostsType.class);
            }
        }

        return null;
    }

    /**
     * Gets billed usage for the specified L1 for a given billing month; Usage is shown only for
     * months for which bill is generated. It will include details like bill duration, entity
     * details, metric name, usage, unit, rate, currency and cost If query params are not specified,
     * then it will default to 'month={last billed month} and year={last billed year}'
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH2 token
     * @param version
     *            version of the API to invoke
     * @param serviceInstanceId
     *            the service instance id
     * @param l1id
     *            the L1 id
     * @return an instance of BilledUsageType or null
     */
    public static BilledUsageType getL1BilledUsage(String url, String authToken, String version,
            String serviceInstanceId, String l1id) {
        HttpGet get = new HttpGet(url + "/api/billing/service-instance/" + serviceInstanceId
                + "/l1/" + l1id + "/billed-usage");
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        get.setHeader(HttpHeaders.ACCEPT,
                "application/xml;class=vnd.vmware.vchs.billing.billedUsage;version=" + version);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return HttpUtils.unmarshal(response.getEntity(), BilledUsageType.class);
            }
        }

        return null;
    }

    /**
     * Gets billed usage for the specified L2 for a given billing month; Usage is shown only for
     * months for which bill is generated. It will include details like bill duration, entity
     * details, metric name, usage, unit, rate, currency and cost If query params are not specified,
     * then it will default to 'month={last billed month}, year={last billed year} and level=self'
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH2 token
     * @param version
     *            version of the API to invoke
     * @param serviceInstanceId
     *            the service instance id
     * @param l2
     *            the L2 id
     * @return an instance of BilledUsageType or null
     */
    public static BilledUsageType getL2BilledUsage(String url, String authToken, String version,
            String serviceInstanceId, String l2id) {
        HttpGet get = new HttpGet(url + "/api/billing/service-instance/" + serviceInstanceId
                + "/l2/" + l2id + "/billed-usage");
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        get.setHeader(HttpHeaders.ACCEPT,
                "application/xml;class=vnd.vmware.vchs.billing.billedUsage;version=" + version);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return HttpUtils.unmarshal(response.getEntity(), BilledUsageType.class);
            }
        }

        return null;
    }

    /**
     * Gets billed usage for the specified service instance for a given billing month; Usage is
     * shown only for months for which bill is generated. It will include details like bill
     * duration, entity details, metric name, usage, unit, rate, currency and cost If query params
     * are not specified, then it will default to 'month={last billed month}, year={last billed
     * year} and level=self'
     * 
     * @param url
     *            the base API url
     * @param authToken
     *            OAUTH2 token
     * @param version
     *            version of the API to invoke
     * @param serviceInstanceId
     *            the service instance id
     * @return an instance of BilledUsageType or null
     */
    public static BilledUsageType getBilledUsageForServiceInstance(String url, String authToken,
            String version, String serviceInstanceId) {
        HttpGet get = new HttpGet(url + "/api/billing/service-instance/" + serviceInstanceId
                + "/billed-usage");
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        get.setHeader(HttpHeaders.ACCEPT,
                "application/xml;class=vnd.vmware.vchs.billing.billedUsage;version=" + version);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return HttpUtils.unmarshal(response.getEntity(), BilledUsageType.class);
            }
        }

        return null;
    }
}