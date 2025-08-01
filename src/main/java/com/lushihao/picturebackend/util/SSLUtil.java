package com.lushihao.picturebackend.util;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/**
 * 用于跳过SSL证书验证
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-01   9:30
 */
public class SSLUtil {
    public static void ignoreSsl() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // 忽略主机名验证
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
}
