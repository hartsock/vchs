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
package com.vmware.vchs.api.samples.services.helper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.GregorianCalendar;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.vmware.vchs.api.samples.SampleConstants;
import com.vmware.vcloud.api.rest.schema_v1_5.QueryResultRecordsType;

/**
 * This class provides the common http functionality using the Apache HttpClient library.
 */
public class HttpUtils {
    /**
     * Executes an http request using the passed in request parameter.
     * 
     * @param request
     *            the HttpRequestBase subclass to make a request with
     * @return the response of the request
     */
    public static HttpResponse httpInvoke(HttpRequestBase request) {
        HttpResponse httpResponse = null;
        HttpClient httpClient = null;

        try {
            // Create a fresh HttpClient.. some samples will make calls to two (or more)
            // urls in a single run, sharing a static non-multithreaded instance causes
            // exceptions. This prevents that by ensuring each call to httpInvoke gets
            // its own instance.
            httpClient = createTrustingHttpClient();
            httpResponse = httpClient.execute(request);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return httpResponse;
    }

    /**
     * This method returns a secure HttpClient instance.
     * 
     * @return HttpClient a new secure instance of HttpClient
     */
    static HttpClient createSecureHttpClient() {
        return null;
    }

    /**
     * This method returns an HttpClient instance wrapped to trust all HTTPS certificates.
     * 
     * @return HttpClient a new instance of HttpClient
     */
    static HttpClient createTrustingHttpClient() {
        HttpClient base = new DefaultHttpClient();

        try {
            SSLContext ctx = SSLContext.getInstance("TLS");

            // WARNING: This creates a TrustManager that trusts all certificates and should not be
            // used in production code.
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                        String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                        String authType) {
                }
            }
            };

            ctx.init(null, trustAllCerts, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = base.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", 443, ssf));

            return new DefaultHttpClient(ccm, base.getParams());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * This helper method will create the JAXB context for the provided Class<T> and
     * marshal the provided JAXBElement<T> into a StringEntity.
     * 
     * @param clazz
     * @param jaxb
     * @return
     */
    public static <T> StringEntity marshal(Class<T> clazz, JAXBElement<T> jaxb) {
        JAXBContext jaxbContexts = null;
        OutputStream os = null;

        try {
            jaxbContexts = JAXBContext.newInstance(clazz);
        } catch (JAXBException ex) {
            throw new RuntimeException("Problem creating JAXB Context: ", ex);
        }

        try {
            javax.xml.bind.Marshaller marshaller = jaxbContexts.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            os = new ByteArrayOutputStream();
            // Marshal the object via JAXB to XML
            marshaller.marshal(jaxb, os);
        } catch (JAXBException e) {
            throw new RuntimeException("Problem marshalling instantiation VDC template", e);
        }

        try {
            return new StringEntity(os.toString());
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException("Problem marshalling instantiation VDC template", e1);
        }
    }

    /**
     * This method will unmarshal the passed in entity using the passed in class type. It will check
     * the content-type to determine if the response is json or xml and use the appropriate
     * deserializer.
     * 
     * @param entity
     *            the entity to unmarshal
     * @param clazz
     *            the class type to base the unmarshal from
     * @return unmarshal an instance of the provided class type
     */
    public static <T> T unmarshal(HttpEntity entity, Class<T> clazz) {
        InputStream is = null;

        try {
            String s = EntityUtils.toString(entity);

            // Check if the response content-type contains the string json.. if so use GSON to
            // convert from json to the provided Class<T> type
            if (entity.getContentType().toString().toLowerCase().contains("json")) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class,
                        new XMLGregorianClassConverter.Serializer());
                gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class,
                        new XMLGregorianClassConverter.Deserializer());

                Gson g = gsonBuilder.create();
                return g.fromJson(s, clazz);
            }

