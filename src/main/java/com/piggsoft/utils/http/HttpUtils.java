package com.piggsoft.utils.http;

import com.alibaba.fastjson.JSON;
import com.piggsoft.exception.HttpException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author piggsoft@163.com
 * Created by user on 2015/11/16.
 * http 请求的工具类
 */
public class HttpUtils {

    /**LOGGER**/
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * 连接超时时间，单位毫秒
     */
    private static int TIMEOUT = 10 * 1000;

    /**
     * 默认的字符编码
     */
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * 公共的请求配置
     */
    private static final RequestConfig CONFIG = RequestConfig.custom()
            .setConnectionRequestTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
            .setSocketTimeout(TIMEOUT).build();

    /**
     * reponse 回调
     */
    private static ResponseHandler RESPONSE_HANDLER = new ResponseHandler();

    /**
     * post 请求
     * @param url 请求地址
     * @param params 参数
     * @return response string
     */
    public static String post(String url, Map<String, Object> params) {
        return post(url, DEFAULT_ENCODING, params, null);
    }

    /**
     * post 请求
     * @param url 请求地址
     * @param content post内容
     * @return response string
     */
    public static String post(String url, String content) {
        return post(url, DEFAULT_ENCODING, null, content);
    }

    /**
     * post 请求
     * @param uri 请求地址
     * @param content post内容
     * @return response string
     */
    public static String post(URI uri, String content) {
        return post(uri, DEFAULT_ENCODING, null, content);
    }

    /**
     * post 请求
     * @param url 请求地址
     * @param charset 字符集
     * @param params 参数
     * @param content post内容
     * @return response string
     */
    public static String post(String url, String charset, Map<String, Object> params, String content) {
        return doPost(url, charset, params, content);
    }

    /**
     * post 请求
     * @param uri 请求地址
     * @param charset 字符集
     * @param params 参数
     * @param content post内容
     * @return response string
     */
    public static String post(URI uri, String charset, Map<String, Object> params, String content) {
        return doPost(uri, charset, params, content);
    }

    /**
     * post 请求
     * @param url 请求地址
     * @param charset 字符集
     * @param params 参数
     * @param content post内容
     * @throws HttpException HttpException
     * @return response string
     */
    private static String doPost(String url, String charset, Map<String, Object> params, String content) throws HttpException {
        return doPost(URI.create(url), charset, params, content);
    }

    /**
     * post 请求
     * @param uri 请求地址
     * @param charset 字符集
     * @param params 参数
     * @param content post内容
     * @throws HttpException HttpException
     * @return response string
     */
    private static String doPost(URI uri, String charset, Map<String, Object> params, String content) throws HttpException {
        try {
            HttpEntity httpEntity = getEntity(params, content, charset);
            return post(uri, httpEntity);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
            throw new HttpException(e);
        }
    }

    /**
     * 解析参数类信息到httpEntity， params ， content， charset 都为非必传
     * @param params 参数
     * @param content post内容
     * @param charset 字符集
     * @return {@link HttpEntity}
     * @throws UnsupportedEncodingException UnsupportedEncodingException
     */
    private static HttpEntity getEntity(Map<String, Object> params, String content, String charset) throws UnsupportedEncodingException {
        if (params != null && !params.isEmpty()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("请求：params({});chartset({})", JSON.toJSONString(params), charset);
            }
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (!ObjectUtils.isEmpty(entry.getValue())) {
                    nameValuePairs.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
                }
            }
            return new UrlEncodedFormEntity(nameValuePairs, charset);
        }
        if (StringUtils.isNotEmpty(content)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("请求：params({});chartset({})", content, charset);
            }
            return new StringEntity(content, ContentType.create("application/x-www-form-urlencoded", Charset.forName(charset)));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("请求：没有参数");
        }
        return new StringEntity("");
    }

    /**
     * get 请求
     * @param url 请求地址
     * @param params 参数
     * @return response string
     */
    public static String get(String url, Map<String, Object> params) {
        return get(url, params, DEFAULT_ENCODING);
    }

    /**
     * get 请求
     * @param url 请求地址
     * @return response string
     */
    public static String get(String url) {
        return get(url, null, DEFAULT_ENCODING);
    }

    /**
     * get 请求
     * @param url 请求地址
     * @param params 参数
     * @param encoding 字符集
     * @return response string
     */
    public static String get(String url, Map<String, Object> params, String encoding) {
        return get(URI.create(url), params, encoding);
    }

    /**
     * get 请求
     * @param uri 请求地址
     * @param params 参数
     * @return response string
     */
    public static String get(URI uri, Map<String, Object> params) {
        return get(uri, params, DEFAULT_ENCODING);
    }

    /**
     * get 请求
     * @param uri 请求地址
     * @return response string
     */
    public static String get(URI uri) {
        return get(uri, null, null);
    }

    /**
     * get 请求
     * @param uri 请求地址
     * @param params 参数
     * @param encoding 字符集
     * @return response string
     */
    public static String get(URI uri, Map<String, Object> params, String encoding) {
        RequestBuilder requestBuilder = RequestBuilder.get();
        requestBuilder.setUri(uri);
        String requestEncoding = StringUtils.isEmpty(encoding) ? DEFAULT_ENCODING : encoding;
        requestBuilder.setHeader("Content-Type", "text/xml;charset=" + requestEncoding);
        if (null != params && !params.isEmpty()) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (!ObjectUtils.isEmpty(entry.getValue())) {
                    requestBuilder.addParameter(entry.getKey(), String.valueOf(entry.getValue()));
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("key : {} ; value : {}", entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        HttpUriRequest get = requestBuilder.build();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Send Request : {}", get.getRequestLine().toString());
        }
        return send(get);
    }

    /**
     * 文件上传
     * @param url 请求地址
     * @param file 要上传的文件
     * @return response string
     */
    public static String postChunk(String url, File file) {
        try {
            InputStreamEntity httpEntity = new InputStreamEntity(
                    new FileInputStream(file), -1, ContentType.APPLICATION_OCTET_STREAM);
            return post(url, httpEntity);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
            throw new HttpException(e);
        }
    }


    /**
     * post请求
     * @param url 请求地址
     * @param entity {@link HttpEntity}
     * @return response string
     */
    public static String post(String url, HttpEntity entity) {
        return post(URI.create(url), entity);
    }

    /**
     * post请求
     * @param uri 请求地址
     * @param entity {@link HttpEntity}
     * @return response string
     */
    public static String post(URI uri, HttpEntity entity) {
        HttpPost post = new HttpPost(uri);
        post.setConfig(CONFIG);
        post.setEntity(entity);
        return send(post);
    }

    /**
     * 发送请求
     * @param request 请求
     * @return 返回结果
     */
    private static String send(HttpUriRequest request) {
        String responseMessage = null;
        CloseableHttpClient httpClient = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Send Request : {}", request.getRequestLine().toString());
            }
            httpClient = HttpClients.createDefault();
            responseMessage = httpClient.execute(request, RESPONSE_HANDLER);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new HttpException(e);
        }
        return responseMessage;
    }


    static class ResponseHandler implements org.apache.http.client.ResponseHandler<String> {
        /**
         * 对请求结果作初步处理
         * @param response 请求返回的结果
         * @return response string
         * @throws IOException IOException
         */
        public String handleResponse(HttpResponse response) throws IOException {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, "UTF-8") : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        }
    }

}
