/*
 * Copyright (c) 2014 VMware, Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.vmware.vchs.api.samples.services;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.google.gson.Gson;
import com.vmware.vchs.api.samples.SampleConstants;
import com.vmware.vchs.api.samples.services.helper.HttpUtils;
import com.vmware.vchs.sc.instance.v1.InstanceListType;
import com.vmware.vchs.sc.instance.v1.InstanceType;
import com.vmware.vchs.sc.service.v1.PlanListType;
import com.vmware.vchs.sc.service.v1.PlanType;

/**
 * This helper class implements API calls to the service controller. It provides methods to get the
 * list of plans and instances for the provided authorization token, as well as creating a new
 * instance or deleting an existing instance.
 */
public class ServiceController {
    public static List<PlanType> getPlans(String hostname, String version, String token) {

        HttpGet get = new HttpGet(hostname + "/api/sc/plans");
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        get.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_XML_VERSION + version);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                PlanListType plans = HttpUtils.unmarshal(response.getEntity(), PlanListType.class);
                if (null != plans) {
                    return plans.getPlans();
                }
            }
        }

        return null;
    }

    /**
     * This method will retrieve all the discoverable instances accessible for the logged in user as
     * determined by the provided authToken
     * 
     * @param options
     * @param authToken
     * @return
     */
    public static List<InstanceType> getInstances(String hostname, String version, String token) {
        HttpGet get = new HttpGet(hostname + "/api/sc/instances");
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        get.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_XML_VERSION + version);

        HttpResponse response = HttpUtils.httpInvoke(get);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                InstanceListType instances = HttpUtils.unmarshal(response.getEntity(),
                        InstanceListType.class);
                if (null != instances) {
                    return instances.getInstances();
                }
            }
        }

        return null;
    }

    /**
     * Creates an instance of the service provided by the instanceId
     * 
     * @param hostname
     * @param version
     * @param authToken
     * @param instanceId
     * @return
     */
    public static boolean createInstance(String hostname, String version, String token,
            String planId, String serviceGroupId) {
        HttpPost post = new HttpPost(hostname + "/api/sc/instances");
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        post.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_JSON_VERSION + version
                + ";class=com.vmware.vchs.sc.restapi.model.instancetype");
        post.setHeader(HttpHeaders.CONTENT_TYPE, SampleConstants.APPLICATION_JSON_VERSION + version
                + ";class=com.vmware.vchs.sc.restapi.model.instancespecparamstype");

        InstanceType it = new InstanceType();
        it.setName("NewVDC");
        it.setDescription("A description of new service");
        it.setPlanId(planId);
        it.setServiceGroupId(serviceGroupId);

        Gson g = new Gson();

        String instanceToCreate = g.toJson(it);

        HttpEntity entity;
        try {
            entity = new StringEntity(instanceToCreate);
            post.setEntity(entity);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        HttpResponse response = HttpUtils.httpInvoke(post);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return true;
            }
        }

        return false;
    }

    /**
     * Deletes a service provided by the instance id
     * 
     * @param hostname
     * @param version
     * @param authToken
     * @param instanceId
     * @return
     */
    public static boolean deleteInstance(String hostname, String version, String token,
            String instanceId) {
        HttpDelete delete = new HttpDelete(hostname + "/api/sc/instances/" + instanceId);
        delete.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        delete.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_JSON_VERSION + version);

        HttpResponse response = HttpUtils.httpInvoke(delete);

        if (null != response) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return true;
            }
        }

        return false;
    }
}