
package org.neogroup.sparks.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebRouteIndex {

    private List<WebRouteEntry> genericRoutes;
    private List<WebRouteEntry> routes;
    private Map<String, WebRouteIndex> routeIndexes;

    public WebRouteIndex() {
        genericRoutes = new ArrayList<>();
        routes = new ArrayList<>();
        routeIndexes = new HashMap<>();
    }

    public List<WebRouteEntry> getRoutes() {
        return routes;
    }

    public void addRoute(WebRouteEntry route) {
        routes.add(route);
    }

    public List<WebRouteEntry> getGenericRoutes() {
        return genericRoutes;
    }

    public void addGenericRoute(WebRouteEntry route) {
        genericRoutes.add(route);
    }

    public void addRouteIndex (String context, WebRouteIndex index) {
        routeIndexes.put(context, index);
    }

    public WebRouteIndex getRouteIndex (String context) {
        return routeIndexes.get(context);
    }
}
