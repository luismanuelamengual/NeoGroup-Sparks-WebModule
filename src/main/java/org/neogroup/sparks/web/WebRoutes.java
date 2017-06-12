
package org.neogroup.sparks.web;

import org.neogroup.httpserver.HttpRequest;

public class WebRoutes {

    private static final String ROUTE_GENERIC_PATH = "*";
    private static final String ROUTE_PARAMETER_PREFIX = ":";
    private static final String ROUTE_PARAMETER_WILDCARD = "%";
    private static final String ROUTE_PATH_SEPARATOR = "/";

    private final WebRouteIndex routeIndex;

    public WebRoutes() {
        routeIndex = new WebRouteIndex();
    }

    /**
     * Adds a new web route for a controller method
     * @param route Route for controller method
     */
    public void addWebRoute (WebRouteEntry route) {

        String path = route.getPath();
        String[] pathParts = path.split(ROUTE_PATH_SEPARATOR);
        WebRouteIndex currentRootIndex = routeIndex;
        for (String pathPart : pathParts) {
            if (pathPart.isEmpty()) {
                continue;
            }
            String index = null;
            if (pathPart.startsWith(ROUTE_PARAMETER_PREFIX)) {
                index = ROUTE_PARAMETER_WILDCARD;
            } else {
                index = pathPart;
            }
            WebRouteIndex routeIndex = currentRootIndex.getRouteIndex(index);
            if (routeIndex == null) {
                routeIndex = new WebRouteIndex();
                currentRootIndex.addRouteIndex(index, routeIndex);
            }
            currentRootIndex = routeIndex;
            if (index.equals(ROUTE_GENERIC_PATH)) {
                break;
            }
        }
        currentRootIndex.addRoute(route);
    }

    /**
     * Finds a web route from an http request
     * @param request http request
     * @return route for a controller method
     */
    public WebRouteEntry findWebRoute (HttpRequest request) {

        String[] pathParts = request.getPath().split(ROUTE_PATH_SEPARATOR);
        WebRouteEntry webRoute = findWebRoute(request, routeIndex, pathParts, 0);

        if (webRoute != null) {
            if (webRoute.getPath().contains(ROUTE_PARAMETER_PREFIX)) {
                String[] routePathParts = webRoute.getPath().split(ROUTE_PATH_SEPARATOR);
                for (int i = 0; i < routePathParts.length; i++) {
                    String pathPart = routePathParts[i];
                    if (pathPart.startsWith(ROUTE_PARAMETER_PREFIX)) {
                        String parameterName = pathPart.substring(1);
                        String parameterValue = pathParts[i];
                        request.setParameter(parameterName, parameterValue);
                    }
                }
            }
        }

        return webRoute;
    }

    /**
     * Finds a web route from an http request
     * @param request http request
     * @param currentRootIndex current root index
     * @param pathParts path parts
     * @param pathIndex index of path
     * @return route for a controller method
     */
    protected WebRouteEntry findWebRoute (HttpRequest request, WebRouteIndex currentRootIndex, String[] pathParts, int pathIndex) {

        WebRouteEntry route = null;
        if (pathIndex >= pathParts.length) {
            for (WebRouteEntry routeEntry : currentRootIndex.getRoutes()) {
                if (routeEntry.getHttpMethod() == null || routeEntry.getHttpMethod().equals(request.getMethod())) {
                    route = routeEntry;
                    break;
                }
            }
            if (route == null) {
                WebRouteIndex genericRouteIndex = currentRootIndex.getRouteIndex(ROUTE_GENERIC_PATH);
                if (genericRouteIndex != null) {
                    route = findWebRoute(request, genericRouteIndex, pathParts, pathParts.length);
                }
            }
        }
        else {
            String pathPart = pathParts[pathIndex];
            if (pathPart.isEmpty()) {
                route = findWebRoute(request, currentRootIndex, pathParts, pathIndex + 1);
            }
            else {
                WebRouteIndex nextRootIndex = currentRootIndex.getRouteIndex(pathPart);
                if (nextRootIndex != null) {
                    route = findWebRoute(request, nextRootIndex, pathParts, pathIndex + 1);
                }
                if (route == null) {
                    nextRootIndex = currentRootIndex.getRouteIndex(ROUTE_PARAMETER_WILDCARD);
                    if (nextRootIndex != null) {
                        route = findWebRoute(request, nextRootIndex, pathParts, pathIndex + 1);
                    }
                }
                if (route == null) {
                    nextRootIndex = currentRootIndex.getRouteIndex(ROUTE_GENERIC_PATH);
                    if (nextRootIndex != null) {
                        route = findWebRoute(request, nextRootIndex, pathParts, pathParts.length);
                    }
                }
            }
        }
        return route;
    }
}
