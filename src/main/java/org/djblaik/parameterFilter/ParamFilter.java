package org.djblaik.parameterFilter;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 *
 * @author Tanin
 */
@SuppressWarnings("rawtypes")
public class ParamFilter extends Filter {
    private static final ResourceBundle FosEn = ResourceBundle.getBundle("Fos_En");

    @Override
    public String description() {
        return FosEn.getString("parses.the.requested.uri.for.parameters");
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain)
            throws IOException {
        parseGetParameters(exchange);
        parsePostParameters(exchange);
        chain.doFilter(exchange);
    }

    private void parseGetParameters(HttpExchange exchange)
            throws UnsupportedEncodingException {

        LinkedHashMap parameters = new LinkedHashMap();
        URI requestedUri = exchange.getRequestURI();
        String query = requestedUri.getRawPath();
        query = query.replace("/","");
        if (query.equals("")) {query = null;}
        parseQuery(query, parameters);
        exchange.setAttribute("parameters", parameters);



    }

    private void parsePostParameters(HttpExchange exchange)
            throws IOException {

        if (FosEn.getString("post").equalsIgnoreCase(exchange.getRequestMethod())) {
            LinkedHashMap parameters = (LinkedHashMap) exchange.getAttribute("parameters");
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine();
            parseQuery(query, parameters);
        }
    }

    @SuppressWarnings("unchecked")
    private void parseQuery(String query, Map parameters)
            throws UnsupportedEncodingException {

        if (query != null) {
            String[] pairs = query.split("&");

            for (String pair : pairs) {
                String[] param = pair.split("=");

                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0],
                            System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1],
                            System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if(obj instanceof List values) {
                        values.add(value);
                    } else if(obj instanceof String) {
                        List values = new ArrayList();
                        values.add(obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }
}

