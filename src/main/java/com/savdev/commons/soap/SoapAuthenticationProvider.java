package com.savdev.commons.soap;

import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import javax.xml.ws.BindingProvider;
import java.util.Map;
import java.util.function.Supplier;

public class SoapAuthenticationProvider {

  public  <T> T getAuthenticatedSoapPort(
    Supplier<T> rawSoapProvider,
    final String login,
    final String password) {

    T soapPort = rawSoapProvider.get();
    BindingProvider bindingProvider = (BindingProvider) soapPort;
    final Map<String, Object> requestContext =
      bindingProvider.getRequestContext();
    requestContext.put(BindingProvider.USERNAME_PROPERTY,
      login);
    requestContext.put(BindingProvider.PASSWORD_PROPERTY,
      password);

    final HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
    httpClientPolicy.setAllowChunking(true);

    final HTTPConduit httpConduit =
      (HTTPConduit) ClientProxy.getClient(soapPort)
        .getConduit();
    httpConduit.setClient(httpClientPolicy);

    return soapPort;
  }
}
