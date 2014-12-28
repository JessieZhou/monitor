package util;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenlingpeng on 2014/12/28.
 */
public class HttpClientUtil {
    public static final java.lang.String defaultUserAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.2) Gecko/2008070208 Firefox/3.0.1";
    public static final java.lang.String defaultAccept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
    public static final java.lang.String defaultAcceptEncoding = "gzip,deflate,sdch";
    public static final java.lang.String defaultAcceptLanguage = "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4";

    public static CloseableHttpClient getHttpClient() {
        MessageConstraints messageConstraints = MessageConstraints.custom()
                .setMaxHeaderCount(200)
                .setMaxLineLength(5000)
                .build();

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8)
                .setBufferSize(64 * 1024)
                .setMessageConstraints(messageConstraints)
                .build();

        RequestConfig globalConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.BEST_MATCH)
                .setCircularRedirectsAllowed(false)
                .setRedirectsEnabled(false)
                .setConnectTimeout(10000)
                .setSocketTimeout(10000)
//        .setConnectionRequestTimeout(10000)
                .build();

//        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
//        cm.setMaxTotal(threanNum + 5);
//        cm.setDefaultMaxPerRoute(threanNum + 5);
//        cm.setDefaultConnectionConfig(connectionConfig);
        BasicHttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        cm.setConnectionConfig(connectionConfig);
        List<Header> headers = new ArrayList<>(4);
        headers.add(new BasicHeader("User-Agent", defaultUserAgent));
        headers.add(new BasicHeader("Accept", defaultAccept));
        headers.add(new BasicHeader("Accept-Encoding", defaultAcceptEncoding));
        headers.add(new BasicHeader("Accept-Language", defaultAcceptLanguage));

        return HttpClients.custom().setConnectionManager(cm)
                .setDefaultRequestConfig(globalConfig)
//                .setDefaultCookieStore(cookieStore)
                .setRetryHandler(new HttpRequestRetryHandler() {
                    @Override
                    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                        if (executionCount >= 2) {
                            // Do not retry if over max retry count
                            return false;
                        }
                        if (exception instanceof InterruptedIOException) {
                            // Timeout
                            return false;
                        }
                        if (exception instanceof UnknownHostException) {
                            // Unknown host
                            return false;
                        }
                        if (exception instanceof SSLException) {
                            // SSL handshake exception
                            return false;
                        }
                        HttpClientContext clientContext = HttpClientContext.adapt(context);
                        HttpRequest request = clientContext.getRequest();
                        return !(request instanceof HttpEntityEnclosingRequest);
                    }
                })
//        .setRetryHandler(new DefaultHttpRequestRetryHandler())
                .setDefaultHeaders(headers)
                .build();
    }

    public static CloseableHttpResponse executeHttpGet(String url, CloseableHttpClient client) throws IOException {
        HttpGet get = new HttpGet(url);
        return client.execute(get);
    }
}
