package fr.aeris.gewex.maintenance;import java.security.cert.X509Certificate;import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;public class SslUtils {    public static  SSLContext getSslContext() {
       SSLContext sc = null;
       try {
       sc = SSLContext.getInstance("SSL");
       sc.init(null, getTrustManagers(), new java.security.SecureRandom());
       }
       catch (Exception e) {
       }
       return sc;
   }    public static TrustManager[] getTrustManagers() {
       TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
           @Override
           public java.security.cert.X509Certificate[] getAcceptedIssuers() {
               return null;
           }            @Override
           public void checkClientTrusted(X509Certificate[] certs, String authType) {
           }            @Override
           public void checkServerTrusted(X509Certificate[] certs, String authType) {
           }
       } };
       return trustAllCerts;
   }
public static Client getClientForUrl(String url) {
	Client client = null;
    if (url.toLowerCase().startsWith("https")) {
            client = ClientBuilder.newBuilder().sslContext(SslUtils.getSslContext()).build();
        }                else {
            client = ClientBuilder.newClient();
        }
    return client;

}}