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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.vmware.vchs.api.samples.SampleConstants;
import com.vmware.vchs.api.samples.ondemand.endtoend.SampleCommandLineOptions;
import com.vmware.vchs.api.samples.services.helper.HttpUtils;
import com.vmware.vchs.iam.v2.User;
import com.vmware.vchs.iam.v2.Users;
import com.vmware.vchs.sc.common.v1.ErrorType;

/**
 * This helper class implements the API calls to the IAM service. It provides methods for making
 * REST API calls to log in to IAM as well as user management API calls.
 */
public class IAM {
    // IAM resource path for Users API calls
    private static final String USERS_URL_RESOURCE = "/api/iam/Users";

    // IAM resource path for login API call
    private static final String LOGIN_URL_RESOURCE = "/api/iam/login";

    /**
     * This method will assume the provided URL is to a IAM PayGo service. The Accept header will be
     * set to application/json;version=5.7
     */
    public static final String login(String hostname, String username, String password, String version) {
        HttpPost post = new HttpPost(hostname + LOGIN_URL_RESOURCE);
        post.setHeader(
                HttpHeaders.AUTHORIZATION,
                "Basic "
                        + Base64.encodeBase64URLSafeString(new String(username + ":"
                                + password).getBytes()));
        post.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_JSON_VERSION + version);

        HttpResponse response = HttpUtils.httpInvoke(post);
        if (null != response && response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
            return response.getFirstHeader(SampleConstants.VCHS_AUTHORIZATION_HEADER).getValue();
        }

        return null;
    }

    public static final User getUser(String url, String token, String userId, String version) {
        HttpGet get = new HttpGet(url + USERS_URL_RESOURCE + "/" + userId);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        get.setHeader(HttpHeaders.ACCEPT,
                "application/xml;class=com.vmware.vchs.iam.api.schema.v2.classes.user.User;version="
                        + version);

        HttpResponse response = HttpUtils.httpInvoke(get);
        if (null != response && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                return HttpUtils.unmarshal(response.getEntity(), User.class);
            } catch (ParseException e) {
                e.printStackTrace(System.out);
            }
        } else {
            System.out.println("ERROR . " + response.getStatusLine().getStatusCode());
        }

        return null;
    }

    /**
     * This method will assume the provied URL is to a IAM PayGo service. The Accept header will be
     * set to application/json;version=5.7
     */
    public static final Users getUsers(String url, String token, String version) {
        HttpGet get = new HttpGet(url + USERS_URL_RESOURCE);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        get.setHeader(HttpHeaders.ACCEPT,
                "application/xml;class=com.vmware.vchs.iam.api.schema.v2.classes.user.Users;version="
                        + version);

        HttpResponse response = HttpUtils.httpInvoke(get);
        if (null != response && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                return HttpUtils.unmarshal(response.getEntity(), Users.class);
            } catch (ParseException e) {
                e.printStackTrace(System.out);
            }
        } else {
            ErrorType error = HttpUtils.unmarshal(response.getEntity(), ErrorType.class);
            if (null != error) {
                System.out.println("ERROR CODE : " + error.getCode());
                System.out.println("MESSAGE    : " + error.getMessage());
            }
        }

        return null;
    }

    /**
     * This method will return the User instance of the logged in user, which will include the
     * logged in users companyId and serviceGroupId(s) which can be used for creating new users, and
     * service controller instances.
     * 
     * @param url
     * @param token
     * @return
     */
    public static final User getSelf(String url, String token, String version) {
        HttpGet get = new HttpGet(url + USERS_URL_RESOURCE + "?self=1");
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        get.setHeader(HttpHeaders.ACCEPT,
                "application/json;class=com.vmware.vchs.iam.api.schema.v2.classes.user.Users;version="
                        + version);

        HttpResponse response = HttpUtils.httpInvoke(get);
        if (null != response && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            Users users = HttpUtils.unmarshal(response.getEntity(), Users.class);
            return users.getUsers().get(0);
        }

        System.out.println("ERROR . " + response.getStatusLine().getStatusCode());

        return null;
    }

    /**
     * This method will create a new user.
     * 
     * @param url
     * @param token
     * @param username
     * @param email
     * @param companyId
     * @param role
     * @return
     */
    public static final User createUser(String url, String token, User user, String version) {
        HttpPost post = new HttpPost(url + USERS_URL_RESOURCE);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        post.setHeader(HttpHeaders.CONTENT_TYPE,
                "application/json;class=com.vmware.vchs.iam.api.schema.v2.classes.user.User;version="
                        + version);
        post.setHeader(HttpHeaders.ACCEPT,
                "application/json;class=com.vmware.vchs.iam.api.schema.v2.classes.user.User;version="
                        + version);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class,
                new HttpUtils.XMLGregorianClassConverter.Serializer());
        gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class,
                new HttpUtils.XMLGregorianClassConverter.Deserializer());
        Gson g = gsonBuilder.create();

        String userToSend = g.toJson(user);

        HttpEntity entity;

        try {
            entity = new StringEntity(userToSend);
            post.setEntity(entity);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        HttpResponse response = HttpUtils.httpInvoke(post);
        if (null != response && response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
            try {
                String s = EntityUtils.toString(response.getEntity());
                User createdUser = g.fromJson(s, User.class);
                return createdUser;
            } catch (JsonSyntaxException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (ParseException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } else {
            System.out.println("ERROR . " + response.getStatusLine().getStatusCode() + " message "
                    + response.getStatusLine().getReasonPhrase());
        }

        return null;
    }

    /**
     * This method updates the passed in user by using the PUT method to the
     * /api/iam/Users/{user-id} url. The response should be a 204 if the update was successful.
     * 
     * @param url
     *            the hostname url to send the update request to
     * @param token
     *            the OAUTH token to authenticate the request with
     * @param user
     *            the user instance to send as the entity to update with
     * @return the http status code
     */
    public static int updateUser(String url, String token, User user, String version) {
        // Configure the HttpPut object
        HttpPut put = new HttpPut(url + USERS_URL_RESOURCE + "/" + user.getId());
        put.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        put.setHeader(HttpHeaders.CONTENT_TYPE,
                "application/json;class=com.vmware.vchs.iam.api.schema.v2.classes.user.User;version="
                        + version);
        put.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_JSON_VERSION + version);

        // Configure the GSON object
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class,
                new HttpUtils.XMLGregorianClassConverter.Serializer());
        gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class,
                new HttpUtils.XMLGregorianClassConverter.Deserializer());
        Gson g = gsonBuilder.create();

        // Convert the object to JSON
        String userToSend = g.toJson(user);

        HttpEntity entity;

        try {
            // Configure the HttpEntity with the JSON string
            entity = new StringEntity(userToSend);
            put.setEntity(entity);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        // Send the PUT request
        HttpResponse response = HttpUtils.httpInvoke(put);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
            try {
                System.out.println(EntityUtils.toString(response.getEntity()));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // Return the Http response status code
        return response.getStatusLine().getStatusCode();
    }

    /**
     * This method will send a DELETE request to the provided url to delete the user referenced by
     * the provided userId.
     * 
     * @param url
     *            the hostname url to send the update request to
     * @param token
     *            the OAUTH token to authenticate the request with
     * @param userId
     *            the id of the user to delete
     * @return the http status code
     */
    public static int deleteUser(String url, String token, String userId, String version) {
        HttpDelete delete = new HttpDelete(url + USERS_URL_RESOURCE + "/" + userId);
        delete.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_JSON_VERSION + version);
        delete.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        HttpResponse response = HttpUtils.httpInvoke(delete);
        return response.getStatusLine().getStatusCode();
    }
}