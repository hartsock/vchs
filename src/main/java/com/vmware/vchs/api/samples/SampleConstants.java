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
package com.vmware.vchs.api.samples;

/**
 * This class provides common constants that will be used in samples.
 */
public final class SampleConstants {
    /*
     * Prevent this class from being instantiated
     */
    private SampleConstants() {
    }

    /*
     * Content-Type header for vCHS API
     */
    public static final String APPLICATION_XML_VERSION = "application/xml;version=";

    /*
     * Content-Type header for VCD API
     */
    public static final String APPLICATION_PLUS_XML_VERSION = "application/*+xml;version=";

    /*
     * VCD Authorization header string
     */
    public static final String VCD_AUTHORIZATION_HEADER = "x-vcloud-authorization";

    /*
     * Content-Type header for VCD session
     */
    public static final String APPLICATION_XML_VCD_SESSION = "application/xml;class=vnd.vmware.vchs.vcloudsession";

    /*
     * Default vCHS Public API entry point
     */
    public static final String DEFAULT_HOSTNAME = "vchs.vmware.com";

    /*
     * Default vCHS IAM Public API entry point for login and user management
     */
    public static final String DEFAULT_IAM_HOSTNAME = "iam.vchs.vmware.com";

    /*
     * Default vCHS Public API Version to make calls to
     */
    public static final String DEFAULT_VCHS_VERSION = "5.7";

    /*
     * Default VCD API Version to make calls to
     */
    public static final String DEFAULT_VCD_VERSION = "5.7";

    /*
     * VCloud Public API Versions url
     */
    public static final String VERSION_URL = "/api/versions";

    /*
     * The string value representing a vCloud Org
     */
    public static final String ORG = "application/vnd.vmware.vcloud.org+xml";

    /*
     * The string value representing a vCloud Edge Gateway
     */
    public static final String CONTENT_TYPE_EDGE_GATEWAY = "application/vnd.vmware.admin.edgeGatewayServiceConfiguration+xml";

    /*
     * The string value representing the vCHS Authorization request header
     */
    public static final String VCHS_AUTHORIZATION_HEADER = "vchs-authorization";

    /*
     * The string value representing the application/json media type and the version of the API to
     * use
     */
    public static final String APPLICATION_JSON_VERSION = "application/json;version=";
}