            is = new ByteArrayInputStream(s.getBytes("UTF-8"));
            return JAXB.unmarshal(is, clazz);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * This method can be used to query the vCloud Query API. The baseVcdUrl represents the portion
     * of the url up to the /api at the end. The /query is appended. Query parameters allow any of
     * the Query API to be called, and the response is the QueryResultRecordsType which the calling
     * method can then use to parse the response. The version is the version of the vCloud Query API
     * to call, and the token is the vCloud API token provided by the login step via the
     * x-vcloud-authorization response header.
     * 
     * @param baseVcdUrl
     *            the base vCloud API url up to the /api on the end
     * @param queryParameters
     *            any vCloud Query API Parameters
     * @param version
     *            the vCloud Query API version to call against
     * @param token
     *            the vCloud API token retrieved after a successful login
     * @return
     */
    public static QueryResultRecordsType getQueryResults(String baseVcdUrl, String queryParameters,
            String version, String token) {
        URL url = null;
        QueryResultRecordsType results = null;

        try {
            // Construct the URL from the baseVcdUrl to utilize the vCloud Query API to find a
            // matching template
            url = new URL(baseVcdUrl + "/query?" + queryParameters);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid URL: " + baseVcdUrl);
        }

        HttpGet httpGet = new HttpGet(url.toString());
        httpGet.setHeader(HttpHeaders.ACCEPT, SampleConstants.APPLICATION_PLUS_XML_VERSION
                + version);

        httpGet.setHeader(SampleConstants.VCD_AUTHORIZATION_HEADER, token);

        HttpResponse response = HttpUtils.httpInvoke(httpGet);

        // make sure the status is 200 OK
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            results = HttpUtils.unmarshal(response.getEntity(), QueryResultRecordsType.class);
        }

        return results;
    }

    /**
     * Gets the string content from the passed in InputStream
     * 
     * @param is
     *            response stream from GET/POST method call
     * @return String content of the passed in InputStream
     */
    public static String getContent(InputStream is) {
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        throw new RuntimeException(e);
                    }
                }
            }

            return sb.toString();
        }

        return "";
    }

    /**
     * This method uses the vCloud API Query service
     * 
     * @param baseVcdUrl
     * @param queryParameters
     * @return
     */

    /*
     * public static QueryResultRecordsType getQueryResults(String baseVcdUrl, String
     * queryParameters, DefaultSampleCommandLineOptions options, String vCloudToken) { URL url =
     * null; QueryResultRecordsType results = null; try { // Construct the URL from the baseVcdUrl
     * to utilize the vCloud Query API to find a // matching template url = new URL(baseVcdUrl +
     * "/api/query?" + queryParameters); } catch (MalformedURLException e) { throw new
     * RuntimeException("Invalid URL: " + baseVcdUrl); } HttpGet httpGet = new
     * HttpGet(url.toString()); httpGet.setHeader(HttpHeaders.ACCEPT,
     * SampleConstants.APPLICATION_PLUS_XML_VERSION + options.vcdVersion);
     * httpGet.setHeader(SampleConstants.VCD_AUTHORIZATION_HEADER, vCloudToken); HttpResponse
     * response = HttpUtils.httpInvoke(httpGet); // make sure the status is 200 OK if
     * (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) { results =
     * HttpUtils.unmarshal(response.getEntity(), QueryResultRecordsType.class); } return results; }
     */

    public static class XMLGregorianCalendarDeserializer implements JsonDeserializer<XMLGregorianCalendar> {
        public XMLGregorianCalendar deserialize(JsonElement je, Type type,
                JsonDeserializationContext jdc) throws JsonParseException {
            JsonObject jo = je.getAsJsonObject();
            GregorianCalendar c = new GregorianCalendar();
            if (null == jo) {
                System.out.println("DESERIALIZING JO IS NULL");
            } else {
                System.out.println("TOS created date is " + jo.getAsString());
            }
            try {
                XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar();
            } catch (DatatypeConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.out);
            }
            // Location l = new Location(jo.getAsJsonPrimitive("mProvider").getAsString());
            // l.setAccuracy(jo.getAsJsonPrimitive("mAccuracy").getAsFloat());
            // etc, getting and setting all the data
            return null;
        }
    }

    public static class XMLGregorianClassConverter {
        public static class Serializer implements JsonSerializer {
            public Serializer() {
                super();
            }

            public JsonElement serialize(Object t, Type type,
                    JsonSerializationContext jsonSerializationContext) {
                XMLGregorianCalendar xgcal = (XMLGregorianCalendar) t;
                return new JsonPrimitive(xgcal.toXMLFormat());
            }
        }

        public static class Deserializer implements JsonDeserializer {
            public Object deserialize(JsonElement jsonElement, Type type,
                    JsonDeserializationContext jsonDeserializationContext) {
                try {
                    return DatatypeFactory.newInstance().newXMLGregorianCalendar(
                            jsonElement.getAsString());
                } catch (Exception e) {
                    return null;
                }
            }
        }
    }
}